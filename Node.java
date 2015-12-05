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

    // An identifier for this node
    private final String name;

    // The size of packets (in bits) that this node sends
    private int packetSize;

    // The backoff windows used in the Ethernet binary exponential backoff algorithm
    private int backoffWindow;

    // number of packets passing by the receiver
    // increment on start of preamble, contents, jamming
    // decrement on end of preamble, contents, jamming
    public int ongoingTransmissions;

    // a count of the number of packets that have been successfully sent
    private double successfulPackets;

    public ReceiverState receiver;
    public TransmitterState transmitter;

    private EthernetSimulator simulator;

    public Node(EthernetSimulator simulator, String name, int packetSize) {
        this.name = name;
        this.packetSize = packetSize;
        this.simulator = simulator;

        backoffWindow = 0;

        receiver = ReceiverState.IDLE;
        transmitter = TransmitterState.PREPARING_NEXT_PACKET;

        // start preparing a packet to start this node up
        simulator.add(new PacketReadyEvent(simulator, this, simulator.getTime()));
    }

    // public <T extends RoutedDataEvent> void broadcastRoutedDataEvents(double sendTime, double duration) {
    //     for (Node dest : simulator.getNodes()) {
    //         simulator.add(new T(simulator, this, dest, sendTime, true));
    //         simulator.add(new T(simulator, this, dest, sendTime + duration, false));
    //     }
    // }

    public void broadcastPreambleEvents(double sendTime) {
        // a preamble
        double duration = PreambleEvent.BIT_TIME_DURATION * EthernetSimulator.BIT_TIME;

        this.transmitter = TransmitterState.TRANSMITTING_PREAMBLE;

        for (Node dest : simulator.getNodes()) {
            simulator.add(new PreambleEvent(simulator, this, dest, sendTime, true));
            simulator.add(new PreambleEvent(simulator, this, dest, sendTime + duration, false));
        }
    }

    public String getName() { return name; }
    public double getPacketSize() { return packetSize; }
    public double getSuccessfulPackets() { return successfulPackets; }
}
