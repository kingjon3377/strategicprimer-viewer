package lovelace.util;

import java.awt.Container;
import java.awt.CardLayout;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

/**
 * A convenience wrapper around {@link CardLayout} so callers don't have to
 * pass around a reference to the laid-out container to flip between cards.
 */
@SuppressWarnings("ClassHasNoToStringMethod") // CardLayout toString suffices
public final class SimpleCardLayout extends CardLayout {
	@Serial
	private static final long serialVersionUID = 1L;
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

	@Serial
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, NotSerializableException {
		throw new NotSerializableException("lovelace.util.SimpleCardLayout");
	}

	@Serial
	private void writeObject(final ObjectOutputStream out) throws NotSerializableException {
		throw new NotSerializableException("lovelace.util.SimpleCardLayout");
	}
}
