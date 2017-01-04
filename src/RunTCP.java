import java.util.Scanner;

/**
 * This class initializes different TCP protocols
 *
 * @author Alimuddin Khan (aak5031@rit.edu)
 * @author Nisha Bhanushali (nnb7791@rit.edu)
 */
public class RunTCP {
    public static void main(String[] args) {

        // check if we have got the proper number of arguments
        if ((args.length - 1) % 5 != 0){
            usage();
            return;
        }


        System.out.println("ALERT(RunTCP): Starting TCP protocol");

        // cerate a TCP protocol
        TCPProtocol tcpProtocol = new TCPProtocol();

        // parse and validate the input
        int numberOfDestinations = Integer.parseInt(args[0]);
        if(numberOfDestinations > 0) {
            for (int i = 0; i < numberOfDestinations; i++) {
                // get the destination IP
                String destinationIP = args[5 * i + 1];

                // get the number of packets to be sent
                int packets = Integer.parseInt(args[5 * i + 2]);

                // get the initial sequence of the packets to be sent
                int startSequence = Integer.parseInt(args[5 * i + 3]);
                tcpProtocol.getTcp().addPacketsInQueue(destinationIP, 55556, packets, startSequence);

                // get the TCP version
                String tcpVersion = args[5 * i + 4];
                tcpProtocol.getTcp().setTcpVersion(tcpVersion);

                // get the IP of the router
                String routerAddress = args[5 * i + 5];
                tcpProtocol.getTcp().getRouter().setAddress(routerAddress);
            }

            Thread tcpSnderThread = new Thread(tcpProtocol);
            long startTime = System.currentTimeMillis();
            System.out.printf("%5s%10s%10s%20s\n","t","CWND_size","ssthresh","status");
            tcpSnderThread.start();
            // wait while entire data is sent
            //System.out.println("ALERT(RunTCP): Waiting for all packets to be sent");

            while (!tcpProtocol.hasCompleted()) {
                // keep waiting
            }

            System.out.println("ALERT(RunTcp): Took " + (System.currentTimeMillis() - startTime) + " milliseconds to send" +
                    " all packets");

            // stop the running TCP socket and the listener
            tcpProtocol.stopTcp();

        }else {

        // interactive TCP protocols which helps in doing interactive tasks

        Scanner scanner = new Scanner(System.in);
        String commandString = "";
        String[] commandArray;
        String command;


        String destination;
        int noOfpackets;
        int initialSequence;


        while (!commandString.matches("quit") ){
            commandString = scanner.nextLine();
            commandArray = commandString.split(" ");
            command = commandArray[0];
            switch (command){
                case "quit":
                    tcpProtocol.stopTcp();
                    break;
                case "add":
                    System.out.println("Adding packets!!");
                    destination = commandArray[1];
                    noOfpackets = Integer.parseInt(commandArray[2]);
                    System.out.println("called : send " + destination + " " + noOfpackets);
                    System.out.println("Will add here!!!");
                    initialSequence = Integer.parseInt(commandArray[3]);
                    // default listening port we are using is 55556
                    // let initial sequence be 100
                    System.out.println("Adding " + noOfpackets + " in the queue");
                    tcpProtocol.getTcp().generatePackets(destination, 55556,initialSequence, noOfpackets);
                    tcpProtocol.printTCPQueuePackets();
                    tcpProtocol.printCWNDPackets();

                    break;
                    //tcpProtocol.getTcp().generatePackets();

                case "addn":

                    // please change names here
                    System.out.println("Adding new  packets!!");
                    String dstAddress = commandArray[1];
                    int n =  Integer.parseInt(commandArray[2]);
                    int is = Integer.parseInt(commandArray[3]);

                    // call new addition method
                    tcpProtocol.getTcp().addPacketsInQueue(dstAddress, 55556, n, is);
                    break;

                case "send":
                    int number = Integer.parseInt(commandArray[1]);
                    tcpProtocol.forwardPacketToRouter(number);
                    break;

                case "me":
                    tcpProtocol.getTcp().printMyDetails();
                    break;

                case "printic":
                    System.out.println("Printing incoming connections.....");
                    tcpProtocol.getTcp().printIncomingConnections();
                    break;

                case "printoc":
                    System.out.println("Printing outgoing connections");
                    tcpProtocol.getTcp().printOutGoingConnection();
                    break;

                // updates the congestion window of a destination
                case "uc":
                    destination = commandArray[1];
                    System.out.println("ALERT(RunTCP): Updating condestion window of destination " + destination);
                    tcpProtocol.getTcp().updateCWNDElements(destination);
                    break;

                // updates cwnd window size of a destination
                case "ucs":
                    destination = commandArray[1];
                    int size = Integer.parseInt(commandArray[2]);
                    System.out.println("ALERT(RunTCP): Updating size of destination " + destination + " to size of " + size);
                    tcpProtocol.getTcp().updateCWNDSize(destination, size);
                    break;

                case "su":
                    destination =commandArray[1];
                    System.out.println("ALERT(RunTCP): Sending unsent packets in CWND of " + destination );
                    tcpProtocol.sendUnsetPakcetsInCWND(destination);
                    break;
                case "slide":
                    destination =commandArray[1];
                    System.out.println("ALERT(RunTCP): Sliding window of destination of " + destination );
                    tcpProtocol.slideCongestionWindow(destination);

                    break;

                case "hto":
                    destination =commandArray[1];
                    System.out.println("ALERT(RunTCP): Checking TO for destination " + destination);
                    tcpProtocol.hasTimeOut(destination);
                    break;

                case "rc":
                    destination =commandArray[1];
                    System.out.println("ALERT(RunTCP): Resetting connection for destination " + destination);
                    tcpProtocol.resetConnection(destination);
                    break;

                case "sto":
                    destination =commandArray[1];
                    System.out.println("ALERT(RunTCP): Resetting connection for destination " + destination);
                    tcpProtocol.resetConnection(destination);
                    break;

                case "hc":
                    destination  =commandArray[1];
                    System.out.println("ALERT(RunTCP): Seeting if all sent or not for destination " + destination);
                    System.out.println("ALERT(RunTCP):" + tcpProtocol.hasCompleted(destination));
                    break;

                default:
                    System.out.println("Please type a valid command!!!");
                    break;
            }

        }

        // stop the tcp protocol
        tcpProtocol.stopTcp();

        scanner.close();
        }
    }


    /**
     * This method prints the proper usage of this program
     */
    public static void usage(){
        System.out.println("This program can be used int 2 modes!\n" +
                "1. Sender Mode : \n" +
                "RunTCP <nd> [<destIP> <np> <is> <tcp-type> <routerTP>] [....]\n" +
                "where;" +
                "<nd> : number of destinations to send the packets\n" +
                "next five parameters will be for each destination separately" +
                "<destIP> : destination IP\n" +
                "<np> : number of packets to send\n" +
                "<is> : initial sequence for those packets\n" +
                "<routerIP> : virtual router through which you want to send those packets\n" +
                "2. Receiver Modee:\n" +
                "java RunTCP 0");

    }
}
