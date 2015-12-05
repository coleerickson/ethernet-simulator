public abstract class EthernetEvent implements Comparable<EthernetEvent> {

	Node source, dest;
	double scheduledTime;
	EthernetSimulator simulator;

	private boolean canceled;

	public EthernetEvent(Node source, Node dest, double scheduledTime, EthernetSimulator simulator) {
		this.source = source;
		this.dest = dest;
		this.scheduledTime = scheduledTime;
		this.simulator = simulator;

		this.canceled = false;
	}

	public abstract void process();

	public abstract String toString();

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
