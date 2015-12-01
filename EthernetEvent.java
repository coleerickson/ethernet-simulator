public abstract class EthernetEvent implements Comparable<EthernetEvent> {

	Node source, dest;
	double scheduledTime;
	
	private boolean canceled;

    public EthernetEvent(Node source, Node dest, double scheduledTime) {
		this.source = source;
		this.dest = dest;
		this.scheduledTime = scheduledTime;
		
		this.canceled = false;
    }

    public abstract void process();

    public String toString() {
		return "T" + scheduledTime + ", " + source.getName() + " -> " + dest.getName() + ": Base event.";
    }

	public final int compareTo(EthernetEvent e) {
		return this.scheduledTime > e.scheduledTime ? 1 : -1;
	}
	
	public void cancel() {
	    this.canceled = true;
	}
	
	public boolean isCanceled() {
	    return canceled;
	}
}
