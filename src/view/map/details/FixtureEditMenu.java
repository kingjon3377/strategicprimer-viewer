package view.map.details;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.PlayerCollection;
/**
 * A pop-up menu to let the user edit a fixture.
 * @author Jonathan Lovelace
 *
 */
public class FixtureEditMenu extends JPopupMenu {
	/**
	 * The listener for the name-changing menu item.
	 * @author Jonathan Lovelace
	 */
	private static final class NameChangeListener implements ActionListener {
		/**
		 * The parent component.
		 */
		private final FixtureEditMenu outer;
		/**
		 * The fixture being edited.
		 */
		private final IFixture fixture;
		/**
		 * Constructor.
		 * @param parent the parent component
		 * @param fix the fixture being edited.
		 */
		NameChangeListener(final FixtureEditMenu parent, final IFixture fix) {
			outer = parent;
			fixture = fix;
		}
		/**
		 * @param event the button press being handled
		 */
		@Override
		public void actionPerformed(final ActionEvent event) {
			final String result = (String) JOptionPane.showInputDialog(
					outer, "Fixture's new name:",
					"Rename Fixture",
					JOptionPane.PLAIN_MESSAGE, null, null,
					((HasName) fixture).getName());
			if (result != null) {
				((HasName) fixture).setName(result);
			}
		}
	}
	/**
	 * Constructor.
	 * @param fixture the fixture the user clicked on
	 * @param players the players in the map
	 */
	public FixtureEditMenu(final IFixture fixture, final PlayerCollection players) {
		boolean mutable = false;
		final FixtureEditMenu outer = this;
		if (fixture instanceof HasName) {
			addMenuItem(new JMenuItem("Rename", KeyEvent.VK_N),
					new NameChangeListener(outer, fixture));
			mutable = true;
		}
		if (fixture instanceof HasKind) {
			addMenuItem(new JMenuItem("Change kind", KeyEvent.VK_K),
					new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent event) {
							final String result = (String) JOptionPane.showInputDialog(
									outer, "Fixture's new kind:",
									"Change Fixture Kind",
									JOptionPane.PLAIN_MESSAGE, null, null,
									((HasKind) fixture).getKind());
							if (result != null) {
								((HasKind) fixture).setKind(result);
							}
						}
					});
			mutable = true;
		}
		if (fixture instanceof HasOwner) {
			addMenuItem(new JMenuItem("Change owner", KeyEvent.VK_O),
					new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent event) {
							final Player result = (Player) JOptionPane
									.showInputDialog(outer,
											"Fixture's new owner:",
											"Change Fixture Owner",
											JOptionPane.PLAIN_MESSAGE, null,
											players.asArray(),
											((HasOwner) fixture).getOwner());
							if (result != null) {
								((HasOwner) fixture).setOwner(result);
							}
						}
					});
			mutable = true;
		}
		if (!mutable) {
			add(new JLabel("Fixture is not mutable"));
		}
	}
	/**
	 * Add a menu item, and attach a suitable listener to it.
	 * @param item the menu item
	 * @param listener the listener to listen to it
	 */
	private void addMenuItem(final JMenuItem item, final ActionListener listener) {
		add(item);
		item.addActionListener(listener);
	}
}
