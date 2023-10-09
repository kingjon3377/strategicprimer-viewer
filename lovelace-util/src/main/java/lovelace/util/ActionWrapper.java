package lovelace.util;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A wrapper around an {@link ActionListener} (or equivalent lambda) that extends {@link AbstractAction}, for the
 * exceedingly common case of a JDK method requiring an {@link Action} when we don't need more functionality than a
 * single method accepting an {@link ActionEvent}.
 */
public class ActionWrapper extends AbstractAction {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ActionListener wrapped;

    public ActionWrapper(final ActionListener wrappedListener) {
        wrapped = wrappedListener;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        wrapped.actionPerformed(event);
    }
}
