import java.io.Serializable;

/**
 * This class represent the Host Object. Which can be
 * a source, destination or Router
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class Host
        implements Serializable {

    // host name Or IP address
    private String address;

    // port number on which we will be listening
    private int port;


    /**
     * This is a parametrized constructor
     * @param address   IP address of host
     * @param port      Listening port of Host
     */
    public Host(String address, int port) {
        this.address = address;
        this.port = port;
    }


    /**
     * Default constructor
     */
    public Host() {
    }


    /**
     * This method returns String representation of Host object
     * @return  String representation of Host object
     */
    @Override
    public String toString() {
        return "Host{" +
                "address='" + address + '\'' +
                ", port=" + port +
                '}';
    }


    /**
     * Checks for equality of two Host objects
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        obj = (Host) obj;
        return (((Host) obj).getAddress().equals(this.address))
                && (this.port == ((Host) obj).port);
    }


    /**
     * Getter for host address
     * @return  host address
     */
    public String getAddress() {
        return address;
    }


    /**
     * Setter for Host object
     * @param address   host address
     */
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * Getter for host listening port
     * @return
     */
    public int getPort() {
        return port;
    }


    /**
     * Setter for Host listening port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


}
