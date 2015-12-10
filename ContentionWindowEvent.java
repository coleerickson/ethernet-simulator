public class ContentionWindowEvent extends EthernetEvent {
    public ContentionWindowEvent(EthernetSimulator simulator, Node source, double currentTime) {
        super(simulator, source, currentTime + source.getBackoffSlots() * Node.SLOT_WAITING_TIME);
        assert source.transmitter == Node.TransmitterState.WAITING_FOR_BACKOFF;
    }

    @Override
    public void process() {
        assert source.transmitter == Node.TransmitterState.WAITING_FOR_BACKOFF;

        source.transmitter = Node.TransmitterState.EAGER;
        if (source.receiver == Node.ReceiverState.IDLE) {
            source.broadcastPreambleEvents(scheduledTime);
        }
    }

    // TODO implement
    @Override
    public String toString() {
        return source.getName() + " at " + scheduledTime + ": Finished backing off";
    }
}
