/* Spawned by transmitting jamming state.

Sent to all nodes.

Triggers backoff transmitter state.
Triggers interpacket gap receiver state.
*/

public class EndJammingEvent extends EthernetEvent {
    public EndJammingEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
