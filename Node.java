public class Node {

    enum ReceiverState {
        BUSY,
	    WAITING_INTERPACKET,
	    IDLE;
    }

    enum TransmitterState {
        // INACTIVE, // it might be useful to have a state in which the node is not trying to send at all
        PREPARING_NEXT_PACKET,
        EAGER,
        TRANSMITTING_PREAMBLE,
        TRANSMITTING_CONTENTS,
        JAMMING,
        WAITING_FOR_BACKOFF;
    }

    private final String name;

    private ReceiverState receiver;
    private TransmitterState transmitter;

    public Node(String name) {
        this.name = name;

        receiver = ReceiverState.IDLE;
        transmitter = TransmitterState.PREPARING_NEXT_PACKET;
    }

    /**
     * Allows the node to check if it needs to schedule event.
     */
    public void update() {
        
    }

    public String getName() { return name; }
}
