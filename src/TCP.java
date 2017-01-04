import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class provides features which are common in all the
 * TCP flavors and make the core of all TCP connections
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class TCP {
    // to identify yourself
    private Host me;

    // decide TCP version classical, tahoe, reno, vegas
    private String tcpVersion;

    // indicate the router
    private Host router;

    // to keep record of outgoing connections
    // here key is destination and value is last ack received
    private HashMap<String, CongestionWindow> outGoingConnections;

    // to keep record of incoming connections
    // here key is source and value is last ACK sent
    private HashMap<String, Ack> incomingConnections;

    //Queue of packets to be sent
    private Queue<Packet> packetsToBeSent;

    // features the congestion window
    private CongestionWindow cwnd;


    /**
     * Initializes the TCp protocol
     */
    public TCP() {
        String myAddress = null;
        try {
            myAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // it happens when the given destination address is not the valid one
        }
        // port in which we will be listening
        int listeningPort = 55556;

        // start ur self as the new Host object with local host address
        this.me = new Host(myAddress, listeningPort);

        // initialize all the components
        this.packetsToBeSent = new LinkedList<>();

        // initialize the cwnds
        this.cwnd = new CongestionWindow();

        // initialize the incoming and outdgoing connections HashMap
        this.incomingConnections = new HashMap<>();
        this.outGoingConnections = new HashMap<>();

        // setting nessie as router
        this.router = new Host("129.21.37.49", 55556);

    }

    /**
     * This is the parameterized construcotor
     * @param tcpVersion
     */
    public TCP(String tcpVersion) {
        String myAddress = null;
        try {
            myAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // it happens when the given destination address is not the valid one

        }
        // port in which we will be listening
        int listeningPort = 55556;

        // create a new host as urself wiht local host address
        this.me = new Host(myAddress, listeningPort);

        // initialize all the components
        this.packetsToBeSent = new LinkedList<>();
        this.cwnd = new CongestionWindow();
        this.incomingConnections = new HashMap<>();
        this.outGoingConnections = new HashMap<>();

        // decide TCP version
        this.tcpVersion = tcpVersion;

        // setting rhea.cs.rit.edu as router
        this.router = new Host("129.21.37.49", 55556);

    }

    /**
     * This method generates some random packets starting and adds them into the
     * queue of packets to be sent
     * @param destinationAddress address of the destination(can be a domain name or IP)
     * @param destinationPort    destination port
     * @param initialSequence    ISN of the connection
     * @param numberOfPackets    number of packets to generate
     */
    public void generatePackets(String destinationAddress,
                                int destinationPort,
                                int initialSequence,
                                int numberOfPackets){
        Packet packet;
        for (int i = 0; i < numberOfPackets; i++,initialSequence++){
             packet = new Packet(initialSequence, this.me, destinationAddress, destinationPort);
            // set the first packet as the SIN i.e. session initialization packet
            if(i == 0) packet.SIN = true;
            this.packetsToBeSent.add(packet);
        }
    }


    /**
     * This method adds packets in the queue of the specified outgoing connection
     * @param destinationAddress
     * @param destinationPort
     * @param numberOfPackets
     * @param initialSequence
     */
    public void addPacketsInQueue(String destinationAddress,
                                  int destinationPort,
                                  int numberOfPackets,
                                  int initialSequence){
        // check if there is already a key in the outgoing connection
        Host dst = new Host(destinationAddress, destinationPort);
        CongestionWindow cw;

        // decide which congestion window part
        if(this.getOutGoingConnections().containsKey(dst.getAddress())){
            cw = this.getOutGoingConnections().get(dst.getAddress());

        }else{
            // if it is not available then create a new one for that connection
             cw = new CongestionWindow();
            this.getOutGoingConnections().put(dst.getAddress(),cw);
        }

        // addition part
        Packet packet;
        for(int i = 0; i < numberOfPackets; i++){
            // create a new packet object
            packet = new Packet(initialSequence + i, this.me, dst);

            // adding packets to the queue in the given destination connection
            cw.addPacketToQueue(packet);
        }
    }

    /**
     * This method prints current servers hostname and IP
     */
    public void printMyDetails(){
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println(inetAddress.getHostName() + "(" + inetAddress.getHostAddress() + ")");
        } catch (UnknownHostException e) {
            System.out.println("ERROR(Router): unable to get local host address");
        }

    }

    /**
     * print incoming connection
     */
    public void printIncomingConnections(){
        System.out.println("#### Incoming connection ####");
        int i = 0;
        for (String sender: this.getIncomingConnections().keySet()){
            // will print incoming connections
            System.out.println(i  + " : Sender : " + sender);
            System.out.println("Last Ack : " + this.getIncomingConnections().get(sender));
            i++;
        }
    }

    /**
     * This method prints all the details of an outgoign connection
     */
    public void printOutGoingConnection(){
        System.out.println("#### Outgoing connection ####");
        System.out.println();
        CongestionWindow cw;
        for (String destination: this.outGoingConnections.keySet()){
            System.out.println("ALERT(TCP) : --Destination-- : " + destination);
            cw = this.outGoingConnections.get(destination);
            // print queue details
            System.out.println("ALERT(TCP): packets in queue");
            cw.printPacketsInTheQueue();
            System.out.println("ALERT(TCP): CWND packets");
            cw.printCongestionWindowElements();
            // print ss thresh
            System.out.println("SSThresh :" +cw.getSsthresh());
            System.out.println("CWND size : " + cw.getCwndSize());
            System.out.println("Sending mode : " + cw.getFlowControlStatus());
        }
        if(this.outGoingConnections.size() == 0){
            System.out.println("ALERT(TCP): Sorry you do not have any outgoing connections" );
        }
    }


    /**
     * This method updates the cwnd element for the specified destination
     * @param destinationIP
     */
    public void updateCWNDElements(String destinationIP){
        if(this.outGoingConnections.containsKey(destinationIP)){
            CongestionWindow cw = this.outGoingConnections.get(destinationIP);
            cw.updateCongestionWindow();
        }else{
            // destination not present in the o/g connection list
        }
    }


    /**
     * This method updates the cwnd size for the specified destination
     * @param destinationIP
     */
    public void updateCWNDSize(String destinationIP, int cwndSize){
        if(this.outGoingConnections.containsKey(destinationIP)){
            CongestionWindow cw = this.outGoingConnections.get(destinationIP);

            // set the new CWND size
            cw.setCwndSize(cwndSize);
        }else{
            // destination not present in the o/g connection list
        }
    }


    // getter and setter for the rest of the fields

    public Host getMe() {
        return me;
    }

    public void setMe(Host me) {
        this.me = me;
    }

    public HashMap<String, CongestionWindow> getOutGoingConnections() {
        return outGoingConnections;
    }

    public void setOutGoingConnections(HashMap<String, CongestionWindow> outGoingConnections) {
        this.outGoingConnections = outGoingConnections;
    }

    public HashMap<String, Ack> getIncomingConnections() {
        return incomingConnections;
    }

    public void setIncomingConnections(HashMap<String, Ack> incomingConnections) {
        this.incomingConnections = incomingConnections;
    }

    public CongestionWindow getCwnd() {
        return cwnd;
    }

    public void setCwnd(CongestionWindow cwnd) {
        this.cwnd = cwnd;
    }


    public Queue<Packet> getPacketsToBeSent() {
        return packetsToBeSent;
    }

    public void setPacketsToBeSent(Queue<Packet> packetsToBeSent) {
        this.packetsToBeSent = packetsToBeSent;
    }

    public Host getRouter() {
        return router;
    }

    public void setRouter(Host router) {
        this.router = router;
    }

    public String getTcpVersion() {
        return tcpVersion;
    }

    public void setTcpVersion(String tcpVersion) {
        this.tcpVersion = tcpVersion;
    }
}
