/* Spawned by transmitting content state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class ContentsEvent extends RoutedDataEvent {

    private boolean failed;

    public ContentsEvent(EthernetSimulator simulator, Node source, Node dest, double currentTime, boolean start) {
        super(simulator, source, dest, currentTime, start);

        failed = false;
    }

    @Override
    public void process() {
        super.process();

        // if we successfully sent the packet contents, then we are done with the packet and move back to preparing a new packet.
        if (source == dest && !start && !isFailed()) {
            assert source.transmitter == Node.TransmitterState.TRANSMITTING_CONTENTS;
            source.transmitter = Node.TransmitterState.PREPARING_NEXT_PACKET;
            simulator.add(new PacketReadyEvent(simulator, source, super.scheduledTime));
        }
    }

    public void fail() { failed = true; }
    public boolean isFailed() { return failed; }

    @Override
    public String toString() {
        if (source == dest) {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": " + (start ? "Started" : "Finished")
                    + " transmitting packet contents.";
        } else {
            return dest.getName() + " (source: " + source.getName() + ") at " + scheduledTime
                    + ": Received the " + (start ? "beginning" : "end")
                    + " of a packet's contents.";
        }
    }
}