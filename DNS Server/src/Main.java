import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {

    //Create a server instance and start the server
    DNSServer DNSServer = new DNSServer();
    DNSServer.run();

    //Once the server is running, use dig commands, and Wireshark to test functionality:
    //dig example.com @127.0.0.1 -p 8053 +tries=1

    }
}
