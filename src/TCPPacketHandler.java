import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class provides the functionality for Paxcket Hadler
 * for all TCP versions as they behave the same for any Packet received
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class TCPPacketHandler implements Runnable {


    private TCP tcp;
    private Packet packet;
    private DatagramSocket socket;

    public TCPPacketHandler(TCP tcp, Packet packet, DatagramSocket socket) {
        this.tcp = tcp;
        this.packet = packet;
        this.socket = socket;
    }

    public TCP getTcp() {
        return tcp;
    }

    public void setTcp(TCP tcp) {
        this.tcp = tcp;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
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
     * This method Handles the received packet and sends the ACKs to the sender of
     * the packet depending on the current sequence ID of the packet
     * and the ACK id of the last ACK sent
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        synchronized (this.getTcp()) {

            // recieved packet
            Packet currentPacket = this.getPacket();

            // ack to be sent
            Ack ackToSend;

            // check if the incoming packet is coming from previous connections
            // Also check SIN flag in case we need to reset the connection
            if (this.tcp.getIncomingConnections().get(currentPacket.getSource().getAddress()) != null
                    && currentPacket.SIN == false) {

                // get what was the last ACK sent
                Ack lastAck = this.getTcp().getIncomingConnections().get(this.getPacket().getSource().getAddress());


                // got the packet which we were expecting i.e. lastACKID == current sequence ID
                if( lastAck.getAckID() == currentPacket.sequenceNumber){

                    // its a normal one with no dupAck issue
                    if(lastAck.getDupAckCount() == 0){
                        ackToSend = new Ack(this.getPacket().sequenceNumber + 1,
                                this.getTcp().getMe(),
                                currentPacket.getSource().getAddress(),
                                currentPacket.getSource().getPort());

                    }else{
                        // it is resent packet and we have already sent dupACK for it
                        ackToSend = new Ack(this.getPacket().sequenceNumber + 1 + lastAck.getDupAckCount(),
                                this.getTcp().getMe(),
                                currentPacket.getSource().getAddress(),
                                currentPacket.getSource().getPort());
                    }
                }

                // we did not get the packet which we were expecting. Just update the dupAck count
                else {

                    ackToSend = lastAck;
                    ackToSend.setDupAckCount(lastAck.getDupAckCount() + 1);
                    System.out.println("ALERT(Reno Packet Handler): Packet lost or delayed. Resending " + ackToSend );
                }

                // update the incoming connection hashmap with the new ACK to send
                this.tcp.getIncomingConnections().put(currentPacket.getSource().getAddress(),ackToSend);

            } else {

                // decide the ACK parameter to be sent
                ackToSend = new Ack(this.getPacket().sequenceNumber + 1,
                        this.getTcp().getMe(),
                        this.getPacket().getSource().getAddress(),
                        this.getPacket().getSource().getPort());

                // update the ;ast ACK sent for the coneciton
                this.getTcp().getIncomingConnections().put(this.getPacket().getSource().getAddress(), ackToSend);

            }

            System.out.println("Alert(Reno Packet Handler): Replying ACK : " + ackToSend);

            // send the ACK to the sender to the packet
            this.sendAck(ackToSend);

        }
    }


    /**
     * This method send the ack to the sender of the packet
     * @param ack
     */
    public void sendAck(Ack ack){
        try {

            // get the destination address from the ACK itself
            InetAddress destination = InetAddress.getByName(ack.destination.getAddress());

            // convert the ACK object into byte array
            byte[] routerByteArray = this.convertObjectToByteArray(ack);

            // listening port of the sender
            int destinationport = 55556;

            // design the packet
            DatagramPacket myPacket = new DatagramPacket(routerByteArray,
                    routerByteArray.length, destination, destinationport);

            // sending the packet to destination
            this.socket.send(myPacket);

        } catch (UnknownHostException e) {

            // in case destination was not a proper IPv4 hostname
        } catch (IOException e) {
            // in case socket was not started properly
        }
    }


    /**
     * This method converts the Ack object into byte array
     * Which will be useful in sendig the object as packet
     * @param object Ack object to be converted into byte array
     * @return byte[] format of the object Ack
     */
    public  byte[] convertObjectToByteArray(Ack object){
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

}

