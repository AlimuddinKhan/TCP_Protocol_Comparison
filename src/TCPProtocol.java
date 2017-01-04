import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * This class has all the utility functions for the TCP Protocol
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class TCPProtocol
        implements Runnable{
    private TCP tcp;
    private TcpListener tcpListener;
    private DatagramSocket senderSocket;

    // you can change sending port as per your application
    private int sendingPort;

    // flag to decide when to stop sending the data
    private boolean keepSending;

    // define auto auto packet send update interval in millisecond
    private int updateInterval;

    public TCPProtocol()
    {
        // can be taken as the processing delay
        this.updateInterval = 500;
        this.keepSending = true;
        this.tcp = new TCP();
        this.sendingPort = 55557;

        // start the socket
        try {
            this.senderSocket = new DatagramSocket(this.sendingPort);
        } catch (SocketException e) {
            // port may be in use by some other process
        }

        // start the listener
        this.tcpListener = new TcpListener(this.tcp);
        Thread listenerThread = new Thread(this.tcpListener);
        listenerThread.start();

    }


    /**
     * This method stops the TCP socket
     */
    public void stopTcp(){
        try {
            this.senderSocket.close();
        } catch (NullPointerException e){
            // may be socket was not initialized
        }

        // stop the TCP listener thread and socket
        this.tcpListener.stopListener();

        //stop the tcp sender by setteing the flag to false
        this.keepSending = false;
    }

    /**
     * This method converts the RouterModel object into byte array
     * Which will be useful in sendig the object as packet
     * @param object Packet object to be converted into byte array
     * @return byte[] format of the object RouterModel
     */
    public  byte[] convertObjectToByteArray(Packet object){
        byte[] myObjectByteArray = new byte[10240];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput output= null;
        try{
            output = new ObjectOutputStream(bos);
            output.writeObject(object);
            output.flush();
            myObjectByteArray = bos.toByteArray();

        }catch(Exception e){
            // corrupt object
        }
        return myObjectByteArray;
    }


    /**
     * This method acts as the TCP liste
     */
    @Override
    public void run() {
        double t = 0;
        while (isKeepSending()){

            // Send in the packets which are not sent
            CongestionWindow cw;
            //int ssthreash;
            for (String destination: this.getTcp().getOutGoingConnections().keySet()){

                // get the congestion window for that connection
                cw = this.getTcp().getOutGoingConnections().get(destination);

                // print the status
                //ssthreash = (cw.getSsthresh() < Integer.MAX_VALUE)?cw.getSsthresh():-1;
                //System.out.printf("%5.2f%10.2f%10d%20s\n",t,cw.getCwndSize(),ssthreash,cw.getFlowControlStatus());
                System.out.printf("%5.2f%10.2f%10d%20s\n",t,cw.getCwndSize(),cw.getSsthresh(),
                        cw.getFlowControlStatus());
                t += updateInterval/1000.0;


                synchronized (cw) {
                    // update the congestion window

                    //
                    cw.updateCongestionWindow();
                    //cw.updateVegasCongestionWindow();

                    // send unsent packets in the congestion window
                    this.sendUnsetPakcetsInCWND(destination);

                    // check if TO has occured for something
                    if (cw.hasTimeOut()) {

                        // reset the connection when TO occurs
                        cw.resetTahoeConnection();
                    }
                }
            }
            try {

                // sleep for the upgrade interval
                Thread.sleep(this.getUpdateInterval());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * This mehtod send the packet to th given destination on port 55556
     * We ca make it general and in future we can send it to desired destination
     * @param packet
     * @param destinationIP
     */
    public void sendPacket(Packet packet, String destinationIP){
        //System.out.println("ALERT: sending Data unit : " + packet + " to " + destinationIP);
        try {
            // here destination is just the router
            InetAddress destination = InetAddress.getByName(destinationIP);
            //InetAddress destination = InetAddress.getByName(packet.getDestination().getAddress());
            byte[] routerByteArray = this.convertObjectToByteArray(packet);
            int destinationport = 55556;
            DatagramPacket myPacket = new DatagramPacket(routerByteArray,
                    routerByteArray.length, destination, destinationport);

            // sending the packet to destination
            this.senderSocket.send(myPacket);

        } catch (UnknownHostException e) {
//            System.out.println("ERROR: No destination called " + packet.getDestination().getAddress() + " found");

        } catch (IOException e) {
//            System.out.println("ERROR: Sender socket was not properly initialized");
        }
    }

    /**
     * This metho send the packet to the desired destination
     * in the packet object itself
     * @param packet
     */
    public void sendPacket(Packet packet){
//        System.out.println("ALERT: sending Data unit : " + packet + " to "
//                + packet.getDestination().getAddress());
        try {
            // here destination is just the router
            InetAddress destination = InetAddress.getByName(packet.getDestination().getAddress());
            //InetAddress destination = InetAddress.getByName(packet.getDestination().getAddress());
            byte[] routerByteArray = this.convertObjectToByteArray(packet);
            int destinationport = 55556;
            DatagramPacket myPacket = new DatagramPacket(routerByteArray,
                    routerByteArray.length, destination, destinationport);

            // sending the packet to destination
            this.senderSocket.send(myPacket);

        } catch (UnknownHostException e) {
//            System.out.println("ERROR: No destination called " + packet.getDestination().getAddress() + " found");

        } catch (IOException e) {
//            System.out.println("ERROR: Sender socket was not properly initialized");
        }
    }

    /**
     * This method removes one packet at a time form the sender queue and forwards it to the router
     * @param n
     */
    public void forwardPacketToRouter(int n){
        Packet packet;
//        System.out.println("Forwarding to router : " + this.getTcp().getRouter().getAddress());
        for (int i = 0;i < n && !this.getTcp().getPacketsToBeSent().isEmpty(); i++){
            packet = this.getTcp().getPacketsToBeSent().poll();
            this.sendPacket(packet, this.getTcp().getRouter().getAddress());
        }
    }


    /**
     * This method checks unsent packets in the CWND of the given destination and sends
     * to that destination
     * @param destinationIP
     */
    public void sendUnsetPakcetsInCWND(String destinationIP){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)){
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);
            int counter = 0;

            // check all the packets in the congestion window and send unset packets
            for(DataUnit packet: cw.getCwndInPackets()){
                packet = (Packet) packet;
                if(packet.sent == false
                        && counter < cw.getCwndSize()){
                    // update sent time
                    packet.setSendingTime(System.currentTimeMillis());
                    // update sent status
                    packet.setSent(true);


                    // will send the packet here :)
                    this.sendPacket((Packet) packet, this.getTcp().getRouter().getAddress());
                    counter++;
                }
            }
        }else{
            // OC not present
        }

    }


    /**
     * This method check whether TO has occurred or not
     * @param destinationIP
     * @return True or False depending on TO status
     */
    public boolean hasTimeOut(String destinationIP){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)){
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);
            if(cw.hasTimeOut()){
                return true;
            }
        }else{
            //OC not present
        }
        return false;
    }


    /**
     * This method resets the connection on TO for the specific destination
     * @param destinationIP
     */
    public void resetConnection(String destinationIP){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)){

            // get the CW for the given connection
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);

            // resetting the congestion window
            cw.resetConnection();

        }else{
            //OC not present
        }
    }


    /**
     * This method sets the time out of a destination to a specific value
     * @param destinationIP IP of the destination
     * @param timeOut Time out
     */
    public void setTimeOut(String destinationIP, int timeOut){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)) {
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);
            cw.setTimeOut(timeOut);
        } else{

            //OC not presnet
        }
    }


    /**
     * This method slides the window of a particular outgping connection :)
     * @param destinationIP
     */
    public  void slideCongestionWindow(String destinationIP){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)){
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);
            cw.slideWindow();
        }else{
            // OC not present
        }
    }

    /**
     * This method decides whether all the packets have been sent OR not
     * @return
     */
    public boolean hasCompleted(String destinationIP){
        if(this.getTcp().getOutGoingConnections().containsKey(destinationIP)){
            CongestionWindow cw = this.getTcp().getOutGoingConnections().get(destinationIP);
            return cw.getCwndInPackets().size() == 0 && cw.getPacketsTobeSent().size() == 0;
        }else{
            // OC not present
        }
        return false;
    }


    /**
     * This method check whether all the packets in all the destination hace been sent or not?
     * @return true if all have completed and false if any of the og connection hasn't yet sent
     */
    public boolean hasCompleted(){
        for(CongestionWindow cw : this.getTcp().getOutGoingConnections().values()){
            if(!cw.hasCompleted()){
                return false;
            }else{
                // OC connection not present
            }
        }
        return true;
    }


    /**
     * This method prints which packets are still in the queue and not in the cwnd
     * This is of no use please removae it ASAP
     * Wil be deleted
     */
    public void printTCPQueuePackets(){
        System.out.println("##### TCP Queue #####");
        for (Packet packet: this.getTcp().getPacketsToBeSent()){
            System.out.println(packet);
        }
    }


    // getter and setters for the fiedls

    public void printCWNDPackets(){
        this.getTcp().getCwnd().printCongestionWindowElements();
    }

    public TCP getTcp() {
        return tcp;
    }

    public void setTcp(TCP tcp) {
        this.tcp = tcp;
    }

    public TcpListener getTcpListener() {
        return tcpListener;
    }

    public void setTcpListener(TcpListener tcpListener) {
        this.tcpListener = tcpListener;
    }

    public DatagramSocket getSenderSocket() {
        return senderSocket;
    }

    public void setSenderSocket(DatagramSocket senderSocket) {
        this.senderSocket = senderSocket;
    }

    public int getSendingPort() {
        return sendingPort;
    }

    public void setSendingPort(int sendingPort) {
        this.sendingPort = sendingPort;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public boolean isKeepSending() {
        return keepSending;
    }

    public void setKeepSending(boolean keepSending) {
        this.keepSending = keepSending;
    }
}
