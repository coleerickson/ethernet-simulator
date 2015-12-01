/* Spawned by transmitting content state.

Sent to all nodes.

Triggers preparing next packet transmitter state.
Triggers interpacket gap receiver state.
*/

public class EndContentsEvent extends EthernetEvent {
    public EndContentsEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
