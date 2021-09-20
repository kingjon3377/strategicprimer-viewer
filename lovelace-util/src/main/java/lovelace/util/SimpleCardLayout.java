package lovelace.util;

import java.awt.Container;
import java.awt.CardLayout;

/**
 * A convenience wrapper around {@link CardLayout} so callers don't have to
 * pass around a reference to the laid-out container to flip between cards.
 */
public class SimpleCardLayout extends CardLayout {
	private static final long serialVersionUID = 1;
	private final Container container;
	public SimpleCardLayout(final Container container) {
		this.container = container;
	}
	public void goFirst() {
		super.first(container);
	}
	public void goNext() {
		super.next(container);
	}
	public void goPrevious() {
		super.previous(container);
	}
}
