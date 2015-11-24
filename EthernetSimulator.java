import java.util.*;

public class EthernetSimulator {
	public static void main(String[] args) {
		PriorityQueue<EthernetEvent> eventQueue = new PriorityQueue<>();
		List<Node> nodes = new ArrayList<>();

		Node a = new Node("A"), b = new Node("B");
		nodes.add(a); 
		nodes.add(b);

		eventQueue.add(new EthernetEvent(a, b, 2));
		eventQueue.add(new EthernetEvent(a, b, 1));
		eventQueue.add(new EthernetEvent(a, b, 3));

		double time = 0;
		EthernetEvent event = eventQueue.poll();
		while (event != null) {

			time = event.scheduledTime;
			System.out.println(event);
			event.process();

			event = eventQueue.poll();
		}
		
		System.out.println("Done.");
	}      	      
}	
