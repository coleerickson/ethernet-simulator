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
    
    private int packetSize;
	
	// The backoff windows used in the Ethernet binary exponential backoff algorithm
	private int backoffWindow;

    // number of packets passing by the receiver, excluding packets that
    // this node transmits.
    private int ongoingTransmissions;
    
    // a count of the number of packets that have been successfully sent
    private double successfulPackets;
    
    private ReceiverState receiver;
    private TransmitterState transmitter;
    

    public Node(String name, int packetSize) {
        this.name = name;
        this.packetSize = packetSize; 
        backoffWindow = 0;

        receiver = ReceiverState.IDLE;
        transmitter = TransmitterState.PREPARING_NEXT_PACKET;
    }

    public String getName() { return name; }
    public double getPacketSize() { return packetSize; }
    public double getSuccessfulPackets() { return successfulPackets; }
}
