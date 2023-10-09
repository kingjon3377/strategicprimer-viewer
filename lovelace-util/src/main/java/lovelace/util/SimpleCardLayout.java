package lovelace.util;

import java.awt.Container;
import java.awt.CardLayout;
import java.io.Serial;

/**
 * A convenience wrapper around {@link CardLayout} so callers don't have to
 * pass around a reference to the laid-out container to flip between cards.
 */
public class SimpleCardLayout extends CardLayout {
    @Serial
    private static final long serialVersionUID = 1;
    private final Container container;

    public SimpleCardLayout(final Container container) {
        this.container = container;
    }

    public void goFirst() {
        first(container);
    }

    public void goNext() {
        next(container);
    }

    public void goPrevious() {
        previous(container);
    }
}
