import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//This class should store all the data provided by the 12 byte DNS header. See the spec for all the fields needed.
public class DNSHeader {

    //dig example.com @127.0.0.1 -p 8053 +tries=1

    //https://www.ietf.org/rfc/rfc1035.txt  Page 25
    //https://datatracker.ietf.org/doc/html/rfc5395

    //Member Variables
    short ID;       //This identifier is copied to the corresponding reply
    boolean QR;     //Query (0) or response (1)
    byte OPCODE;    //standard query: 0 | an inverse query: 1 | server status request: 2
    boolean AA;     //Authoritative Answer
    boolean TC;     //Truncation
    boolean RD;     //Recursion Desired
    boolean RA;     //Recursion Available
    boolean Z;      //Reserved
    boolean AD;     //Authentic Data
    boolean CD;     //Checking Disabled
    byte RCODE;     //Response code: No error(0), Format error(1), Server failure(2), Name Error(3), Not Implemented(4), Refused(5)
    short QDCOUNT;  //Number of entries in the question section.
    short ANCOUNT;  //Number of resource records in the answer section
    short NSCOUNT;  //Number of name server resource records in the authority records section.
    short ARCOUNT;  //Number of resource records in the additional records section.


    //Read the header from an input stream
    static DNSHeader decodeHeader(InputStream inputStream) throws IOException {

        //New instance of DNSHeader
        DNSHeader dnsHeader = new DNSHeader();

        //Temp byte array
        byte[] bytes;

        //Get ID
        bytes = inputStream.readNBytes(2);
        dnsHeader.ID = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("Header ID: " + String.format("%x", dnsHeader.ID));

        //Get QR
        bytes = inputStream.readNBytes(1);
        dnsHeader.QR = ((bytes[0] & 0x80) != 0);
//        System.out.println("Header QR: " + (dnsHeader.QR ? "Response" : "Query"));

        //Get OPCode
        dnsHeader.OPCODE = (byte)((bytes[0] & 0x78) >> 3);
//        System.out.println("Header OpCode: " + String.format("%x", dnsHeader.OPCODE));

        //Get QR
        dnsHeader.AA = ((bytes[0] & 0x04) != 0);
//        System.out.println("Header AA: " + (dnsHeader.AA ? "Server is an authority for the domain name" : "Not an authority"));

        //Get TC
        dnsHeader.TC = ((bytes[0] & 0x02) != 0);
//        System.out.println("Header TC: " + (dnsHeader.TC ? "Truncated" : "Not truncated"));

        //Get RD
        dnsHeader.RD = ((bytes[0] & 0x01) != 0);
//        System.out.println("Header RD: " + (dnsHeader.RD ? "Recursion Desired" : "Recursion Not Desired"));

        //Get RA
        bytes = inputStream.readNBytes(1);
        dnsHeader.RA = ((bytes[0] & 0x80) != 0);
//        System.out.println("Header RA: " + (dnsHeader.RA ? "Recursion available" : "Recursion not available"));

        //Get Z
        dnsHeader.Z = ((bytes[0] & 0x40) != 0);
//        System.out.println("Header Z: " + (dnsHeader.Z ? "True" : "False"));

        //Get AD
        dnsHeader.AD = ((bytes[0] & 0x20) != 0);
//        System.out.println("Header AD: " + (dnsHeader.AD ? "Bit set" : "Bit not set"));

        //Get CD
        dnsHeader.CD = ((bytes[0] & 0x10) != 0);
//        System.out.println("Header CD: " + (dnsHeader.CD ? "Non-authenticated data: Acceptable" : "Non-authenticated data: Unacceptable"));

        //Get RCODE
        dnsHeader.RCODE = (byte)((bytes[0] & 0x0F));
//        System.out.println("Header RCODE: " + String.format("%x", dnsHeader.RCODE));

        //Get QDCOUNT
        bytes = inputStream.readNBytes(2);
        dnsHeader.QDCOUNT = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("Header QDCOUNT: " + String.format("%x", dnsHeader.QDCOUNT));

        //Get ANCOUNT
        bytes = inputStream.readNBytes(2);
        dnsHeader.ANCOUNT = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("Header ANCOUNT: " + String.format("%x", dnsHeader.ANCOUNT));

        //Get NSCOUNT
        bytes = inputStream.readNBytes(2);
        dnsHeader.NSCOUNT = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("Header NSCOUNT: " + String.format("%x", dnsHeader.NSCOUNT));

        //Get ARCOUNT
        bytes = inputStream.readNBytes(2);
        dnsHeader.ARCOUNT = (short)((bytes[0] << 8) | (bytes[1] & 0xFF));
//        System.out.println("Header ARCOUNT: " + String.format("%x", dnsHeader.ARCOUNT));

        return dnsHeader;
    }


    //This will create the header for the response. It will copy some fields from the request
    static DNSHeader buildResponseHeader(DNSMessage request, DNSMessage response){
        DNSHeader responseHeader = new DNSHeader();
        responseHeader.ID = request.dnsHeader.ID;                           //This identifier is copied to the corresponding reply
        responseHeader.QR = true;                                           //Query (0) or response (1)
        responseHeader.OPCODE = request.dnsHeader.OPCODE;                   //standard query: 0 | an inverse query: 1 | server status request: 2
        responseHeader.AA = request.dnsHeader.AA;                           //Authoritative Answer
        responseHeader.TC = request.dnsHeader.TC;                           //Truncation
        responseHeader.RD = request.dnsHeader.RD;                           //Recursion Desired
        responseHeader.RA = request.dnsHeader.RA;                           //Recursion Available
        responseHeader.Z = request.dnsHeader.Z;                             //Reserved
        responseHeader.AD = request.dnsHeader.AD;                           //Authentic Data
        responseHeader.CD = request.dnsHeader.CD;                           //Checking Disabled
        responseHeader.RCODE = request.dnsHeader.RCODE;                     //Response code: No error(0), Format error(1), Server failure(2), Name Error(3), Not Implemented(4), Refused(5)
        responseHeader.QDCOUNT = (short)response.dnsQuestions.size();       //Number of entries in the question section.
        responseHeader.ANCOUNT = (short)response.dnsAnswers.size();         //Number of resource records in the answer section
        responseHeader.NSCOUNT = (short)response.dnsAuthorityRecords.size();//Number of name server resource records in the authority records section.
        responseHeader.ARCOUNT = (short)response.dnsAdditionalRecords.size();//Number of resource records in the additional records section.

        return responseHeader;
    }


    //Encode the header to bytes to be sent back to the client. The OutputStream interface has methods to write a
    //single byte or an array of bytes.
    void writeBytes(OutputStream outputStream) throws IOException {

        //Write the ID
        outputStream.write(DNSMessage.shortToBytes(ID));

        //Byte 3
        byte thirdByte = 0;
        if (QR) { thirdByte = (byte)(thirdByte | 0x80); }
        thirdByte = (byte) (thirdByte | (OPCODE & 0x78));
        if (AA) { thirdByte = (byte)(thirdByte | 0x04); }
        if (TC) { thirdByte = (byte)(thirdByte | 0x02); }
        if (RD) { thirdByte = (byte)(thirdByte | 0x01); }
        outputStream.write(thirdByte);

        //Byte 4
        byte fourthByte = 0;
        if (RA) { fourthByte = (byte)(fourthByte | 0x80); }
        if (Z) { fourthByte = (byte)(fourthByte | 0x40); }
        if (AD) { fourthByte = (byte)(fourthByte | 0x20); }
        if (CD) { fourthByte = (byte)(fourthByte | 0x10); }
        fourthByte = (byte) (fourthByte | (RCODE & 0x0F));
        outputStream.write(fourthByte);

        //Write the last four shorts
        outputStream.write(DNSMessage.shortToBytes(QDCOUNT));
        outputStream.write(DNSMessage.shortToBytes(ANCOUNT));
        outputStream.write(DNSMessage.shortToBytes(NSCOUNT));
        outputStream.write(DNSMessage.shortToBytes(ARCOUNT));

    }

    //IDE generated toString method. Useful for debugging
    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID=" + ID +
                ", QR=" + QR +
                ", OPCODE=" + OPCODE +
                ", AA=" + AA +
                ", TC=" + TC +
                ", RD=" + RD +
                ", RA=" + RA +
                ", Z=" + Z +
                ", AD=" + AD +
                ", CD=" + CD +
                ", RCODE=" + RCODE +
                ", QDCOUNT=" + QDCOUNT +
                ", ANCOUNT=" + ANCOUNT +
                ", NSCOUNT=" + NSCOUNT +
                ", ARCOUNT=" + ARCOUNT +
                '}';

    }


    //Getters
    public short getQDCOUNT() {
        return QDCOUNT;
    }

    public short getANCOUNT() {
        return ANCOUNT;
    }

    public short getNSCOUNT() {
        return NSCOUNT;
    }

    public short getARCOUNT() {
        return ARCOUNT;
    }

}
