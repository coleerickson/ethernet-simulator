/* Sent by node in backoff state to itself.

   Possible result states:
   Backoff again
   Eager
   Sending preamble
 */

public class BackoffEvent extends EthernetEvent {
    public BackoffEvent(Node source, Node dest, double scheduledTime) {
        super(source, dest, scheduledTime);
    }
}
