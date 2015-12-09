/* Spawned by preparing next packet transmitter state.

Sent to all nodes.

Triggers eager transmitter state or sending preamble transmitter state.
*/

public class PacketReadyEvent extends EthernetEvent {
    // TODO determine these constants by analyzing the simulator's performance with only one node on the network
    private static final double DELAY_MEAN = 50, DELAY_STANDARD_DEVIATION = 0.0005;

    public PacketReadyEvent(EthernetSimulator simulator, Node source, double currentTime) {
        super(simulator, source, currentTime + simulator.getRandom().nextGaussian() * DELAY_STANDARD_DEVIATION + DELAY_MEAN);

        // ensure that we do not end up with a time-traveling packet ready event
        if (scheduledTime < currentTime) {
            scheduledTime = currentTime;
        }
    }

    @Override
    public void process() {
        assert source.transmitter == Node.TransmitterState.PREPARING_NEXT_PACKET;

        // TODO this duplicates logic in backoff event and perhaps elsewhere. we should refactor into a Node method

        source.transmitter = Node.TransmitterState.EAGER;
        // if possible, send the preamble now. otherwise, switch the node to its eager state so that it will send the packet later.
        if (source.receiver == Node.ReceiverState.IDLE) {
            source.broadcastPreambleEvents(super.scheduledTime);
        }
    }

    @Override
    public String toString() {
        return source.getName() + " at " + scheduledTime + ": Packet ready. Switching node state to eager.";
    }
}
