package view.map.details;

import static javax.swing.JOptionPane.showInputDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMutablePlayerCollection;
import model.map.IPlayerCollection;
import model.map.Player;
import model.map.PlayerCollection;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A pop-up menu to let the user edit a fixture.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureEditMenu extends JPopupMenu {
	/**
	 * Listeners to notify about name and kind changes.
	 */
	protected List<IWorkerTreeModel> listeners = new ArrayList<>();
	/**
	 * The listener for the name-changing menu item.
	 *
	 * @author Jonathan Lovelace
	 */
	private final class NameChangeListener implements ActionListener {
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
		 *
		 * @param parent the parent component
		 * @param fix the fixture being edited.
		 */
		protected NameChangeListener(final FixtureEditMenu parent,
				final IFixture fix) {
			outer = parent;
			fixture = fix;
		}

		/**
		 * @param event the button press being handled
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent event) {
			final String old = ((HasName) fixture).getName();
			final String result =
					(String) showInputDialog(outer, "Fixture's new name:",
							"Rename Fixture", JOptionPane.PLAIN_MESSAGE, null,
							null, ((HasName) fixture).getName());
			if (result != null && !result.equals(old)) {
				((HasName) fixture).setName(result);
				for (final IWorkerTreeModel listener : listeners) {
					listener.renameItem((HasName) fixture);
				}
			}
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "NameChangeListener";
		}
	}

	/**
	 * Constructor.
	 *
	 * @param fixture the fixture the user clicked on
	 * @param players the players in the map
	 */
	public FixtureEditMenu(final IFixture fixture,
			final IPlayerCollection players,
			final IWorkerTreeModel... changeListeners) {
		for (final IWorkerTreeModel listener : changeListeners) {
			listeners.add(listener);
		}
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
						public void actionPerformed(
								@Nullable final ActionEvent event) {
							final String old = ((HasKind) fixture).getKind();
							final String result = (String) showInputDialog(
									outer, "Fixture's new kind:",
									"Change Fixture Kind",
									JOptionPane.PLAIN_MESSAGE, null, null,
									((HasKind) fixture).getKind());
							if (result != null && !old.equals(result)) {
								((HasKind) fixture).setKind(result);
								for (final IWorkerTreeModel listener : listeners) {
									listener.moveItem((HasKind) fixture);
								}
							}
						}
					});
			mutable = true;
		}
		if (fixture instanceof HasOwner) {
			addMenuItem(new JMenuItem("Change owner", KeyEvent.VK_O),
					new ActionListener() {
						@Override
						public void actionPerformed(
								@Nullable final ActionEvent event) {
							final Player result =
									(Player) showInputDialog(outer,
											"Fixture's new owner:",
											"Change Fixture Owner",
											JOptionPane.PLAIN_MESSAGE, null,
											playersAsArray(players),
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
	 * @param players a collection of players
	 * @return it as an array
	 */
	protected static Player[] playersAsArray(final IPlayerCollection players) {
		if (players instanceof IMutablePlayerCollection) {
			return ((PlayerCollection) players).asArray(); // NOPMD
		} else {
			final List<Player> list = new ArrayList<>();
			for (final Player player : players) {
				list.add(player);
			}
			return NullCleaner.assertNotNull(list.toArray(new Player[list.size()]));
		}
	}
	/**
	 * Add a menu item, and attach a suitable listener to it.
	 *
	 * @param item the menu item
	 * @param listener the listener to listen to it
	 */
	private void addMenuItem(final JMenuItem item, final ActionListener listener) {
		add(item);
		item.addActionListener(listener);
	}
}
