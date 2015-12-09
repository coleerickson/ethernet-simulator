/* Sent by node in backoff state to itself.

   Possible result states:
   Backoff again
   Eager
   Sending preamble
 */

public class BackoffEvent extends EthernetEvent {

    private boolean complete; // boolean to save the state of backoff. not complete means start

    private static final double SLOT_WAITING_TIME = 51.2  ; // waiting time for one slot, in microseconds

    public BackoffEvent(EthernetSimulator simulator, Node source, double scheduledTime, boolean complete) {
        super(simulator, source, scheduledTime);

        this.complete = complete;
    }

    @Override
    public void process(){
        if(!complete){
            // increment backOffWindow, schedule complete backoff event after a random slot waiting time from backOffWindow
            int backoffWindow = ++source.backoffWindow; // Here source.backoffWindow keeps track of backoff rounds done
            // while backoffWindow is the value we use to wait during backoff

            if (source.backoffWindow > 10) {
                backoffWindow = 10; // If backoff rounds greater than 10, we use a backoff window of 10
            }

            // if backoff rounds greater than 16, abort current packet, transmitter goes into preparing next packet state, then schedule
            // a packet ready event.
            if (source.backoffWindow > 16){
                source.backoffWindow = 0;
                source.transmitter = Node.TransmitterState.PREPARING_NEXT_PACKET;
                simulator.add(new PacketReadyEvent(simulator, source, scheduledTime));
                return;
            }

            int waitingTimeSlots = simulator.getRandom().nextInt(1 << backoffWindow);

            //System.out.println("[***] Back off round " + source.backoffWindow + " : Back off window is " + backoffWindow +
            //" and going to wait " + waitingTimeSlots + " slots.");

            simulator.add(new BackoffEvent(simulator, source, scheduledTime + SLOT_WAITING_TIME * waitingTimeSlots, !complete));
        } else {
            // backoff complete. If receiver idle, send preamble, otherwise transmitter state becomes eager.
            source.transmitter = Node.TransmitterState.EAGER;
            if (source.receiver == Node.ReceiverState.IDLE){
                // Schedule preamble event
                source.broadcastPreambleEvents(scheduledTime);
            }
        }
    }

    // TODO add transmitter and receiver states to debug info

    @Override
    public String toString() {
        return "T" + scheduledTime + ", " + source.getName() + ": back off round " +
                (complete ? source.backoffWindow : source.backoffWindow + 1) +
                (complete ? " complete" : " started.");
    }
}
