/**
 * This class acts as the Tahoe ack handler which handles the ACk as per the
 * Tahoe TCP protocol
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class TahoeAckHandler
        implements Runnable{
    private TCP tcp;
    private Ack ack;

    /**
     * This is the parameterized constructor
     * @param tcp   TCP object
     * @param ack   Ack received
     */
    public TahoeAckHandler(TCP tcp, Ack ack) {
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
        //System.out.println("ALERT(Reno Ack Handler): Handling ack " + ack);

        // set the packet as acked


        CongestionWindow cw;
        // checking if we have the destination in our list
        if (tcp.getOutGoingConnections().containsKey(ack.source.getAddress())){

            cw = tcp.getOutGoingConnections().get(ack.getSource().getAddress());

            // make sure you are the only on editing the congestion window
            synchronized (cw){

                // check no of dupAckCounts
                if(ack.getDupAckCount() == 0){

                    // increase cwnd
                    cw.increaseCWNDsize();

                }else{
                    if(ack.getDupAckCount() < 3){
                        // do nothing
                    }else if(ack.getDupAckCount() == 3){

                        // decrease the ss thresh
                        cw.setSsthresh((int)cw.getCwndSize()/2);

                        // retransmit the lost packet
                        cw.retransmitPacket(ack.getAckID());

                        // reset the connection and set CWND size to 1
                        cw.resetTahoeConnection();

                    }
                }

                // update the ACK status for the recived packet
                cw.updateAckStatus(ack.getAckID() - 1, true);

                // slide the window
                cw.slideWindow();

                // update cwnd contents. It involes removing acjed packets and adding extra packets to be sent
                cw.updateCongestionWindow();

            }

        }else{
            // we have received from the unknown source
        }

    }
}

