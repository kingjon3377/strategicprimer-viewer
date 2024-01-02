package drivers.map_viewer;

import java.io.Serial;
import java.util.stream.StreamSupport;
import java.util.Optional;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import legacy.map.HasMutableKind;
import legacy.map.HasMutableName;
import legacy.map.IFixture;
import common.map.HasName;
import legacy.map.Player;
import legacy.map.HasMutableOwner;
import legacy.map.fixtures.UnitMember;

import worker.common.IFixtureEditHelper;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Animal;
import legacy.idreg.IDRegistrar;

/**
 * A pop-up menu to let the user edit a fixture.
 */
public class FixtureEditMenu extends JPopupMenu {
    @Serial
    private static final long serialVersionUID = 1L;

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
        final boolean isAnimalPopulation = fixture instanceof final Animal a && a.getPopulation() > 1;

        addMenuItem(new JMenuItem("Split animal population", KeyEvent.VK_S),
                ignored -> splitAnimalHandler(), isAnimalPopulation);

        addMenuItem(new JMenuItem("Sort", KeyEvent.VK_R), ignored -> sortHandler(),
                fixture instanceof IUnit);

        final boolean isEmptyUnit = fixture instanceof final IUnit u && u.isEmpty();

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
        final HasMutableName fix = (HasMutableName) fixture;
        final String originalName = fix.getName();
        final Object result = JOptionPane.showInputDialog(getParent(), "Fixture's new name:",
                "Rename Fixture", JOptionPane.PLAIN_MESSAGE, null, null, originalName);
        if (result instanceof final String s) {
            final String resultString = s.strip();
            if (!resultString.equals(originalName.strip())) {
                handler.renameItem(fix, resultString);
            }
        }
    }

    private void changeKindHandler() {
        final HasMutableKind fix = (HasMutableKind) fixture;
        final String originalKind = fix.getKind();
        final Object result = JOptionPane.showInputDialog(getParent(), "Fixture's new kind:",
                "Change Fixture Kind", JOptionPane.PLAIN_MESSAGE, null, null, originalKind);
        if (result instanceof final String s) {
            final String resultString = s.strip();
            if (!resultString.equals(originalKind.strip())) {
                handler.changeKind(fix, resultString);
            }
        }
    }

    private void changeOwnerHandler() {
        final HasMutableOwner fix = (HasMutableOwner) fixture;
        final Object player = JOptionPane.showInputDialog(getParent(), "Fixture's new owner:",
                "Change Fixture Owner", JOptionPane.PLAIN_MESSAGE, null,
                StreamSupport.stream(players.spliterator(), false).toArray(Player[]::new),
                fix.owner());
        if (player instanceof final Player p) {
            handler.changeOwner(fix, p);
        }
    }

    private void dismissHandler() {
        final UnitMember fix = (UnitMember) fixture;
        final String name = Optional.of(fix).filter(HasName.class::isInstance).map(HasName.class::cast)
                .map(HasName::getName).orElse("this " + fix);
        final int reply = JOptionPane.showConfirmDialog(getParent(),
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
        final Animal fix = (Animal) fixture;
        final Object result = JOptionPane.showInputDialog(getParent(),
                "Number of animals to split to new population:", "Split Animal Population",
                JOptionPane.PLAIN_MESSAGE, null, null, "0");
        if (result instanceof final String s) {
            final int num;
            try {
                num = Integer.parseInt(s.strip());
            } catch (final NumberFormatException except) {
                // FIXME: Log the failure
                return;
            }
            final int orig = fix.getPopulation();
            if (num <= 0 || num > orig) {
                return;
            }
            final int remaining = orig - num;
            final Animal split = fix.reduced(num, idf.createID()); // TODO: Add helper for this to IFixtureEditHelper
            final Animal remainder = fix.reduced(remaining);
            handler.addSibling(fix, split);
            handler.dismissUnitMember(fix);
            handler.addSibling(split, remainder);
        }
    }

    private void sortHandler() {
        if (fixture instanceof final IUnit u) {
            handler.sortMembers(u);
        }
        // TODO: Allow sorting fortresses as well.
    }

    private void removeUnitHandler() {
        final IUnit fix = (IUnit) fixture;
        final int reply = JOptionPane.showConfirmDialog(getParent(),
                String.format("Are you sure you want to remove this %s unit, \"%s\"?",
                        fix.getKind(), fix.getName()),
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            handler.removeUnit(fix);
        }
    }
}
