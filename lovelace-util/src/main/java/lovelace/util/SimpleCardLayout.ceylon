import java.awt {
    Container,
    CardLayout
}

"A convenience wrapper around [[CardLayout]] so callers don't have to pass around a
 reference to the laid-out container to flip between cards."
shared class SimpleCardLayout(Container container) extends CardLayout() {
    shared void goFirst() => super.first(container);
    shared void goNext() => super.next(container);
    shared void goPrevious() => super.previous(container);
}
