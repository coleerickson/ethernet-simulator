/* Spawned by preparing next packet transmitter state.

Sent to all nodes.

Triggers eager transmitter state or sending preamble transmitter state.
*/

public class PacketReadyEvent extends EthernetEvent {
    public PacketReadyEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
