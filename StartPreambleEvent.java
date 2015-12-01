/* Spawned by transmitting packet preamble state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class StartPreambleEvent extends EthernetEvent {
    public StartPreambleEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
