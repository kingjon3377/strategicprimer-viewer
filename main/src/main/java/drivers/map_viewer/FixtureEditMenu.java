package drivers.map_viewer;

import java.util.stream.StreamSupport;
import java.util.Optional;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.IFixture;
import common.map.HasName;
import common.map.Player;
import common.map.HasMutableOwner;
import common.map.fixtures.UnitMember;

import worker.common.IFixtureEditHelper;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.idreg.IDRegistrar;
import java.util.logging.Logger;

/**
 * A pop-up menu to let the user edit a fixture.
 */
public class FixtureEditMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	protected static final Logger LOGGER = Logger.getLogger(FixtureEditMenu.class.getName());
	/**
	 * @param fixture The fixture to be edited. Its type determines what
	 * menu items are enabled.
	 * @param players The players in the map(s).
	 * @param idf A source for unique-in-the-map ID numbers.
	 * @param handler Listeners to notify when something is renamed or changes kind.
	 */
	public FixtureEditMenu(final IFixture fixture, final Iterable<Player> players, final IDRegistrar idf,
	                       final IFixtureEditHelper handler) {
		this.fixture = fixture;
		this.players = players;
		this.idf = idf;
		this.handler = handler;
		addMenuItem(new JMenuItem("Rename", KeyEvent.VK_N), ignored -> renameHandler(),
			fixture instanceof HasMutableName);
		addMenuItem(new JMenuItem("Change kind", KeyEvent.VK_K), ignored -> changeKindHandler(),
			fixture instanceof HasMutableKind);
		addMenuItem(new JMenuItem("Change owner", KeyEvent.VK_O), ignored -> changeOwnerHandler(),
			fixture instanceof HasMutableOwner);
		addMenuItem(new JMenuItem("Dismiss", KeyEvent.VK_D), ignored -> dismissHandler(),
			fixture instanceof UnitMember);
		boolean isAnimalPopulation;
		if (fixture instanceof Animal) { // TODO: Condense to ternary
			isAnimalPopulation = ((Animal) fixture).getPopulation() > 1;
		} else {
			isAnimalPopulation = false;
		}

		addMenuItem(new JMenuItem("Split animal population", KeyEvent.VK_S),
			ignored -> splitAnimalHandler(), isAnimalPopulation);

		addMenuItem(new JMenuItem("Sort", KeyEvent.VK_R), ignored -> sortHandler(),
			fixture instanceof IUnit);

		boolean isEmptyUnit;
		if (fixture instanceof IUnit) { // TODO: condense to ternary
			isEmptyUnit = ((IUnit) fixture).isEmpty();
		} else {
			isEmptyUnit = false;
		}

		addMenuItem(new JMenuItem("Remove Unit", KeyEvent.VK_M), ignored -> removeUnitHandler(),
			isEmptyUnit);
	}

	private final IFixture fixture;
	private final Iterable<Player> players;
	private final IDRegistrar idf;
	private final IFixtureEditHelper handler;

	private void addMenuItem(final JMenuItem item, final ActionListener listener, final boolean enabled) {
		add(item);
		if (enabled) {
			item.addActionListener(listener);
		} else {
			item.setEnabled(false);
		}
	}

	private void renameHandler() {
		HasMutableName fix = (HasMutableName) fixture;
		String originalName = fix.getName();
		Object result = JOptionPane.showInputDialog(getParent(), "Fixture's new name:",
			"Rename Fixture", JOptionPane.PLAIN_MESSAGE, null, null, originalName);
		if (result instanceof String) {
			String resultString = ((String) result).trim();
			if (!resultString.equals(originalName.trim())) {
				handler.renameItem(fix, resultString);
			}
		}
	}

	private void changeKindHandler() {
		HasMutableKind fix = (HasMutableKind) fixture;
		String originalKind = fix.getKind();
		Object result = JOptionPane.showInputDialog(getParent(), "Fixture's new kind:",
			"Change Fixture Kind", JOptionPane.PLAIN_MESSAGE, null, null, originalKind);
		if (result instanceof String) {
			String resultString = ((String) result).trim();
			if (!resultString.equals(originalKind.trim())) {
				handler.changeKind(fix, resultString);
			}
		}
	}

	private void changeOwnerHandler() {
		HasMutableOwner fix = (HasMutableOwner) fixture;
		Object player = JOptionPane.showInputDialog(getParent(), "Fixture's new owner:",
			"Change Fixture Owner", JOptionPane.PLAIN_MESSAGE, null,
			StreamSupport.stream(players.spliterator(), false).toArray(Player[]::new),
			fix.getOwner());
		if (player instanceof Player) {
			handler.changeOwner(fix, (Player) player);
		}
	}

	private void dismissHandler() {
		UnitMember fix = (UnitMember) fixture;
		String name = Optional.of(fix).filter(HasName.class::isInstance).map(HasName.class::cast)
			.map(HasName::getName).orElse("this " + fix);
		int reply = JOptionPane.showConfirmDialog(getParent(),
			String.format("Are you sure you want to dismiss %s?", name), "Confirm Dismissal",
			JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			handler.dismissUnitMember(fix);
		}
	}

	/**
	 * TODO: Generalize splitting to HasPopulation more generally
	 */
	private void splitAnimalHandler() {
		Animal fix = (Animal) fixture;
		Object result = JOptionPane.showInputDialog(getParent(),
			"Number of animals to split to new population:", "Split Animal Population",
			JOptionPane.PLAIN_MESSAGE, null, null, "0");
		if (result instanceof String) {
			int num;
			try {
				num = Integer.parseInt(((String) result).trim());
			} catch (final NumberFormatException except) {
				// FIXME: Log the failure
				return;
			}
			int orig = fix.getPopulation();
			if (num <= 0 || num > orig) {
				return;
			}
			int remaining = orig - num;
			Animal split = fix.reduced(num, idf.createID()); // TODO: Add helper for this to IFixtureEditHelper
			Animal remainder = fix.reduced(remaining);
			handler.addSibling(fix, split);
			handler.dismissUnitMember(fix);
			handler.addSibling(split, remainder);
		}
	}

	private void sortHandler() {
		if (fixture instanceof IUnit) {
			handler.sortMembers((IUnit) fixture);
		}
		// TODO: Allow sorting fortresses as well.
	}

	private void removeUnitHandler() {
		IUnit fix = (IUnit) fixture;
		int reply = JOptionPane.showConfirmDialog(getParent(),
			String.format("Are you sure you want to remove this %s unit, \"%s\"?",
				fix.getKind(), fix.getName()),
			"Confirm Removal", JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			handler.removeUnit(fix);
		}
	}
}
