import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This class acts as the TCP listener
 * This listens for the packets as well as the acknowledgements.
 * If packet is received then passed it to packet handler and
 * if ACK is received then passes it to specific Ack handler
 * depending in the running TCP version
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class TcpListener
        extends MyListener
        implements Runnable {

    // TCP object
    private TCP tcp;


    /**
     * Parametrized constructor
     * @param tcp   TCP object
     */
    public TcpListener(TCP tcp){
        super();
        this.tcp = tcp;
    }


    /**
     * This method acts as the TCP listener thread
     */
    @Override
    public void run() {
        byte[] receivedByteArray;
        DatagramPacket packet;

        // keep listening till we get the listening flag ti be false
        while (isKeepListening()){
            receivedByteArray = new byte[10240];
            packet = new DatagramPacket(receivedByteArray, receivedByteArray.length);
            try {
                this.getSocket().receive(packet);
            } catch (IOException e) {
                // if listener thread is not started properly then simply stop the socket and the listener thread
                this.stopListener();
                break;
            }

            // convert the received byte array into the Data Unit object
            DataUnit dataUnit = this.convertByteArrayToObject(receivedByteArray);

            // make sure we have got something
            if(dataUnit != null){

                // If it is a packet then transfer to the Packet Handler
                if(dataUnit.getClass().getName().matches("Packet")){
                    new Thread(new TCPPacketHandler(tcp, (Packet)dataUnit, this.getSocket())).start();

                    // If it is an ACK then pass it to the ACK handler
                }else if(dataUnit.getClass().getName().matches("Ack")){

                    // pass to the respctive running tcp  versio ACK handler
                    if(this.tcp.getTcpVersion().matches("goback")){

                        // handle as per classical TCP
                        new Thread(new TCPClassicalAckhandler(tcp, (Ack) dataUnit)).start();

                    }else if (this.tcp.getTcpVersion().matches("tahoe")){

                        // handle as per Tahoe
                        new Thread(new TahoeAckHandler(tcp, (Ack) dataUnit)).start();
                    }else if (this.tcp.getTcpVersion().matches("reno")){

                        // handle as per TCP reno
                        new Thread(new RenoAckHandler(tcp, (Ack) dataUnit)).start();
                    }else if (this.tcp.getTcpVersion().matches("stopnwait")){

                        // handle as per TCP reno
                        new Thread(new StopNWaitAckHandler(tcp, (Ack) dataUnit)).start();
                    }

                }

            }

        }
    }

}
