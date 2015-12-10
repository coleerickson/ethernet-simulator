/* Spawned by jamming transmitter state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class JammingEvent extends RoutedDataEvent {
    // The duration of a jamming signal in bit times.
    public static final int BIT_TIME_DURATION = 32;

    public JammingEvent(EthernetSimulator simulator, Node source, Node dest, double currentTime, boolean start) {
        super(simulator, source, dest, currentTime, start);
    }

    @Override
    public void process() {
        super.process();

        if (source == dest) {
            assert source.transmitter == Node.TransmitterState.JAMMING;

            // if this is the end of the jamming signal, then we move right into backoff unconditionally.
            if (!start) {
                // TODO do we need to drop packets after a certain number of attempts in idle sense?
                source.transmitter = Node.TransmitterState.WAITING_FOR_BACKOFF;
                simulator.add(new ContentionWindowEvent(simulator, source, scheduledTime));
            }
        }
    }

    @Override
    public String toString() {
        if (source == dest) {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": " + (start ? "Started" : "Finished")
                    + " jamming.";
        } else {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": Received the " + (start ? "beginning" : "end")
                    + " of a jamming signal.";
        }
    }
}
