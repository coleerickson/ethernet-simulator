import java.util.*;

public class EthernetSimulator {
    // Bit rate in bits per microsecond, bit time in microseconds
    public static final double BIT_RATE = 10.0E6 / 1.0E6,
                               BIT_TIME = 1.0 / BIT_RATE,
                               MAX_PROPAGATION_DELAY = 232 * BIT_TIME,
                               COLLECT_DATA_INTERVAL = 1.0E6; // Data collection interval is 1 second by default.

    public static final int PREAMBLE_SIZE = 64;

    private PriorityQueue<EthernetEvent> eventQueue;
    private List<Node> nodes;
    private Layout layout;
    private Random random;
    private double time;

    public EthernetSimulator(int hosts, int packetSize) {
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
                    return MAX_PROPAGATION_DELAY;
                }
            }
        };

        for (int i = 1; i <= hosts; ++i) {
            nodes.add(new Node(this, i, packetSize));
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
            totalBits += node.successfulPackets * node.getPacketSize() + node.preamblesSent * PREAMBLE_SIZE;
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

        // we will modify this to report data at shorter intervals throughout the execution
        System.out.println("The overall utilization of the network was: " + computeUtilization(nodes, time));
        System.out.println("The standard deviation of the utilization across all hosts was: "
        + computeNodeUtilizationStandardDeviation(nodes));


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
        double duration;
        if (args.length == 0) {
            duration = 10E6; // by default, simulate the behavior of the network over 10 seconds
        } else {
            try {
                duration = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("The first argument should be a double.");
                throw e;
            }
        }

        new EthernetSimulator(5, 1536 * 8).simulate(duration);
    }
}
