package lovelace.util;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

/**
 * A wrapper around an {@link ActionListener} (or equivalent lambda) that extends {@link AbstractAction}, for the
 * exceedingly common case of a JDK method requiring an {@link Action} when we don't need more functionality than a
 * single method accepting an {@link ActionEvent}.
 */
public class ActionWrapper extends AbstractAction {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final String CLASS_NAME = "lovelace.util.ActionWrapper";
	private final ActionListener wrapped;

	public ActionWrapper(final ActionListener wrappedListener) {
		wrapped = wrappedListener;
	}

	@Override
	public final void actionPerformed(final ActionEvent event) {
		wrapped.actionPerformed(event);
	}

	@Override
	public final String toString() {
		return "ActionWrapper wrapping " + wrapped;
	}

	@Serial
	private void readObject(final ObjectInputStream in) throws ClassNotFoundException, NotSerializableException {
		throw new NotSerializableException(CLASS_NAME);
	}

	@Serial
	private void writeObject(final ObjectOutputStream out) throws NotSerializableException {
		throw new NotSerializableException(CLASS_NAME);
	}

	@Override
	public final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(CLASS_NAME);
	}
}
