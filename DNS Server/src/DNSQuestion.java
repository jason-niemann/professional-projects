import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

//This class represents a client request.
public class DNSQuestion {

    //Member Variables
    ArrayList<String> QNAME;
    byte[] QType;
    byte[] QClass;


    //Read a question from the input stream. Due to compression, you may have to ask the DNSMessage containing
    //this question to read some of the fields.
    static DNSQuestion decodeQuestion(InputStream inputStream, DNSMessage dnsMessage) throws IOException {
        //Create new DNSQuestion Object
        DNSQuestion dnsQuestion = new DNSQuestion();
        dnsQuestion.QNAME = dnsMessage.readDomainName(inputStream);
        dnsQuestion.QType = inputStream.readNBytes(2);
        dnsQuestion.QClass = inputStream.readNBytes(2);

        return dnsQuestion;
    }


    //Write the question bytes which will be sent to the client. The hash map is used for us to compress the message,
    //See the DNSMessage class below.
    void writeBytes(ByteArrayOutputStream byteArrayOutputStream, HashMap<String,Integer> domainNameLocations) throws IOException {

        //Use writeDomainName method to write name (or compression scheme) to output stream
        DNSMessage.writeDomainName(byteArrayOutputStream, domainNameLocations, this.QNAME);

        //Write Qtype and Qclass to output stream
        byteArrayOutputStream.write(QType);
        byteArrayOutputStream.write(QClass);

    }


    //Let your IDE generate these. They're needed to use a question as a HashMap key, and to get a human-readable string.
    @Override
    public String toString() {
        return "DNSQuestion{" +
                "QType=" + Arrays.toString(QType) +
                ", QClass=" + Arrays.toString(QClass) +
                ", domainNames=" + QNAME +
                '}';
    }

    //These methods override default methods used in the hashmap, that they are used implicitly
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return Objects.equals(QNAME, that.QNAME) && Arrays.equals(QType, that.QType) && Arrays.equals(QClass, that.QClass);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(QNAME);
        result = 31 * result + Arrays.hashCode(QType);
        result = 31 * result + Arrays.hashCode(QClass);
        return result;
    }

}
