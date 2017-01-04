import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This method acts as the parent listener for Source, destination
 * and Router listener. We are listening on by default port 55556 port
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class MyListener {

    // socket for listening
    private DatagramSocket socket;

    // listening port
    private int listeningPort;

    // flag which decides when to stop listening
    private boolean keepListening;


    /**
     * Default constructor to initialize the socket
     */
    public MyListener() {
        this.listeningPort = 55556;
        this.keepListening = true;
        try {
            System.out.println("ALERT(MyListener): Starting socket for  listener.......");
            this.socket = new DatagramSocket(this.listeningPort);
            System.out.println("SUCCESS(MyListener): successfully started listener socket  on "
                    + this.listeningPort + " port");
        } catch (SocketException e) {
            System.out.println("ERROR(MyListener): unable to start  listener on port " + this.listeningPort);
        }
    }


    /**
     * Getter for data gram socket
     * @return
     */
    public DatagramSocket getSocket() {
        return socket;
    }


    /**
     * Setter for Datagram socket
     * @param socket
     */
    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }


    /**
     * Getter for listening port
     * @return
     */
    public int getListeningPort() {
        return listeningPort;
    }


    /**
     * Setter for listening port
     * @param listeningPort Listening port number
     */
    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }


    /**
     * Getter for listening flag
     * @return
     */
    public boolean isKeepListening() {
        return keepListening;
    }

    /**
     * Setter for listening flag
     * @param keepListening
     */
    public void setKeepListening(boolean keepListening) {
        this.keepListening = keepListening;
    }


    /**
     * This method stops the thread by closing the socket and the listener thread
     */
    public void stopListener(){
        try {
            this.socket.close();
        }catch(NullPointerException e){
            // socket wasn't started properly and we tried ti stop a non-started socket
        }

        // set the flag to flase to stop the thread
        this.setKeepListening(false);
    }

    /**
     * This method converts the byte array into object DataUnit
     * @param bytes a byte[] representation of the DataUnit
     * @return returns the DataUnit(packet or ack for the packet) object
     */
    public  DataUnit convertByteArrayToObject(byte[] bytes){
        DataUnit object = new DataUnit();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput oin = null;
        try{
            oin = new ObjectInputStream(bis);
            object = (DataUnit) oin.readObject();

        }catch (IOException e){
        }catch (ClassNotFoundException e){
        }catch (Exception e){
        }
        finally {
            try {
                oin.close();
            } catch (IOException e) {
            }
        }
        return object;
    }

}
