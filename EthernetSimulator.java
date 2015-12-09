import java.util.*;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;
import java.io.*;

public class EthernetSimulator {
    // Bit rate in bits per microsecond, bit time in microseconds
    public static final double BIT_RATE = 10.0E6 / 1.0E6,
                               BIT_TIME = 1.0 / BIT_RATE,
                               MAX_PROPAGATION_DELAY = 232 * BIT_TIME,
                               COLLECT_DATA_INTERVAL = 1.0E6; // Data collection interval is 1 second by default.

    // The amount of packet overhead in bits. We use 20 bytes of overhead instead of 24 because we do not include a CRC in our simulation.
    public static final int PACKET_OVERHEAD_BITS = 20 * 8;

    private PriorityQueue<EthernetEvent> eventQueue;
    private List<Node> nodes;
    private Layout layout;
    private Random random;
    private double time;
	private int packetSize;

    public EthernetSimulator(int hosts, int packetSize) {
		this.packetSize = packetSize;
        eventQueue = new PriorityQueue<>();
        nodes = new ArrayList<>();
        random = new Random(0L);
        layout = new Layout() {
            // simple implementation where all nodes are separated by a distance such that the bandwidth-delay product
            // is 1 kilobyte.
            // TODO: Change this. Use the topology from 3.5 in Boggs, Mogul, and Kent paper.
            public double getPropagationDelay(Node a, Node b) {
                if (a == b) {
                    return 0;
                } else {
                  // Here I make the assumption that signals travel at 2E8 m/s and that two neighboring repeaters
                  // are sepearated by 300 meters.
                  return 2 * DELAY_TO_REPEATER + 1.5 * Math.abs((a.getRepeater() - b.getRepeater()));
                }
            }
        };

        for (int i = 1; i <= hosts; ++i) {
            nodes.add(new Node(this, (i%4), i, packetSize));
        }

        time = 0;
    }

    /**
    * Adds an event to the event queue. The simulator is passed to EthernetEvents, allowing them to spawn new events.
    */
    public void add(EthernetEvent e) {
        eventQueue.add(e);
    }

    public double computeUtilization(List<Node> nodes, double time) {
        double totalBits = 0;
        for (Node node : nodes) {
            totalBits += node.successfulPackets * (node.getPacketSize() + PACKET_OVERHEAD_BITS);
        }
        double utilization = totalBits / time;
        return utilization;
    }

    public double computeNodeUtilizationStandardDeviation(List<Node> nodes) {
        return 0; // TODO implement
    }

    public void simulate(double duration) {
        System.out.println("Simulating " + duration + " microseconds of the network. One bit time is " + BIT_TIME + " microseconds.");

        double collectData = COLLECT_DATA_INTERVAL;
        double previousUtilization =  0; // Keeps track of network utilization during the last data collection period (defaults to 1 second)
                                         // helps with getting network utilization during current data collection period
        EthernetEvent event = eventQueue.poll();
        while (event != null) {
            assert event.scheduledTime >= time;
            // System.out.println("Time difference between events = " + (event.scheduledTime - time) / BIT_TIME + " bit times.");

            // for (Node node : nodes) { System.out.println("        " + node.getName() + ": " + node.ongoingTransmissions); }

            time = event.scheduledTime;
            if (time > duration) {
                break;
            }

            // This is to collect data at shorter intervals. Right now the interval is 1 second, it's default value.
            if(time > collectData){
              double currentUtilization = computeUtilization(nodes, COLLECT_DATA_INTERVAL);
              System.out.println("[****] Utilization of the network during the " + (collectData / COLLECT_DATA_INTERVAL) +
                " second was: " + (currentUtilization - previousUtilization) );
              previousUtilization = currentUtilization;
              collectData += COLLECT_DATA_INTERVAL;
            }

            if (!event.isCanceled()) {
                System.out.println(event);
                event.process();
            }

            event = eventQueue.poll();
        }

        double utilization = computeUtilization(nodes, time);
        double standardDeviation = computeNodeUtilizationStandardDeviation(nodes);

        // we will modify this to report data at shorter intervals throughout the execution
        System.out.println("The overall utilization of the network was: " + utilization);
        System.out.println("The standard deviation of the utilization across all hosts was: "
        + standardDeviation);

        //Writing data to file.
        Path file = Paths.get("utilization_data_" + packetSize + ".txt");
        String dataPoint = nodes.size() + " " + utilization + "\n";
        byte[] dataPointBytes = dataPoint.getBytes();

        try(OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE, APPEND))){
          out.write(dataPointBytes,0,dataPointBytes.length);
        } catch (IOException x){
          System.err.println(x);
        }

        System.out.println("Done.");
    }

    public double getTime() {
        return time;
    }

    public Random getRandom() {
        return random;
    }

    public Layout getLayout() {
        return layout;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    /**
    * Takes one argument: the number of microseconds that should be simulated.
    */
    public static void main(String[] args) {
        double duration = 10E6; //Defaults to 10 seconds
        int packetSize = 1536; //Defaults to 1536 bytes
        int numHosts = 5; //Defaults to 5 hosts

        if(args.length < 3 ){
          System.err.println("ERROR: Missing Args.\n");
        } else {
          try {
            packetSize = Integer.parseInt(args[0]);
            numHosts = Integer.parseInt(args[1]);
            duration = Double.parseDouble(args[2]);
          } catch(NumberFormatException x){
            System.err.println(x);
          }
        }

        new EthernetSimulator(numHosts, packetSize * 8).simulate(duration);
    }
}
