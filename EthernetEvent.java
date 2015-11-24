public class EthernetEvent implements Comparable<EthernetEvent> {

	Node source, dest;
	double scheduledTime;

    public EthernetEvent(Node source, Node dest, double scheduledTime) {
		this.source = source;
		this.dest = dest;
		this.scheduledTime = scheduledTime;
    }

    public void process() {
        System.out.println("Processed test event.");
    }


    public String toString() {
		return "T" + scheduledTime + ", " + source.getName() + " -> " + dest.getName() + ": Base event.";
    }

	public int compareTo(EthernetEvent e) {
		return this.scheduledTime > e.scheduledTime ? 1 : -1;
	}
}
