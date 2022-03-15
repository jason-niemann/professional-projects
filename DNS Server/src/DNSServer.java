import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

//This class opens up a UDP socket (DatagramSocket class in Java), and listen for requests. When it gets one,
//it looks at all the questions in the request. If there is a valid answer in cache, it adds that to the response,
//otherwise it creates another UDP socket to forward the request to Google (8.8.8.8) and then await their response.
//Once it's dealt with all the questions, it sends the response back to the client.
//Note: dig sends an additional record in the "additionalRecord" fields with a type of 41.
public class DNSServer {

    //Member variables to setup server/socket and store data
    private byte[] buffer = new byte[512]; //512 is max size of packet
    private DatagramSocket socket = null;
    private int receivingPort = 8053;
    private int googlePort = 53;
    private int senderPort;
    private InetAddress senderIP;
    private InetAddress googleAddress = InetAddress.getByName("8.8.8.8");
    private boolean running = true;
    private DatagramPacket responsePacket;

    //Constructor
    DNSServer() throws IOException {
        //Initialize receiver socket at specified port
        socket = new DatagramSocket(receivingPort);

    }

    //Run the server
    public void run() throws IOException {

        //Run indefinitely
        while (running) {

            //Create a initialPacket to hold data of specified size
            DatagramPacket initialPacket = new DatagramPacket(buffer, buffer.length);

            //Put data received into the initialPacket from the socket
            socket.receive(initialPacket);

            //Save the sender's IP and port
            senderIP = initialPacket.getAddress();
            senderPort = initialPacket.getPort();

            //Send byte array to DNSMessage class for decoding, getData returns byte[]
            DNSMessage initialMessage = DNSMessage.decodeMessage(initialPacket.getData());

            //Debugging printout
//            System.out.println(initialMessage);

////////////////////////////////////////////////SENDING///////////////////////////////////////////////////////////////

            //Array of answers for each question in request, boolean to determine if all are found
            ArrayList<DNSRecord> answers = new ArrayList<>();
            boolean allAnswersInCache = true;

            //Check DNS Cache for each answer record. All or nothing. All answers found in cache or ask Google for all answers.
            for (int i = 0; i < initialMessage.dnsHeader.getQDCOUNT(); i++) {

                //Check Cache
                if (DNSCache.isInCache(initialMessage.dnsQuestions.get(i))) {

                    //Add answer to array
                    answers.add(DNSCache.getRecord(initialMessage.dnsQuestions.get(i)));

                } else {
                    //If an answer was not found, mark false
                    allAnswersInCache = false;
                }

            }

            //Respond to request from Cache, else ask Google and forward
            if (allAnswersInCache) {

                //Build response DNSMessage object
                DNSMessage response = DNSMessage.buildResponse(initialMessage, answers);

                //Convert response to bytes
                byte[] responseInBytes = response.toBytes();

                //Create datagram packet to send to user
                responsePacket = new DatagramPacket(responseInBytes, responseInBytes.length, senderIP, senderPort);

            } else {
                //Forward packet to google
                DatagramPacket askGooglePacket = new DatagramPacket(buffer, buffer.length, googleAddress, googlePort);

                //Send the packet
                socket.send(askGooglePacket);

                //Create new packet for the Google response
                DatagramPacket googleResponsePacket = new DatagramPacket(buffer, buffer.length);

                //Put data received into the googleResponsePacket from the socket
                socket.receive(googleResponsePacket);

                //Send byte array to DNSMessage class for decoding
                DNSMessage googleResponseMessage = DNSMessage.decodeMessage(googleResponsePacket.getData());

                //Debugging print out
//                System.out.println(googleResponseMessage);

                //For each answer
                for (int i = 0; i < googleResponseMessage.dnsAnswers.size(); i++) {

                    //Add answer to cache and check that add was successful
                    if (DNSCache.addRecord(googleResponseMessage.dnsQuestions.get(i), googleResponseMessage.dnsAnswers.get(i))) {

                        //Print confirmation
                        System.out.println("Added to cache: " + googleResponseMessage.dnsQuestions.get(i).QNAME);

                    } else {

                        //Print failure
                        System.out.println("Failed to add: " + googleResponseMessage.dnsQuestions.get(i).QNAME);
                    }

                }

                //Build the response packet from the Google response
                responsePacket = new DatagramPacket(googleResponsePacket.getData(), googleResponsePacket.getData().length, senderIP, senderPort);

            }

            //Send the response packet to complete the request
            socket.send(responsePacket);

        }

        //Close the socket when the server stops (currently never stops)
        socket.close();
    }

}
