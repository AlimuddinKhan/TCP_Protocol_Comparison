/**
 * This class provides the functionality for Ack handler in TCP Reno
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class RenoAckHandler
        implements Runnable{
    // current TCP
    private TCP tcp;

    //Received ACK object
    private Ack ack;

    /**
     * This is the parametrized constructor
     * @param tcp   TCP object
     * @param ack   Ack received
     */
    public RenoAckHandler(TCP tcp, Ack ack) {
        this.tcp = tcp;
        this.ack = ack;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        CongestionWindow cw;

        // checking if we have the destination in our outgping connections list
        if (tcp.getOutGoingConnections().containsKey(ack.source.getAddress())){

            // Get the congestion window details for the given connection
            cw = tcp.getOutGoingConnections().get(ack.getSource().getAddress());

            // make sure you are the only one editing the congestion window
            // There can be many ACK handlers working at the same time
            synchronized (cw){

                // check no of dupAckCounts
                if(ack.getDupAckCount() == 0){

                    // if no of dupAck counts are 0 then we may be in slow start mode or just came out of fast recovery mode
                    if(cw.getFlowControlStatus().matches("fastrecovery")){

                        // drop the ssthresh to CWND/2
                        cw.setSsthresh(Math.max(2,(int)cw.getCwndSize()/2));

                        // drop the cwnd to CWND/2
                        cw.setCwndSize(cw.getCwndSize()/2);

                        // update the status to slow start again
                        // will change it to congestionAvoidance after wards
                        cw.setFlowControlStatus("slowstart");

                        // get the first packet which has not yet received the ACK
                        Packet topPakcet = (Packet)cw.getCwndInPackets().get(0);

                        // move the window depending on the current ACK id and Top Packet sequence number difference
                        cw.moveWindow(ack.getAckID() - topPakcet.getSequenceNumber());

                    }

                    // increase cwnd depending on number of ACKS received
                    cw.increaseCWNDsize();

                    // decide what the status should be depending on cwnd size
                    // I think this part can be removed :)
                    if(cw.getCwndSize() >= cw.getSsthresh() &&
                            !cw.getFlowControlStatus().matches("fastrecovery")){
                        cw.setFlowControlStatus("congestionAvoidance");
                    }else{
                        cw.setFlowControlStatus("slowstart");
                    }

                }else{
                    // If dupAck counts are less than 3 and greater than 0 then do nothing
                    if(ack.getDupAckCount() < 3){
                        // do nothing

                    }else if(ack.getDupAckCount() == 3){
                        // enter fast recovery
                        cw.setFlowControlStatus("fastrecovery");

                        // ssthresh = cwnd / 2
                        cw.setSsthresh((int)cw.getCwndSize()/2);

                        // CWND = CWND+3
                        cw.setCwndSize(cw.getCwndSize()/2 + 3);

                        // retransmit the packet
                        cw.retransmitPacket(ack.getAckID());

                    }else{

                        // we are already in fast recovery mode and just increase the window size for each dupACK > 3
                        cw.increaseCWNDsize();
                    }
                }

                // Update the ACK status of the Packet with "sequence ID = ACK ID - 1"
                cw.updateAckStatus(ack.getAckID() - 1, true);

                // slide the window -> remove ACKed packets and add new packets in the available space
                cw.slideWindow();

                // update cwnd contents
                cw.updateCongestionWindow();

            }

        }else{
            // We have recieved ACK from a connection which is not currently in our outgoing connection list
        }

    }
}
