import java.util.*;

public class EthernetSimulator {
  private PriorityQueue<EthernetEvent> eventQueue;
  private List<Node> nodes;
  private Random random;
  private double time;

  public EthernetSimulator(int hosts, int packetSize) {
    eventQueue = new PriorityQueue<>();
    nodes = new ArrayList<>();
    random = new Random(0L);

    for (int i = 0; i < hosts; ++i) {
      nodes.add(new Node("Host " + i, packetSize, this));
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
    double totalBytes = 0;
    for (Node node : nodes) {
      totalBytes += node.getSuccessfulPackets() * node.getPacketSize();
    }
    double utilization = totalBytes / time;
    return utilization;
  }

  public double computeNodeUtilizationStandardDeviation(List<Node> nodes) {
    return 0; // TODO implement
  }

  public void simulate(double duration) {
    System.out.println("Simulating " + duration + " microseconds of the network.");

    EthernetEvent event = eventQueue.poll();
    while (event != null) {
      time = event.scheduledTime;
      if (time > duration) {
        break;
      }

      if (event.isCanceled()) {
        continue;
      }

      System.out.println(event);
      event.process();

      event = eventQueue.poll();
    }

    // we will modify this to report data at shorter intervals throughout the execution
    System.out.println("The overall utilization of the network was: " + computeUtilization(nodes, time));
    System.out.println("The standard deviation of the utilization across all hosts was: "
      + computeNodeUtilizationStandardDeviation(nodes));


    System.out.println("Done.");
  }

  public Random getRandom() {
    return random;
  }

  /**
   * Takes one argument: the number of microseconds that should be simulated.
   */
  public static void main(String[] args) {
    double duration;
    if (args.length == 0) {
      duration = 1E6; // by default, simulate the behavior of the network over 100 microseconds
    } else {
      try {
        duration = Double.parseDouble(args[0]);
      } catch (NumberFormatException e) {
        System.err.println("The first argument should be a double.");
        throw e;
      }
    }

    new EthernetSimulator(4000, 1536).simulate(duration);
  }
}
