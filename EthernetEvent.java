public abstract class EthernetEvent implements Comparable<EthernetEvent> {

	protected Node source;
	protected double scheduledTime;
	protected EthernetSimulator simulator;

	protected boolean canceled;

	protected EthernetEvent(EthernetSimulator simulator, Node source, double scheduledTime) {
		this.source = source;
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
