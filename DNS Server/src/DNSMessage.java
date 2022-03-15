import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//This corresponds to an entire DNS Message.
public class DNSMessage {

    //Member Variables
    //The DNS Header
    DNSHeader dnsHeader;

    //An arraylist of questions objects
    ArrayList<DNSQuestion> dnsQuestions = new ArrayList<>();

    //An arraylist of answers objects
    ArrayList<DNSRecord> dnsAnswers = new ArrayList<>();

    //An arraylist of "authority records" objects which will be ignored
    ArrayList<DNSRecord> dnsAuthorityRecords = new ArrayList<>();

    //An arraylist of "additional records" objects which will almost be ignored
    ArrayList< DNSRecord> dnsAdditionalRecords = new ArrayList<>();

    //The byte array containing the complete message in this class.
    //Needed to handle the compression technique
    byte[] messageBytes;


    //Static constructor
    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        //Create new DNSMessage Object
        DNSMessage dnsMessage = new DNSMessage();

        //Save the byte stream to member variable
        dnsMessage.messageBytes = bytes;

        //Create input stream to read the message bytes sequentially
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dnsMessage.messageBytes);

        //Create DNSHeader object and decode the header
        dnsMessage.dnsHeader = DNSHeader.decodeHeader(byteArrayInputStream);

        //Create DNSQuestion objects, decode the questions, and add them to an array
        for (int i = 0; i < dnsMessage.dnsHeader.getQDCOUNT(); i++) {
            dnsMessage.dnsQuestions.add(DNSQuestion.decodeQuestion(byteArrayInputStream, dnsMessage));
        }

        //Create DNSRecord objects and add them to the appropriate dnsAnswers
        for (int i = 0; i < dnsMessage.dnsHeader.getANCOUNT(); i++) {
            dnsMessage.dnsAnswers.add(DNSRecord.decodeRecord(byteArrayInputStream, dnsMessage));
        }

        //Create DNSRecord objects and add them to the appropriate dnsAuthorityRecords
        for (int i = 0; i < dnsMessage.dnsHeader.getNSCOUNT(); i++) {
            dnsMessage.dnsAuthorityRecords.add(DNSRecord.decodeRecord(byteArrayInputStream, dnsMessage));
        }

        //Create DNSRecord objects and add them to the appropriate dnsAdditionalRecords
        for (int i = 0; i < dnsMessage.dnsHeader.getARCOUNT(); i++) {
            dnsMessage.dnsAdditionalRecords.add(DNSRecord.decodeRecord(byteArrayInputStream, dnsMessage));
        }

        //Return the complete DNSMessage
        return dnsMessage;
    }


    //Read the pieces of a domain name starting from the current position of the input stream
    ArrayList<String> readDomainName(InputStream inputStream) throws IOException {

        //Temp variables
        int subDomainSize = 0;
        byte[] tempBytes;
        String subDomainString = "";
        ArrayList<String> subDomainNames = new ArrayList<>();

        //Process all subdomains, break when terminating character received.
        while (true) {
            //Read in the one byte
            tempBytes = inputStream.readNBytes(1);

            //If byte is the terminating character break
            if (new String(tempBytes).equals("\0")) {
                break;
            }

            //Convert byte to int representing the message size
            subDomainSize = tempBytes[0];

            //Read in the message (bytes) based on message size
            tempBytes = inputStream.readNBytes(subDomainSize);

            //Convert message bytes to String
            subDomainString = new String(tempBytes);

            //Add subdomain to arraylist member variable
            subDomainNames.add(subDomainString);

        }

//        System.out.println(subDomainNames);

        //Return domain array
        return subDomainNames;

    }


    //Same, but used when there's compression and to find the domain from earlier in the message.
    //This method makes a ByteArrayInputStream that starts at the specified byte and calls the other version of this method
    ArrayList<String> readDomainName(int firstByte) throws IOException {
        //Create new input stream from the message bytes
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBytes);

        //Advance pointer in input stream to beginning of domain name
        byteArrayInputStream.readNBytes(firstByte);

        //Return domain name string array
        return readDomainName(byteArrayInputStream);
    }


    //Build a response based on the request and the answers you intend to send back.
    static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers) {
        //Create a new instance of DNSMessage to reply with
        DNSMessage dnsMessage = new DNSMessage();

        //Copy the additional records over (should include a dig record with a type of 41.)
        dnsMessage.dnsAdditionalRecords = request.dnsAdditionalRecords;

        //Copy the authority records over
        dnsMessage.dnsAuthorityRecords = request.dnsAuthorityRecords;

        //Copy the arraylist of answers to the new object
        dnsMessage.dnsAnswers = answers;

        //Copy the questions over
        dnsMessage.dnsQuestions = request.dnsQuestions;

        //Construct the header based on the information in the previous sections
        dnsMessage.dnsHeader = DNSHeader.buildResponseHeader(request, dnsMessage);

        //Return the completed DNSMessage
        return dnsMessage;
    }


    //Get the bytes to put in a packet and send back
    byte[] toBytes() throws IOException {

        //Create output stream and send to other methods for writing
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //Hashmap to store domain names and output stream locations for compression
        HashMap<String, Integer> domainsHashmap = new HashMap<>();

        //Write the header using writeBytes
        this.dnsHeader.writeBytes(byteArrayOutputStream);

        //For each of the following Questions/Records, write to the output stream
        for (DNSQuestion dnsQuestion : this.dnsQuestions) {
            dnsQuestion.writeBytes(byteArrayOutputStream, domainsHashmap);
        }

        for (DNSRecord dnsRecord : this.dnsAnswers) {
            dnsRecord.writeBytes(byteArrayOutputStream, domainsHashmap);
        }

        for (DNSRecord dnsRecord : this.dnsAuthorityRecords) {
            dnsRecord.writeBytes(byteArrayOutputStream, domainsHashmap);
        }

        for (DNSRecord dnsRecord : this.dnsAdditionalRecords) {
            dnsRecord.writeBytes(byteArrayOutputStream, domainsHashmap);
        }

        //Convert output stream to byte array and return
        return byteArrayOutputStream.toByteArray();

    }


    //If this first time this domain name has been seen in the packet, write it using the DNS encoding (each segment of
    //the domain prefixed with its length, 0 at the end), and add it to the hash map. Otherwise, write a back pointer to where
    //the domain has been seen previously.
    static void writeDomainName(ByteArrayOutputStream byteArrayOutputStream, HashMap<String, Integer> domainHashMap, ArrayList<String> domainPieces) throws IOException {

        //Convert subdomains to full domain name string
        String fullDomain = DNSMessage.octetsToString(domainPieces);

        //If the hashmap already has the domain, then it has been used before and a pointer is needed
        if (domainHashMap.containsKey(fullDomain)){

            //Get the pointer location of the domain
            int domainPointer = domainHashMap.get(fullDomain);

            //Temp byte array
            byte[] bytes = new byte[2];

            //Set the first byte
            bytes[0] = (byte) ((domainPointer >>> 8) | 0xC0);

            //Set the second byte
            bytes[1] = (byte) (domainPointer & 0xFF);

            //Write compressed bytes to output stream
            byteArrayOutputStream.write(bytes);

        } else{
            //Save pointer to hashmap
            domainHashMap.put(fullDomain, byteArrayOutputStream.size());

            //Loop through each subdomain
            for (String str : domainPieces) {

                //Write the length of each subdomain to the output stream
                byteArrayOutputStream.write(str.length());

                //For each character
                for (int i = 0; i < str.length(); i++) {

                    //Write out each character in the subdomain to the output stream
                    byteArrayOutputStream.write(str.charAt(i));
                }

            }

            //Write the 0 terminating character to the output stream
            byteArrayOutputStream.write(0);

        }

    }


    //Join the pieces of a domain name with dots ([ "utah", "edu"] -> "utah.edu" )
    static String octetsToString(ArrayList<String> octets) {
        return String.join(".", octets);
    }

    //Takes in a short and returns it as a byte array
    static byte[] shortToBytes(short s){
        return ByteBuffer.allocate(2).putShort(s).array();
    }

    //Takes in an integer and returns it as a byte array
    static byte[] intToBytes(int i){
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    //IDE generated toString method. Useful for debugging
    @Override
    public String toString() {
        return "DNSMessage{" +
                "dnsHeader=" + dnsHeader +
                ", dnsQuestions=" + dnsQuestions +
                ", dnsAnswers=" + dnsAnswers +
                ", dnsAuthorityRecords=" + dnsAuthorityRecords +
                ", dnsAdditionalRecords=" + dnsAdditionalRecords +
                ", messageBytes=" + Arrays.toString(messageBytes) +
                '}';
    }

}
