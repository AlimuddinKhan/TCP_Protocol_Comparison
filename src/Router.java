import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

/**
 * This class acts as the Router in our program
 * It handles the packet
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */

public class Router implements Runnable{

    // router model
    private RouterModel routerModel;

    // packet listener
    private RouterListener routerListener;

    // socket to listen
    private DatagramSocket senderSocket;

    // sending port used dor sending the packets
    private int sendingPort;

    // update interval
    private int updateInterval;

    // transmission delay
    private int tranmissionDelay;

    // flag to decide stopping of the sneder thread
    private boolean keepSending;


    /**
     * Default constructor
     */
    public Router() {
        // currently we are not using it
        this.updateInterval = 10;
        this.tranmissionDelay = 10;
        this.routerModel = new RouterModel();
        this.sendingPort = 55557;
        this.keepSending = true;
        try {
            this.senderSocket = new DatagramSocket(this.sendingPort);
        } catch (SocketException e) {
            // socket might be in use by some other program
        }


        // create the listener thread
        this.routerListener = new RouterListener(this.routerModel);
        Thread listenerThread = new Thread(this.routerListener);

        // start the listener Thread
        listenerThread.start();
    }


    /**
     * This is a parametrized constructor used to initialize the router object
     * @param queueLength
     * @param seed
     * @param lossPercentage
     */
    public Router(int queueLength, long seed, double lossPercentage) {

        // this is the update interval for the sender thread
        this.updateInterval = 10;

        // this decides the transmission delay
        this.tranmissionDelay = 10;

        // initialize the model
        this.routerModel = new RouterModel(queueLength, seed, lossPercentage);

        // sending port
        this.sendingPort = 55557;

        // make flag true for listening
        this.keepSending = true;

        // start the sending socket
        try {
            this.senderSocket = new DatagramSocket(this.sendingPort);
        } catch (SocketException e) {
            // port might be in use by some other thread
        }

        // start the listener
        this.routerListener = new RouterListener(this.routerModel);
        Thread listenerThread = new Thread(this.routerListener);
        listenerThread.start();
    }


    /**
     * This acts as the sender thread
     * It checks for the packets in the queue and sends them to the destinations
     * It also has the random drop functionalities which allows it to drop packets
     * randomly
     */
    @Override
    public void run() {
        DataUnit packet;

        // keeo running till the keeplistening flag is true
        while (keepSending){

            // send all the packets in the queue which haven't been sent yet
            while (!this.routerModel.getPacketsQueue().isEmpty()){

                // remove one packet from the queue
                packet = this.routerModel.getPacketsQueue().poll();

                // send Packet
                sendPacket(packet);

                try {
                    // simulate the transmission delay
                    Thread.sleep(this.tranmissionDelay);
                } catch (InterruptedException e) {
                    // interrupted
                    break;
                }
            }

            try {
                // wait for the new packets to be added into the queue
                Thread.sleep(this.updateInterval);
            } catch (InterruptedException e) {
                // interrupted
                break;
            }
        }
    }

    /**
     * This method stops the router by safely
     * closing its sockets and all threads
     */
    public void stopRouter(){
        try {
            this.setKeepSending(false);
            this.senderSocket.close();
        } catch (NullPointerException e){
        }
        this.routerListener.stopListener();
    }

    /**
     * This method will be actually sending the packet to the destination
     * Before sending the packet it waits for the constant amount of time
     * Default transmission delay is set to 100ms
     *
     * This is going to be a simple method. Just wait and send and nothing else
     * @param packet Packet to be sent
     */
    public void sendPacket(DataUnit packet){
        try {
            InetAddress destination = InetAddress.getByName(packet.getDestination().getAddress());
            byte[] routerByteArray = this.convertObjectToByteArray(packet);
            int destinationport = 55556;
            DatagramPacket myPacket = new DatagramPacket(routerByteArray,
                    routerByteArray.length, destination, destinationport);

            // sending the packet to destination
            this.senderSocket.send(myPacket);

        } catch (UnknownHostException e) {
            // It occurs when the destination address is not the ideal one
        } catch (IOException e) {
            // it occurs when the socket was not initialized properly
        }
    }


    /**
     * This method pics n elements from the queue and sends them to the
     * destination
     * @param noOfPackets No of packets to be sent
     */
    public void sendToDestination(int noOfPackets){
        DataUnit dataUnit;
        for(int i = 0; i < noOfPackets && !this.routerModel.getPacketsQueue().isEmpty(); i++){
            dataUnit = this.routerModel.getPacketsQueue().poll();
            this.sendPacket(dataUnit);
        }
    }


    /**
     * This method is for manually sending a packet from the router to destination
     * Just fro testing purpose
     * @param sequenceID
     */
    public void sendSpecificPacket(int sequenceID){

        // champ
        Host src = new Host("129.21.22.194", 55556);

        // kraken
        Host dst = new Host("129.21.22.193", 55556);

        Packet packet = new Packet(sequenceID, src, dst);
        System.out.println("Sending packet : " + packet);
        this.sendPacket(packet);

    }

    /**
     * This method is for sending specific ack from the router to the source
     * Just for testing purpose
     * @param ackID
     * @param dupAckCount
     */
    public void sendSpecificAck(int ackID, int dupAckCount){
        Host src = new Host("129.21.22.193", 55556);
        Host dst = new Host("129.21.22.194", 55556);

        Ack ack = new Ack(ackID, src, dst);
        ack.setDupAckCount(dupAckCount);
        System.out.println("ALERT(Router) : Sending Ack : " + ack);
        this.sendPacket(ack);
    }

    public void addPacket(int sequenceID){
        // source is glados(129.21.22.196) 55556
        // destination is queeg(129.21.30.37)
        Host src = new Host("129.21.22.196", 55556);
        Host dst = new Host("129.21.30.37", 55556);
        Packet packet = new Packet(sequenceID, src, dst);
        System.out.println("Adding packet " + packet);
        this.routerModel.getPacketsQueue().add(packet);
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
     * This method converts the Packet object into byte array
     * Which will be useful in sendig the object as packet
     * @param object Packet object to be converted into byte array
     * @return byte[] format of the object RouterModel
     */
    public  byte[] convertObjectToByteArray(DataUnit object){
        byte[] myObjectByteArray = new byte[10240];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput output= null;
        try{
            output = new ObjectOutputStream(bos);
            output.writeObject(object);
            output.flush();
            myObjectByteArray = bos.toByteArray();

        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }
        return myObjectByteArray;
    }


    /**
     * This method prints Router queue contents
     */
    public void printQueueDataUnits(){
        System.out.println("#### Router Queue ####");
        for(DataUnit dataUnit: this.routerModel.getPacketsQueue()){
            System.out.println(dataUnit);
        }
    }


    // getters and setter for various element
    public RouterModel getRouterModel() {
        return routerModel;
    }

    public void setRouterModel(RouterModel routerModel) {
        this.routerModel = routerModel;
    }

    public RouterListener getRouterListener() {
        return routerListener;
    }

    public void setRouterListener(RouterListener routerListener) {
        this.routerListener = routerListener;
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

    public int getTranmissionDelay() {
        return tranmissionDelay;
    }

    public void setTranmissionDelay(int tranmissionDelay) {
        this.tranmissionDelay = tranmissionDelay;
    }

    public boolean isKeepSending() {
        return keepSending;
    }

    public void setKeepSending(boolean keepSending) {
        this.keepSending = keepSending;
    }


    /**
     * This is main method which helps in interacting with the sender and the receiver
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("ALERT(Router): Router Started");


        // parse the command line arguments
        int queueLength = Integer.parseInt(args[0]);
        long seed = Long.parseLong(args[1]);
        double packetLossPercentage = Double.parseDouble(args[2]);




        Scanner scanner = new Scanner(System.in);

        // initialize the router :)
        Router router = new Router(queueLength, seed, packetLossPercentage);



        System.out.println("Queue Length : " + router.routerModel.getQueueLength());
        System.out.println("Seed : " + router.routerModel.getSeed());
        System.out.println("Packet loss : " + router.routerModel.getLossPercentage() + "%");



        String commandString = "";
        String[] commandArray;
        String command;

        // start sender thread
        Thread routerSenderThread = new Thread(router);
        routerSenderThread.start();

        while (!commandString.matches("quit") ){
            commandString = scanner.nextLine();
            commandArray = commandString.split(" ");
            command = commandArray[0];
            switch (command){
                case "quit":
                    router.stopRouter();
                    break;

                case "send":
                    int numberOfPackets = Integer.parseInt(commandArray[1]);
                    System.out.println("Sending  " + numberOfPackets + " packets");
                    router.sendToDestination(numberOfPackets);
                    break;
                case "sendid":
                    int packetSequenceID = Integer.parseInt(commandArray[1]);
                    System.out.println("Sending  " + packetSequenceID + " packets");
                    router.sendSpecificPacket(packetSequenceID);
                    break;
                case "sendack":
                    int ackID = Integer.parseInt(commandArray[1]);
                    int dupAckCount = Integer.parseInt(commandArray[2]);
                    System.out.println("ALERT(Router) : sending ACk " + ackID );
                    router.sendSpecificAck(ackID, dupAckCount);

                case "print":
                    router.printQueueDataUnits();
                    break;
                case "me":
                    router.printMyDetails();
                    break;



                default:
                    System.out.println("Please type a valid command!!!");
                    break;
            }
        }

        routerSenderThread.interrupt();
        scanner.close();
    }
}
