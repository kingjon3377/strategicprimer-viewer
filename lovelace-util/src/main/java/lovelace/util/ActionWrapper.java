package lovelace.util;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A wrapper around an {@link ActionListener} (or equivalent lambda) that extends {@link AbstractAction}, for the
 * exceedingly common case of a JDK method requiring an {@link Action} when we don't need more functionality than a
 * single method accepting an {@link ActionEvent}.
 * <p>
 * TODO: Do we really need this in Java?
 */
public class ActionWrapper extends AbstractAction {
	private final ActionListener wrapped;

	public ActionWrapper(ActionListener wrappedListener) {
		wrapped = wrappedListener;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		wrapped.actionPerformed(event);
	}
}
