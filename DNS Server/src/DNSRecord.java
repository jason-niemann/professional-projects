import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//Everything after the header and question parts of the DNS message are stored as records. This should have all
// the fields listed in the spec as well as a Date object storing when this record was created.
public class DNSRecord {

    //Custom Member Variables
    Instant creationTime;

    //Required Member Variables per the spec
    ArrayList<String> NAME;
    short TYPE;
    short CLASS;
    int TTL;
    short RDLENGTH;
    byte[] RDATA;


    //Decode the record
    static DNSRecord decodeRecord(InputStream inputStream, DNSMessage dnsMessage) throws IOException {

        //New instance of DNSRecord
        DNSRecord dnsRecord = new DNSRecord();

        //Temp byte array
        byte[] bytes;

        //Mark the current location in the input stream
        inputStream.mark(2);

        //Get NAME if compressed...
        bytes = inputStream.readNBytes(2);

        //If the first two bits are set...
        if ((bytes[0] & 0xC0) == 0xC0){

            //Get the offset
            int nameOffset = (((bytes[0] & 0x3F) << 8) | (bytes[1] & 0xFF));

            //Call the read domain method using the offset
            dnsRecord.NAME = dnsMessage.readDomainName(nameOffset);
        }
        //If not compressed...
        else{
            //Reset the input stream to the original position
            inputStream.reset();

            //Call the read domain method without compression
            dnsRecord.NAME = dnsMessage.readDomainName(inputStream);
        }

        //Get TYPE
        bytes = inputStream.readNBytes(2);
        dnsRecord.TYPE = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("DNS Record TYPE: " + String.format("%x", dnsRecord.TYPE));

        //Get CLASS
        bytes = inputStream.readNBytes(2);
        dnsRecord.CLASS = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("DNS Record CLASS: " + String.format("%x", dnsRecord.CLASS));

        //Get TTL
        bytes = inputStream.readNBytes(4);
        dnsRecord.TTL = bytes[0] << 24;
        dnsRecord.TTL = dnsRecord.TTL | ((bytes[1] & 0xFF) << 16);
        dnsRecord.TTL = dnsRecord.TTL | ((bytes[2] & 0xFF) << 8);
        dnsRecord.TTL = dnsRecord.TTL | (bytes[3] & 0xFF);
//        System.out.println("DNS Record TTL: " + String.format("%x", dnsRecord.TTL));

        //Get RDLENGTH
        bytes = inputStream.readNBytes(2);
        dnsRecord.RDLENGTH = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("DNS Record RDLENGTH: " + String.format("%x", dnsRecord.RDLENGTH));

        //Get RDATA, array of bytes. No bit shifting
        dnsRecord.RDATA = inputStream.readNBytes(dnsRecord.RDLENGTH);
//        System.out.println("DNS Record RDATA: " + Arrays.toString(dnsRecord.RDATA) + "\n");

        //Set time of record creation
        dnsRecord.creationTime = Instant.now();

        return dnsRecord;
    }

    //Write out all record parts
    void writeBytes(ByteArrayOutputStream byteArrayOutputStream, HashMap<String, Integer> domainNameLocations) throws IOException {

        //Use writeDomainName method to write name (or compression scheme) to output stream
        DNSMessage.writeDomainName(byteArrayOutputStream, domainNameLocations, this.NAME);

        //Write each of the remaining DNSRecord sections to the output stream
        byteArrayOutputStream.write(DNSMessage.shortToBytes(this.TYPE));
        byteArrayOutputStream.write(DNSMessage.shortToBytes(this.CLASS));
        byteArrayOutputStream.write(DNSMessage.intToBytes(this.TTL));
        byteArrayOutputStream.write(DNSMessage.shortToBytes(this.RDLENGTH));
        byteArrayOutputStream.write(this.RDATA);

    }

    //IDE generated toString method. Useful for debugging
    @Override
    public String toString() {
        return "DNSRecord{" +
                "creationTime=" + creationTime +
                ", NAME=" + NAME +
                ", TYPE=" + TYPE +
                ", CLASS=" + CLASS +
                ", TTL=" + TTL +
                ", RDLENGTH=" + RDLENGTH +
                ", RDATA=" + Arrays.toString(RDATA) +
                '}';
    }

    //Return whether the creation date + the time to live is after the current time.
    //The Date and Calendar classes will be useful for this.
    boolean timestampValid(){

        //Calculate the difference between the creation time and the current time in seconds
        long difference = Instant.now().getEpochSecond() - creationTime.getEpochSecond();

        //Return whether the difference is greater than the Time to live
        return difference < this.TTL;
    }

}
