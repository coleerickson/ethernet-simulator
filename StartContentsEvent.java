public class StartContentsEvent extends EthernetEvent {

    // list of paired end events to cancel if the packet that is associated
    // with this event is canceled
    private Iterable<EndContentsEvent> endEvents;

    public StartContentsEvent(Node source, Node dest, double scheduledTime,
        Iterable<EndContentsEvent> endEvents) {
        super(source, dest, scheduledTime);
        this.endEvents = endEvents;
    }
}
