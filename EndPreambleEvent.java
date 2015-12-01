/* Spawned by transmitting packet preamble state.

Sent to all nodes.

Triggers jamming transmitter state or content-sending transmitter state.
Triggers interpacket gap receiver state.
*/

public class EndPreambleEvent extends EthernetEvent {
    public EndPreambleEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
