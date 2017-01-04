import java.io.Serializable;

/**
 * This program acts as the acknowledgement for our
 * TCP protocol comparison program
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 */
public class Ack
        extends DataUnit
        implements Serializable {
    // uniquely identifies each ACK
    private int ackID;

    // this keeps track of how many dupAcks we have received
    private int dupAckCount;

    /**
     * This is the default constructor to initialize ACK object with default values
     */
    public Ack() {
    }


    /**
     * This is parameterized constructor to initialze the ACK object
     * @param ackID ACK ID
     */
    public Ack(int ackID) {
        this.ackID = ackID;
        this.dupAckCount = 0;
    }


    /**
     * This is parameterized constructor to initialze the ACK object
     * @param ackID                 ACK id
     * @param sourceAddress         Source IP address
     * @param sourcePort            Source port in which it is listening for incoming packets
     * @param destinationAddress    Destination IP address
     * @param destinationPort       Destination port on which it is listening for incoming packets
     */
    public Ack(int ackID,
               String sourceAddress,
               int sourcePort,
               String destinationAddress,
               int destinationPort) {

        super(sourceAddress, sourcePort, destinationAddress, destinationPort);
        this.ackID = ackID;
        this.dupAckCount = 0;
    }


    /**
     * This is parameterized constructor to initialze the ACK object
     * @param ackID                 Ack ID
     * @param me                    Host object characterizing running TCP server
     * @param destinationAddress    Destination IP address
     * @param destinationPort       Destination port on which it is listening for incoming packets
     */
    public Ack(int ackID, Host me,
               String destinationAddress,
               int destinationPort) {
        super(me,destinationAddress, destinationPort);
        this.ackID = ackID;
        this.dupAckCount = 0;
    }


    /**
     * This is parameterized constructor to initialze the ACK object
     * @param ackID Ack ID
     * @param src   Host object characterizing source TCP server
     * @param dst   Host object characterizing destination TCP server
     */
    public Ack(int ackID, Host src, Host dst){
        super(src, dst);
        this.ackID = ackID;
        this.dupAckCount = 0;
    }


    /**
     * This method returns string representation of the ACK object
     * @return
     */
    @Override
    public String toString() {
        return "Ack{" + ackID +
                ", '" + this.getDestination().getAddress() + '\'' +
                ", " + this.getDestination().getPort() + "\'" +
                ", " + this.dupAckCount +  " }";
    }


    /**
     * This is the getter for ACK id
     * @return  ack id
     */
    public int getAckID() {
        return ackID;
    }

    /**
     * This is the setter for ACK id
     * @param ackID ack id
     */
    public void setAckID(int ackID) {
        this.ackID = ackID;
    }


    /**
     * This is the getter for dup ack count in a  given ACK
     * @return
     */
    public int getDupAckCount() {
        return dupAckCount;
    }


    /**
     * This is the setter for the dupAck count
     * @param dupAckCount
     */
    public void setDupAckCount(int dupAckCount) {
        this.dupAckCount = dupAckCount;
    }
}
