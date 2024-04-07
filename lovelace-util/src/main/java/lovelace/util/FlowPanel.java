package lovelace.util;

import java.awt.Component;
import java.io.Serial;
import javax.swing.JPanel;

/**
 * A {@link JPanel} using the default {@link java.awt.FlowLayout} and taking
 * children to add as constructor parameters.
 */
public final class FlowPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;

	public FlowPanel(final Component... components) {
		for (final Component component : components) {
			add(component);
		}
	}
}
