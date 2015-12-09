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
        this.packetSize = packetSize;
        layout = new Layout() {
            // simple implementation where all nodes are separated by a distance such that the bandwidth-delay product
            // is 1 kilobyte.
            // TODO: Change this. Use the topology from 3.5 in Boggs, Mogul, and Kent paper.
            public double getPropagationDelay(Node a, Node b) {
                if (a == b) {
                    return 0;
                } else {
                  double twoRepeaterDelay = 1E6 * 300 / 2E8;
                  // Here I make the assumption that signals travel at 2E8 m/s and that two neighboring repeaters
                  // are sepearated by 300 meters.
                  return 2 * DELAY_TO_REPEATER + twoRepeaterDelay * Math.abs((a.getRepeater() - b.getRepeater()));
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

    public double computeUtilization() {
        double totalBits = 0;
        for (Node node : nodes) {
            totalBits += node.getBitsSent();
        }
        double utilization = totalBits / time;
        return utilization;
    }

    public double computeStandardDeviationUtilization() {
        double meanUtilization = computeUtilization() / nodes.size();

        double squaredDeviations = 0;
        for (Node node : nodes) {
            double observationUtilization = node.getBitsSent() / time;
            double deviation = observationUtilization - meanUtilization;
            double squaredDeviation = deviation * deviation;
            squaredDeviations += squaredDeviation;
        }
        double variance = squaredDeviations / (double)nodes.size();
        double standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }

    public void simulate(double duration) {
        //System.out.println("Simulating " + duration + " microseconds of the network. One bit time is " + BIT_TIME + " microseconds.");

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


//            if(time > collectData){
//              double currentUtilization = computeUtilization(nodes, COLLECT_DATA_INTERVAL);
//              System.out.println("[****] Utilization of the network during the " + (collectData / COLLECT_DATA_INTERVAL) +
//                " second was: " + (currentUtilization - previousUtilization) );
//              previousUtilization = currentUtilization;
//              collectData += COLLECT_DATA_INTERVAL;
//            }


            if (!event.isCanceled()) {
                //System.out.println(event);
                event.process();
            }

            event = eventQueue.poll();
        }

        // we will modify this to report data at shorter intervals throughout the execution
        System.out.println("The overall utilization of the network was: " + computeUtilization());
        System.out.println("The standard deviation of the utilization across all hosts was: " + computeStandardDeviationUtilization());

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
        double duration = 20E6;
        int maxHosts = 30;
        List<Integer> packetSizes = new ArrayList<>();
        packetSizes.add(64);
        packetSizes.add(256);
        packetSizes.add(1536);

        try {
            PrintWriter out = new PrintWriter("utilization_data.txt");
            List<Thread> threads = new ArrayList<>();

            for (int packetSize : packetSizes) {
                for (int numHosts = 1; numHosts <= maxHosts; numHosts++) {
                    final int threadNumHosts = numHosts;
                    Thread t = new Thread(new Runnable() {

                        public void run() {
                            EthernetSimulator simulator = new EthernetSimulator(threadNumHosts, packetSize * 8);
                            simulator.simulate(duration);
                            out.println(threadNumHosts + "\t" + packetSize + "\t" + simulator.computeUtilization());
                        }
                    });
                    t.start();
                    threads.add(t);
                }
            }
            for (Thread t : threads) {
                t.join();
            }
            out.close();
        } catch (IOException x){
            System.err.println(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
