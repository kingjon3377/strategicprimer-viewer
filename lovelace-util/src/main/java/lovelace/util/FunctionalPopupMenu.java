package lovelace.util;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

/**
 * A {@link JPopupMenu} that takes its menu items as constructor parameters.
 */
public class FunctionalPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1;
	public FunctionalPopupMenu(JMenuItem... items) {
		for (JMenuItem item : items) {
			add(item);
		}
	}
}
