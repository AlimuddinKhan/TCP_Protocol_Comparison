/**
 * This acts as the listener of packets and ACKs for the routers
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class StopNWaitAckHandler
        implements Runnable{
    private TCP tcp;
    private Ack ack;

    /**
     * This is the parameterized constructor
     * @param tcp   TCP object
     * @param ack   Ack received
     */
    public StopNWaitAckHandler(TCP tcp, Ack ack) {
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

        // checking if we have the destination in our list
        if (tcp.getOutGoingConnections().containsKey(ack.source.getAddress())){

            cw = tcp.getOutGoingConnections().get(ack.getSource().getAddress());

            // make sure you are the only on editing the congestion window
            synchronized (cw){

                // update the ACK status
                cw.updateAckStatus(ack.getAckID() - 1, true);

                // slide the window
                cw.slideWindow();

                // update cwnd contents
                cw.updateCongestionWindow();

            }

        }else{
            // destination not present
        }

    }
}


