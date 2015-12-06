/* Spawned by preparing next packet transmitter state.

Sent to all nodes.

Triggers eager transmitter state or sending preamble transmitter state.
*/

public class PacketReadyEvent extends EthernetEvent {
    // TODO determine these constants by analyzing the simulator's performance with only one node on the network
    private static final double DELAY_MEAN = 100, DELAY_STANDARD_DEVIATION = 0;

    public PacketReadyEvent(EthernetSimulator simulator, Node source, double currentTime) {
        super(simulator, source, currentTime + simulator.getRandom().nextGaussian() * DELAY_STANDARD_DEVIATION + DELAY_MEAN);
    }

    @Override
    public void process() {
        assert source.transmitter == Node.TransmitterState.PREPARING_NEXT_PACKET;

        source.transmitter = Node.TransmitterState.EAGER;
        // if possible, send the preamble now. otherwise, switch the node to its eager state so that it will send the packet later.
        if (source.receiver == Node.ReceiverState.IDLE) {
            source.broadcastPreambleEvents(super.scheduledTime);
        }

        // TODO on all End events, check if eager and receiver idle and then send preamble so that an EAGER transmitter will actually do something
    }

    @Override
    public String toString() {
        return source.getName() + " at " + scheduledTime + ": Packet ready. Switching node state to eager.";
    }
}
