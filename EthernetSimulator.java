import java.util.*;

public class EthernetSimulator {
    // Bit rate in bits per microsecond, bit time in microseconds
    public static final double BIT_RATE = 10.0E6 / 1.0E6,
                               BIT_TIME = 1.0 / BIT_RATE,
                               MAX_PROPAGATION_DELAY = 232 * BIT_TIME;

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
            // is 1 kilobyte
            public double getPropagationDelay(Node a, Node b) {
                if (a == b) {
                    return 0;
                } else {
                    return MAX_PROPAGATION_DELAY;
                }
            }
        };

        for (int i = 1; i <= hosts; ++i) {
            nodes.add(new Node(this, "Host " + i, packetSize));
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
            totalBits += node.successfulPackets * node.getPacketSize();
        }
        double utilization = totalBits / time;
        return utilization;
    }

    private double computeUtilizationForOneNode(Node node, double time) {
        double totalBits = 0;
        totalBits += node.successfulPackets * node.getPacketSize();
        double utilization = totalBits / time;
        return utilization;
    }

    private double SubtractSquareMean(double utilization, double mean) {
        return Math.pow(utilization - mean, 2)
    }

    public double computeNodeUtilizationStandardDeviation(List<Node> nodes, double time) {
        double totalBitRate = 0;
        double numberOfHosts = nodes.size();
        List<double> utilizations;
        for (Node node : nodes) {
            double utilization = computeUtilizationForOneNode(node, time);
            totalBitRate += utilization;
            utilizations.add(utilization)
        }
        double mean = totalBitRate / numberOfHosts;
        double totalOfSquares = 0;
        double meanOfSquares = 0;

        for (double utilizations: utilization) {
            totalOfSquares += SubtractSquareMean(utilization, mean);
        }

        meanOfSquares = Math.sqrt(totalOfSquares / numberOfHosts);

        return meanOfSquares;
    }

    public void simulate(double duration) {
        System.out.println("Simulating " + duration + " microseconds of the network. One bit time is " + BIT_TIME + " microseconds.");

        EthernetEvent event = eventQueue.poll();
        while (event != null) {
            assert event.scheduledTime >= time;
            // System.out.println("Time difference between events = " + (event.scheduledTime - time) / BIT_TIME + " bit times.");

            // for (Node node : nodes) { System.out.println("        " + node.getName() + ": " + node.ongoingTransmissions); }

            time = event.scheduledTime;
            if (time > duration) {
                break;
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
        + computeNodeUtilizationStandardDeviation(nodes, time));


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

        new EthernetSimulator(2, 1536 * 8).simulate(duration);
    }
}
