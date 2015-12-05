public abstract class RoutedDataEvent extends EthernetEvent {
    protected Node dest;
    protected boolean start;

    /**
     * A routed data event is an event that involves the sending of data from * one node to another. (e.g., sending the packet preamble, contents, or
     * sending a jamming signal)
     *
     * @param sendTime The time that the data is sent from the source.
     * @param start True if this marks the beginning of data transfer,
                    false if it marks the end of data transfer.
     */
    protected RoutedDataEvent(EthernetSimulator simulator, Node source, Node dest, double sendTime, boolean start) {
        super(simulator, source, sendTime);
        this.dest = dest;
        this.start = start;

        super.scheduledTime += simulator.getLayout().getPropagationDelay(source, dest);
    }

    public void process() {
        if (start) {
            ++dest.ongoingTransmissions;

            dest.receiver = Node.ReceiverState.BUSY;

            // If the destination node is transmitting when it receives the start of data, then that is a collision!
            // We have to check if the source node is the same as the destination node, though, because we don't want
            // to detect this

            // TODO check if we need to use .equals() for the source/dest check. I think we only ever are passing one
            // pointer to them around, so it shouldn't be a problem. But it might be better to change it anyway
            if (dest.transmitter == Node.TransmitterState.TRANSMITTING_CONTENTS && source != dest) {
                // TODO abort transmission (cancel the associated end event of the node that is currently transmitting)
                // and jam
            }

        } else {
            --dest.ongoingTransmissions;

            // The receiver can only ever become idle at the end of a data event, so we check for that here.
            if (dest.ongoingTransmissions == 0) {
                // TODO schedule an event for this because we need to allow interpacket gap
                dest.receiver = Node.ReceiverState.IDLE;
            }
        }

        assert dest.ongoingTransmissions >= 0;
    }
}
