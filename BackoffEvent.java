/* Sent by node in backoff state to itself.

   Possible result states:
   Backoff again
   Eager
   Sending preamble
 */

public class BackoffEvent extends EthernetEvent {
    public BackoffEvent(EthernetSimulator simulator, Node source, double scheduledTime) {
        super(simulator, source, scheduledTime);
    }

    @Override
    public void process() {
        assert source.transmitter == Node.TransmitterState.WAITING_FOR_BACKOFF;
        source.transmitter = Node.TransmitterState.PREPARING_NEXT_PACKET;
        simulator.add(new PacketReadyEvent(simulator, source, scheduledTime));
    }

    @Override
    public String toString() {
        return source.getName() + " at " + scheduledTime + ": placeholder backoff event.";
    }
}