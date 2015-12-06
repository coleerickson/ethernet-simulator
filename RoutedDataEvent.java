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

            // if we receive the start of data while we are idle, then switch to busy. but don't switch while we are
            // waiting in the interpacket gap.
            if (dest.receiver == Node.ReceiverState.IDLE) {
                dest.receiver = Node.ReceiverState.BUSY;
            }

            // If the destination node is transmitting when it receives the start of data, then that is a collision!
            // We have to check if the source node is the same as the destination node, though, because we don't want
            // to detect our own transmission as colliding with itself
            if (dest.transmitter == Node.TransmitterState.TRANSMITTING_CONTENTS && source != dest) {
                dest.interruptTransmission(super.scheduledTime);
            }
        } else {
            --dest.ongoingTransmissions;

            // The receiver can only ever become idle at the end of a contents or packetready event, so we check for that here.
            if (dest.ongoingTransmissions == 0) {
                if (dest.transmitter == Node.TransmitterState.TRANSMITTING_PREAMBLE) {
                    // If we receive the end of a transmission and the number of ongoing transmissions is then zero, and
                    // we are in the TRANSMITTING_PREAMBLE state, then we must have received the end of our own preamble.
                    // After we are done transmitting our own preamble, we either jam or transmit contents. For this
                    // reason, we do not schedule a transition to idle in this case. We do, however, become idle
                    // immediately so that it is clear that it is OK to start sending contents immediately.
                    dest.receiver = Node.ReceiverState.IDLE;
                } else {
                    // If we are not transmitting a preamble and the number of ongoing transmissions is now zero, then
                    // we SHOULD transition to idle.
                    dest.transitionToIdle(super.scheduledTime);
                }
            }
        }

        assert dest.ongoingTransmissions >= 0;
    }
}
