import com.sun.xml.internal.ws.api.message.Packet;

import java.util.ArrayList;
import java.util.List;

public class Node {
    // The amount of packet overhead in bits. We use 20 bytes of overhead instead of 24 because we do not include a CRC in our simulation.
    public static final int PACKET_OVERHEAD_BITS = 20 * 8;
    public static final double SLOT_WAITING_TIME = 512 * EthernetSimulator.BIT_TIME; // waiting time for one slot, in microseconds
    // average collision duration was 92.1 computed from simulated data. with slot time of 51.2, implies gamma = 0.803, implies optimal idle slots is 0.8106
    public static final double IDLE_SENSE_TARGET_IDLE_SLOTS = 8;
    public static final double IDLE_SENSE_MULTIPLICATIVE_DECREASE_CONSTANT = 0.001; // epsilon from paper
    public static final double IDLE_SENSE_ADDITIVE_INCREASE_CONSTANT = 1 / 1.2; // alpha from paper


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
    private final int number;
    private final int repeater;

    // The size of packets (in bits) that this node sends
    private int packetSize;

    // Delay analytics
    public double beginningAttemptTime = -1;
    public double totalTransmissionDelay = 0;

    // Used for computing the number of idle slots in idle sense algorithm
    public double lastObservedTransmissionEnd = -1;
    public double idleSlotsBeforeTransmission = 0; // n_i in idle sense
    public int idleSenseMaxtrans = 5; // Grunenberger et al
    public double idleSenseNtrans = 0; // number of transmissions since recomputation of n_i hat -- ntrans as described by idle sense
    public double idleSenseSum = 0; // sum of idle slots observed in last idleSenseNtrans transmissions
    public double contentionWindow = 32;



    // number of packets passing by the receiver
    // increment on start of preamble, contents, jamming
    // decrement on end of preamble, contents, jamming
    public int ongoingTransmissions = 0;

    // a count of the number of packets that have been successfully sent
    public double successfulPackets = 0;

    public ReceiverState receiver = ReceiverState.IDLE;
    public TransmitterState transmitter = TransmitterState.PREPARING_NEXT_PACKET;

    // for analyzing average collision duration
    public double totalCollisionTime = 0;
    public int numCollisions = 0;

    private EthernetSimulator simulator;

    public List<ContentsEvent> packetsInProgress;

    public Node(EthernetSimulator simulator, int repeater, int number, int packetSize) {
        this.simulator = simulator;
        this.repeater = repeater;
        this.number = number;
        this.packetSize = packetSize;

        this.name = "Host " + number;

        // start preparing a packet to start this node up
        simulator.add(new PacketReadyEvent(simulator, this, simulator.getTime()));
    }

    // // The three broadcastEvents methods below are all very similar and demonstrate code duplication. Java does now
    // // allow you to invoke the constructor of the type parameter of a generic method, and alternatives like using
    // // a factory pattern seemed less desirable than a little code duplication.
    // // I would have liked to do something more like this:
    // public <T extends RoutedDataEvent> void broadcastRoutedDataEvents(double sendTime, double duration) {
    //     for (Node dest : simulator.getNodes()) {
    //         simulator.add(new T(simulator, this, dest, sendTime, true));
    //         simulator.add(new T(simulator, this, dest, sendTime + duration, false));
    //     }
    // }

    public void broadcastPreambleEvents(double sendTime) {
        assert this.transmitter == TransmitterState.EAGER;
        assert this.receiver == ReceiverState.IDLE;

        this.transmitter = TransmitterState.TRANSMITTING_PREAMBLE;

        double duration = PreambleEvent.BIT_TIME_DURATION * EthernetSimulator.BIT_TIME;
        for (Node dest : simulator.getNodes()) {
            simulator.add(new PreambleEvent(simulator, this, dest, sendTime, true));
            simulator.add(new PreambleEvent(simulator, this, dest, sendTime + duration, false));
        }
    }

    public void broadcastPacketContentsEvents(double sendTime) {
        assert this.transmitter == TransmitterState.TRANSMITTING_PREAMBLE;
        assert this.receiver == ReceiverState.IDLE;

        this.transmitter = TransmitterState.TRANSMITTING_CONTENTS;

        double duration = this.getPacketSize() * EthernetSimulator.BIT_TIME;

        this.packetsInProgress = new ArrayList<>();
        for (Node dest : simulator.getNodes()) {
            simulator.add(new ContentsEvent(simulator, this, dest, sendTime, true));
            ContentsEvent endContents = new ContentsEvent(simulator, this, dest, sendTime + duration, false);
            simulator.add(endContents);
            packetsInProgress.add(endContents);
        }
    }

    public void interruptTransmission(double sendTime) {
        assert this.transmitter == TransmitterState.TRANSMITTING_CONTENTS
                || this.transmitter == TransmitterState.TRANSMITTING_PREAMBLE;
        assert this.receiver == ReceiverState.BUSY; // TODO this might not be a valid assertion.

        // if we start jamming while sending contents, we have to truncate the contents. if we have just finished sending the
        // preamble, then we don't have to do anything.
        if (this.transmitter == TransmitterState.TRANSMITTING_CONTENTS) {
            assert packetsInProgress != null;
            for (ContentsEvent event : packetsInProgress) {
                assert sendTime < event.scheduledTime; // must make sure that the contents have not somehow already arrived
                assert this == event.source;

                event.cancel();

                // TODO clean this up
                ContentsEvent truncatedEvent = new ContentsEvent(simulator, this, event.dest, 0, false);
                truncatedEvent.scheduledTime = event.scheduledTime + simulator.getLayout().getPropagationDelay(event.source, event.dest);
                truncatedEvent.fail();

                // sum of overhead, packet size (subtracting truncated part), and jamming time
                this.totalCollisionTime += EthernetSimulator.BIT_TIME * (getPacketSize() - (event.scheduledTime - truncatedEvent.scheduledTime) + EthernetSimulator.PACKET_OVERHEAD_BITS + JammingEvent.BIT_TIME_DURATION);
                this.numCollisions++;

                simulator.add(truncatedEvent);
            }
            packetsInProgress = null;
        } else if (this.transmitter == TransmitterState.TRANSMITTING_PREAMBLE) {
            this.totalCollisionTime += EthernetSimulator.BIT_TIME * (JammingEvent.BIT_TIME_DURATION + EthernetSimulator.PACKET_OVERHEAD_BITS);
            this.numCollisions++;

            assert packetsInProgress == null;
        }

        this.transmitter = TransmitterState.JAMMING;

        double duration = JammingEvent.BIT_TIME_DURATION * EthernetSimulator.BIT_TIME;
        for (Node dest : simulator.getNodes()) {
            simulator.add(new JammingEvent(simulator, this, dest, sendTime, true));
            simulator.add(new JammingEvent(simulator, this, dest, sendTime + duration, false));
        }
    }

    public void transitionToIdle(double currentTime) {
        assert this.ongoingTransmissions == 0;

        // whenever we transition to idle, there is no data passing the receiver. So it is here that we track the end of
        // a packet for idle sense. The code below is adapted from Figure 6 of Heusse et al.
        assert this.lastObservedTransmissionEnd == -1;
        this.lastObservedTransmissionEnd = currentTime;
        this.idleSenseSum += this.idleSlotsBeforeTransmission;
        this.idleSlotsBeforeTransmission = 0;
        if (this.idleSenseNtrans >= idleSenseMaxtrans) {
            // compute idle slots estimator
            double idleSlotsEstimate = idleSenseSum / idleSenseNtrans;


            idleSenseSum = 0;
            idleSenseNtrans = 0;


            // additive-increase/multiplicative-decrease
            if (idleSlotsEstimate < IDLE_SENSE_TARGET_IDLE_SLOTS) { // needs to be more idle, so contention window must increase
                this.contentionWindow += 1;
            } else { // needs to be less idle, so contention window must decrease
                this.contentionWindow *= 0.99;
            }
        }

        // schedule idleness
        this.receiver = Node.ReceiverState.WAITING_INTERPACKET;
        this.simulator.add(new ReceiverIdleEvent(this.simulator, this, currentTime));
    }

    /**
     *
     * @return random number in [0, CW)
     */
    public int getBackoffSlots() {
        int cw = (int)contentionWindow;

//        if (cw < 7) {
//            cw = 7;
//            contentionWindow = 7;
//        }

        //System.out.println(cw);

        if (cw <= 0) {
            return 0;
        } else {
            return simulator.getRandom().nextInt(cw);
        }
    }

    public String getName() { return name; }
    public double getPacketSize() { return packetSize; }
    public double getBitsSent() {
        return this.successfulPackets * (getPacketSize() + PACKET_OVERHEAD_BITS);
    }
    public int getNumber() { return number;}
    public int getRepeater() {return repeater; }
}
