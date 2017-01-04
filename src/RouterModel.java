import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * This program acts as the  router model
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class RouterModel {
    private Queue<DataUnit> packetsQueue;

    // deciding queue size
    private int queueLength;

    //Random loss generatio
    private long seed;

    // Random generator
    private Random prng;

    // loss percentage
    private double lossPercentage;


    /**
     * Default constructor
     */
    public RouterModel() {
        packetsQueue = new LinkedList<>();

        // we can change this queue length anytime
        this.queueLength = 1000;
    }

    /**
     * This is a parameterized constructor and is used to set up router configuration
     * @param queueLength
     * @param seed
     * @param lossPercentage
     */
    public RouterModel(int queueLength, long seed, double lossPercentage) {
        this.queueLength = queueLength;
        this.seed = seed;
        this.prng = new Random(this.seed);
        this.lossPercentage = lossPercentage;
        this.packetsQueue = new LinkedList<>();
    }


    /**
     * This method randomly decides to drop the packet
     * @return
     */
    public boolean randomDrop(){
        double prob = this.prng.nextDouble()*100;
        //System.out.println("Got " + prob);
        if(prob <= lossPercentage) return true;
        else return false;
    }

    // getters and setters for various fiedls in the router
    public Queue<DataUnit> getPacketsQueue() {
        return packetsQueue;
    }

    public void setPacketsQueue(Queue<DataUnit> packetsQueue) {
        this.packetsQueue = packetsQueue;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public Random getPrng() {
        return prng;
    }

    public void setPrng(Random prng) {
        this.prng = prng;
    }

    public double getLossPercentage() {
        return lossPercentage;
    }

    public void setLossPercentage(double lossPercentage) {
        this.lossPercentage = lossPercentage;
    }


}
