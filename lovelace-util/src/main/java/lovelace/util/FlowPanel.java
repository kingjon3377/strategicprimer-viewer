package lovelace.util;

import java.awt.Component;
import javax.swing.JPanel;
/**
 * A {@link JPanel} using the default {@link java.awt.FlowLayout} and taking
 * children to add as constructor parameters.
 */
public class FlowPanel extends JPanel {
	private static final long serialVersionUID = 1;
	public FlowPanel(final Component... components) {
		for (Component component : components) {
			add(component);
		}
	}
}
