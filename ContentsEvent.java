/* Spawned by transmitting content state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class ContentsEvent extends RoutedDataEvent {
    public ContentsEvent(EthernetSimulator simulator, Node source, Node dest, double currentTime, boolean start) {
        super(simulator, source, dest, currentTime, start);
    }

    @Override
    public void process() {
        super.process();

    }

    @Override
    public String toString() {
        return "T" + scheduledTime + ", " + source.getName() + " -> " + dest.getName() + ": Received the " + (start ? "beginning" : "end") + " of packet contents.";
    }
}
