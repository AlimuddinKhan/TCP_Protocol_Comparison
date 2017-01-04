import java.io.Serializable;

/**
 * This represents the data unit in TCP connection
 * This unit can be a normal data packet or an
 * ACk for the packet.
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class DataUnit
        implements Serializable {
    // source
    public Host source;

    // destination
    public Host destination;

    // flag to indicate sent or not
    public boolean sent;

    // variable to store timer for right now it would time steps and not exact time :)
    public long sendingTime;

    /**
     * Default constructor
     */
    public DataUnit() {
        this.sent = false;
    }


    /**
     * This is parameterized constructor to initialize the Data Unit object
     * @param sourceAddress         Source IP address
     * @param sourcePort            Source port in which it is listening for incoming packets
     * @param destinationAddress    Destination IP address
     * @param destinationPort       Destination port on which it is listening for incoming packets
     */
    public DataUnit(String sourceAddress,
                    int sourcePort,
                    String destinationAddress,
                    int destinationPort) {
        this.source = new Host(sourceAddress, sourcePort);
        this.destination = new Host(destinationAddress, destinationPort);
        this.sent = false;
    }


    /**
     * This is parameterized constructor to initialze the Data Unit object
     * @param me                    Host object characterizing running TCP server
     * @param destinationAddress    Destination IP address
     * @param destinationPort       Destination port on which it is listening for incoming packets
     */
    public DataUnit(Host me, String destinationAddress,
                    int destinationPort){
        this.source = me;
        this.destination = new Host(destinationAddress, destinationPort);
        this.sent = false;
    }

    /**
     * This is parameterized constructor to initialize the Data Unit object
     * @param source   Host object characterizing source TCP server
     * @param destination   Host object characterizing destination TCP server
     */
    public DataUnit(Host source, Host destination) {
        this.source = source;
        this.destination = destination;
        this.sent = false;
    }


    /**
     * String representation of the Data Unit object
     * @return
     */
    @Override
    public String toString() {
        return "DataUnit{" +
                "source=" + source +
                ", destination=" + destination +
                ", sent=" + sent +
                ", sendingTime=" + sendingTime +
                '}';
    }


    /**
     * Getter for sent flag
     * @return  sent flag
     */
    public boolean isSent() {
        return sent;
    }


    /**
     * Settet for sent flag
     * @param sent  sent flag
     */
    public void setSent(boolean sent) {
        this.sent = sent;
    }


    /**
     * Getter for sent time
     * @return  sent time
     */
    public long getSendingTime() {
        return sendingTime;
    }


    /**
     * Setter for sending time
     * @param sendingTime   sending time
     */
    public void setSendingTime(long sendingTime) {
        this.sendingTime = sendingTime;
    }


    /**
     * Gets the source Host
     * @return      Source Host object
     */
    public Host getSource() {
        return source;
    }


    /**
     * Sets the source Host object
     * @param source    Source Host object
     */
    public void setSource(Host source) {
        this.source = source;
    }


    /**
     * Gets the destination Host object
     * @return      destination Host object
     */
    public Host getDestination() {
        return destination;
    }


    /**
     * Sets the destination Host object
     * @param destination
     */
    public void setDestination(Host destination) {
        this.destination = destination;
    }
}
