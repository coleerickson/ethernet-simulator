interface Layout {

    public static final double DELAY_TO_REPEATER = 0.025; // in microseconds

    /**
     * Computes the propagation delay between two nodes.
     */
    double getPropagationDelay(Node a, Node b);
}
