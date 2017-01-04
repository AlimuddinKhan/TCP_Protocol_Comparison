import java.util.LinkedList;
import java.util.Queue;

/**
 * This class acts as the congestion window for the TC sender
 * Connection. This characterises the Congestion window size and ss-thresh
 * for a particular connection
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class CongestionWindow {

    // stores the congestion window size
    private double cwndSize;
    private int ssthresh;
    private Queue<Packet> packetsTobeSent;

    // flowControlStatus can be slow start, can be 1. slowstart OR 2. congestionAvoidance
    private String flowControlStatus;

    // stores the time out currently set to 10 seconds
    private long timeOut = 10000;

    // Stores list of pacckets in the congestion window
    private LinkedList<DataUnit> cwndInPackets;

    /**
     * Default constructor
     */
    public CongestionWindow() {
        this.cwndSize = 1;
        this.cwndInPackets = new LinkedList<>();
        this.ssthresh = 30;
        packetsTobeSent = new LinkedList<>();
        flowControlStatus = "slowstart";
    }


    /**
     * Parametrized constructor
     * @param cwndSize  Congestion Window size
     */
    public CongestionWindow(int cwndSize) {
        this.cwndSize = cwndSize;
        this.cwndInPackets = new LinkedList<>();
        this.ssthresh = 30;
        flowControlStatus = "slowstart";
    }


    /**
     * This method prints the Elements present in the congestion window
     * Right now we are just sedning cwndInPackets but can be generalized to
     * DataUnit so that it will print Acks as well :)
     */
    public void printCongestionWindowElements(){;
        for( DataUnit packet : this.cwndInPackets){
            System.out.println(packet);
        }
    }


    /**
     * This method adds a packet to queue of congestion window to be sent in next RTT
     * @param packet
     */
    public void addPacketToQueue(Packet packet){
        this.packetsTobeSent.add(packet);
    }


    /**
     * This method prints the packets currently in the queue and ready to be added in
     * the congestion window
     * @return
     */
    public void printPacketsInTheQueue(){
        System.out.println("#### Packets in Queue ####");
        for(Packet packet: this.packetsTobeSent){
            System.out.println(packet);
        }
    }


    /**
     * This method updates the congestion window by;
     * adding elements from the queue into the congestion window if there is any
     * space available in the congestion window.
     */
    public  synchronized void  updateCongestionWindow(){

        // check if we have space available in congestion window
        if(this.cwndSize > this.cwndInPackets.size()){

            // find out how packets you want to send
            int diff = (int)this.cwndSize - this.cwndInPackets.size();

            // mke sure you have available packets in the sending queue
            for (int  i = 0; i < diff  && !this.packetsTobeSent.isEmpty(); i++){
                DataUnit dataUnit = this.packetsTobeSent.poll();
                this.cwndInPackets.add(dataUnit);
            }

        }

        // update flow control status
        if(this.cwndSize > ssthresh &&
                !this.getFlowControlStatus().matches("fastrecovery")){
            this.setFlowControlStatus("congestionAvoidance");
        }else{
            if(this.flowControlStatus.matches("congestionAvoidance")){
                this.flowControlStatus = "slowstart";
            }
        }
    }



    /**
     * This method moves the window by a specific amount
     * @param slidingLenght amount by which we have to slide the window
     */
    public  void moveWindow(int slidingLenght){

        // if sliding length is 0 then simply return and don't do anything
        if(slidingLenght == 0){
            return;
        }

        // move the window by removing first element each time from the queue
        for(int i = 0; i < slidingLenght && !this.cwndInPackets.isEmpty(); i++){
            try {
                this.cwndInPackets.remove(0);
            }catch (IndexOutOfBoundsException e){
                // we tried to remove too many elements
            }
        }

    }


    /**
     * This method determines by how much amount we need to slide the window
     * It returns number of packets which have been acknowledged.
     * This number indicates the sliding length as these packets needs to be removed
     * @return  Sliding length
     */
    public int getTopAckNumberOfPackets(){
        int n = 0;

        for(DataUnit packet: this.cwndInPackets){
            packet = (Packet) packet;
            if(((Packet) packet).isAcknowledged() == true){
                n++;
            }else{
                break;
            }
        }

        return n;
    }


    /**
     * This method slides the window as per requirement
     */
    public void slideWindow(){
        this.moveWindow(this.getTopAckNumberOfPackets());
    }


    /**
     * This method increase the cwnd as per congestion avoidance stage
     */
    public void increaseCWNDsize(){

        // if we are in slow start ot fast recovery then simply increse exponentially
        if ((this.flowControlStatus.matches("slowstart") ||
                this.flowControlStatus.matches("fastrecovery")) ){
            this.cwndSize += 1;
        }else {
            // we are in congestion avoidance phase and increase CWND linearly
            if(this.cwndSize >= 1) {
                this.cwndSize  += (1.0 / (int) this.cwndSize);
                // if CWND goes below 1 make it one
            } else{
                this.cwndSize = 1;
            }
        }
    }


    /**
     * This method sets the sent flag of a Packet to false so that it can be resent
     * This packet will be retransmitted in the next RTT
     * @param sequenceNumber    Sequence number of the packet to be retransmitted
     */
    public void retransmitPacket(int sequenceNumber){
        for(DataUnit packet: this.cwndInPackets){
            packet = (Packet) packet;
            if(((Packet) packet).sequenceNumber == sequenceNumber ){
                packet.setSent(false);
                ((Packet) packet).setAcknowledged(false);
                break;
            }
        }
    }


    /**
     * This method retrasmits n packets starting from given sequence number
     * @param sequenceNumber
     * @param n
     */
    public void retransmitPackets(int sequenceNumber, int n){
        int i = 0;
        for(DataUnit packet: this.cwndInPackets){
            packet = (Packet) packet;
            if(((Packet) packet).sequenceNumber == sequenceNumber ){
                packet.setSent(false);
                ((Packet) packet).setAcknowledged(false);
                i++;
                sequenceNumber+=i;
                if(i >= n){
                    break;
                }
            }
        }
    }


    /**
     * This method sets the ACk status of a packet with given sequence number
     * @param sequneceNumber
     */
    public void updateAckStatus(int sequneceNumber, boolean flag){
        for(DataUnit packet: this.cwndInPackets){
            packet = (Packet) packet;
            if (((Packet) packet).sequenceNumber == sequneceNumber){
                ((Packet) packet).setAcknowledged(flag);
            }
        }
    }


    /**
     * This method resets the connection when the time out occurs
     */
    public void resetConnection(){

        // ###
        this.ssthresh = Math.max(2,(int)this.getCwndSize()/2);
        //this.ssthresh = 30;

        // setting the cwnd size to 1
        this.cwndSize = 1;

        Packet packet = (Packet) this.cwndInPackets.get(0);
        // indicate that a new connection has started to the receiver
        packet.SIN = true;

        // make all sent packets in the congestion window as unsent
        for (DataUnit dataUnit: this.cwndInPackets){
            dataUnit.sent = false;
        }
    }

    /**
     * This method resets the connection when the time out occurs.
     *
     */
    public void resetTahoeConnection(){
        // setting the cwnd size to 1
        this.ssthresh = Math.max(2,(int)this.getCwndSize()/2);
        this.cwndSize = 1;
        // I think I need to change the ss threash as well

        Packet packet = (Packet) this.cwndInPackets.get(0);
        // indicate that a new connection has started to the receiver
        packet.SIN = true;

        // make all sent packets in the congestion window as unsent
        for (DataUnit dataUnit: this.cwndInPackets){
            dataUnit.sent = false;
        }
    }

    /**
     * This method checks whether time out has occurred for any packet or not
     * @return True or false depending on timeout
     */
    public boolean hasTimeOut(){
        long currentTime = System.currentTimeMillis();
        long diff;

        // check all the packets currently in the CWND
        for(DataUnit packet: this.cwndInPackets){
            packet = (Packet) packet;
            diff = currentTime - packet.sendingTime;

            // check TO only for sent packets
            if( packet.sent == true && diff > this.timeOut ){
                return true;
            }

        }

        // if none of the packets have reache TO then return false
        return false;
    }


    /**
     * This method decides whether all the packets have been sent OR not
     * @return  True if all packets have been sent and false if not sent
     */
    public boolean hasCompleted(){
        return this.cwndInPackets.size() == 0 && this.packetsTobeSent.size() == 0;
    }

    /**
     * Getter for the packets queue to be sent
     * @return  packets queue to be sent
     */
    public Queue<Packet> getPacketsTobeSent() {
        return packetsTobeSent;
    }


    /**
     * Setter for the packets queue to be sent
     * @param packetsTobeSent   packets queue to be sent
     */
    public void setPacketsTobeSent(Queue<Packet> packetsTobeSent) {
        this.packetsTobeSent = packetsTobeSent;
    }


    /**
     * Getter for ssthresh
     * @return ssthresh
     */
    public int getSsthresh() {
        return ssthresh;
    }


    /**
     * Setter for ssthresh
     * @param ssthresh ssthresh
     */
    public void setSsthresh(int ssthresh) {
        this.ssthresh = ssthresh;
    }


    /**
     * Getter for cwnd size
     * @return  size of the congestion window
     */
    public double getCwndSize() {
        return cwndSize;
    }


    /**
     * Setter for cwnd
     * @param cwndSize      New size of the CWND
     */
    public void setCwndSize(double cwndSize) {
        this.cwndSize = cwndSize;
    }


    /**
     *Getter for queue of packets in the CWND
     * @return  queue of packets in the CWND
     */
    public LinkedList<DataUnit> getCwndInPackets() {
        return cwndInPackets;
    }


    /**
     * Setter for queue of packets to be added in the CWND
     * @param cwndInPackets queue of packets to be added in the CWND
     */
    public void setCwndInPackets(LinkedList<DataUnit> cwndInPackets) {
        this.cwndInPackets = cwndInPackets;
    }


    /**
     * Getter for flow control status
     * @return  flow control status
     */
    public String getFlowControlStatus() {
        return flowControlStatus;
    }


    /**
     * Setter for flow control status
     * @param flowControlStatus flow control status
     */
    public void setFlowControlStatus(String flowControlStatus) {
        this.flowControlStatus = flowControlStatus;
    }


    /**
     * Getter for time out
     * @return
     */
    public long getTimeOut() {
        return timeOut;
    }


    /**
     * Setter for the time out
     * @param timeOut
     */
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }


}
