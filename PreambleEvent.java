/* Spawned by transmitting packet preamble state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class PreambleEvent extends RoutedDataEvent {
    // The duration of a preamble in bit times.
    public static final double BIT_TIME_DURATION = 64;

    public PreambleEvent(EthernetSimulator simulator, Node source, Node dest, double currentTime, boolean start) {
        super(simulator, source, dest, currentTime, start);
    }

    @Override
    public void process() {
        super.process();

        // If this is the node that sent the event, either send contents or jam due to collision
        if (source == dest) {
            assert source.transmitter == Node.TransmitterState.TRANSMITTING_PREAMBLE;
            if (!start) {
                if (source.receiver == Node.ReceiverState.BUSY) {
                    source.interruptTransmission(super.scheduledTime);
                } else {
                    source.broadcastPacketContentsEvents(super.scheduledTime);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (source == dest) {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": " + (start ? "Started" : "Finished")
                    + " transmitting preamble.";
        } else {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": Received the " + (start ? "beginning" : "end")
                    + " of a preamble.";
        }
    }
}
