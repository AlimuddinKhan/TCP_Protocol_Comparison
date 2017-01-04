import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This acts as the listener of packets and ACKs for the routers
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class RouterListener
        extends MyListener
        implements Runnable{

    // router model
    private RouterModel routerModel;

    public RouterListener(RouterModel routerModel) {
        super();
        this.routerModel = routerModel;
    }


    /**
     * This method acts as the router listener thread
     */
    @Override
    public void run() {
        byte[] receivedByteArray;
        DatagramPacket packet;

        // keep running till lister flag is set to false
        while (isKeepListening()){
            receivedByteArray = new byte[10240];
            packet = new DatagramPacket(receivedByteArray, receivedByteArray.length);
            try {

                // catch the data unit object
                this.getSocket().receive(packet);
            } catch (IOException e) {

                // stop the listener on this error
                this.stopListener();
                break;
            }

            // cobert the received byte array into the Data Unit object
            DataUnit dataUnit = (DataUnit) this.convertByteArrayToObject(receivedByteArray);
            if(dataUnit != null){

                if(routerModel.getPacketsQueue().size() > routerModel.getQueueLength()){
                    System.out.println("ALERT(Router Listener): Tail dropping packet " + dataUnit);

                    // randmly drop the paccket based on loss probabilty
                }else if(routerModel.randomDrop() == true){
                    System.out.println("ALERT(Router Listener): Random dropping packet " + dataUnit);

                }else{

                    // adding the packet to the queue for sending to destination
                    this.routerModel.getPacketsQueue().add(dataUnit);
                }

            }

        }
    }

}
