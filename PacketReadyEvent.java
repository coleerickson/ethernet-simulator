/* Spawned by preparing next packet transmitter state.

Sent to all nodes.

Triggers eager transmitter state or sending preamble transmitter state.
*/

public class PacketReadyEvent extends EthernetEvent {
  // TODO determine these constants by analyzing the simulator's performance with only one node on the network
  private static final double DELAY_MEAN = 100, DELAY_STANDARD_DEVIATION = 20;

  public PacketReadyEvent(Node source, EthernetSimulator simulator) {
    super(source, source, simulator.getRandom().nextGaussian() * DELAY_STANDARD_DEVIATION + DELAY_MEAN, simulator);
  }

  @Override
  public void process() {
    assert source.transmitter == Node.TransmitterState.PREPARING_NEXT_PACKET;
    source.transmitter = Node.TransmitterState.EAGER;

    // TODO create event to send preamble
  }

  @Override
  public String toString() {
    return "T" + scheduledTime + ", " + source.getName() + ": Packet is ready! Switching node state to eager.";
  }
}
