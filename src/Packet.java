/**
 * This class Acts as the unit packet for our TCP
 * protocol comparison program
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class Packet extends DataUnit {
    public int sequenceNumber;



    // this says if the packet has been acknowledged or not
    public boolean acknowledged;

    // SIN indicates that a new session has started
    public boolean SIN;

    // keep track in which RTT packet was sent
    public int sendRTTNumber;

    public Packet() {
    }

    public Packet(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.acknowledged = false;
        this.SIN = false;
    }

    public Packet(int sequenceNumber,
                  String sourceAddress,
                  int sourcePort,
                  String destinationAddress,
                  int destinationPort) {
        super(sourceAddress, sourcePort, destinationAddress, destinationPort);
        this.sequenceNumber = sequenceNumber;
        this.acknowledged = false;
        this.SIN = false;
    }

    public Packet(int sequenceNumber,
                  Host me,
                  String destinationAddress,
                  int destinationPort) {
        super(me,destinationAddress, destinationPort);
        this.sequenceNumber = sequenceNumber;
        this.acknowledged = false;
        this.SIN = false;
    }

    public Packet(int sequenceNumber, Host source, Host destination) {
        this.sequenceNumber = sequenceNumber;
        this.source = source;
        this.destination = destination;
        this.sent = false;
        this.SIN = false;
    }


    @Override
    public String toString() {
        return "Packet{" + sequenceNumber +
                ",'" + this.getDestination().getAddress() + '\'' +
                "," + this.getDestination().getPort() + ", SENT?" + this.sent +
                ", ACK?" + this.acknowledged + ", SIN?" + this.SIN +
                '}';
        //return "Packet{" + sequenceNumber + "}";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public boolean isSIN() {
        return SIN;
    }

    public void setSIN(boolean SIN) {
        this.SIN = SIN;
    }

    public int getSendRTTNumber() {
        return sendRTTNumber;
    }

    public void setSendRTTNumber(int sendRTTNumber) {
        this.sendRTTNumber = sendRTTNumber;
    }
}
