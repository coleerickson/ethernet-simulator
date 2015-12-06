/* Spawned by busy receiver state.

Sent to self.

Triggers interpacket gap state.
*/

public class ReceiverIdleEvent extends EthernetEvent {
    // The interpacket gap is 9.6 microseconds in duration.
    private static final double INTERPACKET_GAP = 9.6;

    public ReceiverIdleEvent(EthernetSimulator simulator, Node source, double currentTime) {
        super(simulator, source, currentTime + INTERPACKET_GAP);
    }

    @Override
    public void process() {
        assert source.receiver == Node.ReceiverState.WAITING_INTERPACKET;

        if (source.ongoingTransmissions > 0) {
            source.receiver = Node.ReceiverState.BUSY;
        } else {
            // TODO are there other places where the receiver can become genuinely idle and we need to check for an eager transmitter?
            source.receiver = Node.ReceiverState.IDLE;
            if (source.transmitter == Node.TransmitterState.EAGER) {
                source.broadcastPreambleEvents(super.scheduledTime);
            }
        }
    }

    @Override
    public String toString() {
        return source.getName() + " at " + scheduledTime + ": The interpacket gap has been completed. ";
    }
}