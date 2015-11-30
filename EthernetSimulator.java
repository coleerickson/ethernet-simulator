import java.util.*;

public class EthernetSimulator {
	public static void main(String[] args) {
	    simulate(2, 120, 1536);
	}
	
	public static void simulate(int hosts, double duration, int packetSize) {
		PriorityQueue<EthernetEvent> eventQueue = new PriorityQueue<>();
		List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < hosts; ++i) {
            nodes.add(new Node("Host " + i, packetSize));
        }

		double time = 0;
		EthernetEvent event = eventQueue.poll();
		while (event != null) {

			time = event.scheduledTime;
			System.out.println(event);
			event.process();

			event = eventQueue.poll();
		}
		
		System.out.println("The overall utilization of the network was: "
		    + computeUtilization(nodes, time) + ".");	
		System.out.println("The standard deviation of the utilization across all hosts was: "
		    + computeNodeUtilizationStandardDeviation(nodes) + ".");	
		
				
		
		System.out.println("Done.");	
	}   
	
	public static double computeUtilization(List<Node> nodes, double time) {		
		double totalBytes = 0;
		for (Node node : nodes) {
		    totalBytes += node.getSuccessfulPackets() * node.getPacketSize();
		}
		double utilization = totalBytes / time;
		return utilization;	
	} 
	
	public static double computeNodeUtilizationStandardDeviation(List<Node> nodes) {
	   return 0; // TODO implement
	}	
}	
