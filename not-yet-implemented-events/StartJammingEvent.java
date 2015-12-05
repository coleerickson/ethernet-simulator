/* Spawned by jamming transmitter state.

Sent to all nodes.

Triggers busy receiver state.
Triggers jamming transmitter state if it is transmitting content.
*/

public class StartJammingEvent  extends EthernetEvent {
    public StartJammingEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
