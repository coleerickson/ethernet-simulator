/* Spawned by busy receiver state.

Sent to self.

Triggers interpacket gap state.
*/

public class ReceiverIdleEvent extends EthernetEvent {
    public ReceiverIdleEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
