import java.awt.event {
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JOptionPane,
    JMenuItem,
    JPopupMenu
}
import strategicprimer.model.common.map {
    HasMutableKind,
    HasMutableName,
    IFixture,
    HasName,
    Player,
    HasMutableOwner
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import java.lang {
    Types,
    ObjectArray
}
import lovelace.util.common {
    silentListener,
    as,
    todo
}

"A pop-up menu to let the user edit a fixture."
shared class FixtureEditMenu(
        "The fixture to be edited. Its type determines what menu items are enabled."
        IFixture fixture,
        "The players in the map(s)."
        {Player*} players,
        "A source for unique-in-the-map ID numbers."
        IDRegistrar idf,
        "A method to call to mark the maps as modified whenever any change is made."
        Anything() mutationListener,
        "Listeners to notify when something is renamed or changes kind."
        IWorkerTreeModel* changeListeners) extends JPopupMenu() { // FIXME: Name and varargs type don't fit usage ...
    void addMenuItem(JMenuItem item, Anything(ActionEvent) listener, Boolean enabled) {
        add(item);
        if (enabled) {
            item.addActionListener(listener);
        } else {
            item.enabled = false;
        }
    }

    void renameHandler() {
        assert (is HasMutableName fixture);
        String originalName = fixture.name;
        if (exists result = JOptionPane.showInputDialog(parent,
            "Fixture's new name:", "Rename Fixture",
            JOptionPane.plainMessage, null, null, Types.nativeString(originalName))) {
            String resultString = result.string.trimmed;
            if (resultString != originalName.trimmed) {
                HasMutableName temp = fixture;
                temp.name = resultString;
                for (listener in changeListeners) {
                    listener.renameItem(fixture);
                }
                mutationListener();
            }
        }
    }

    addMenuItem(JMenuItem("Rename", KeyEvent.vkN), silentListener(renameHandler),
        fixture is HasMutableName);

    void changeKindHandler() {
        assert (is HasMutableKind fixture);
        String originalKind = fixture.kind;
        if (exists result = JOptionPane.showInputDialog(parent,
            "Fixture's new kind:", "Change Fixture Kind",
            JOptionPane.plainMessage, null, null, Types.nativeString(originalKind))) {
            String resultString = result.string.trimmed;
            if (resultString != originalKind.trimmed) {
                HasMutableKind temp = fixture;
                temp.kind = resultString;
                for (listener in changeListeners) {
                    listener.moveItem(fixture, originalKind);
                }
                mutationListener();
            }
        }
    }

    addMenuItem(JMenuItem("Change kind", KeyEvent.vkK),
        silentListener(changeKindHandler), fixture is HasMutableKind);

    void changeOwnerHandler() {
        assert (is HasMutableOwner fixture);
        if (is Player player = JOptionPane.showInputDialog(parent, "Fixture's new owner:",
                "Change Fixture Owner", JOptionPane.plainMessage, null,
                ObjectArray.with(players), fixture.owner)) {
            HasMutableOwner temp = fixture;
            temp.owner = player;
            mutationListener(); // TODO: Notify callers beyond this, adding methods to IWorkerTreeModel if necessary? If a unit's owner changed, it shouldn't be in the tree anymore, after all ...
        }
    }

    addMenuItem(JMenuItem("Change owner", KeyEvent.vkO),
        silentListener(changeOwnerHandler), fixture is HasMutableOwner);

    void dismissHandler() {
        assert (is UnitMember fixture);
        String name = as<HasName>(fixture)?.name else "this ``fixture``";
        Integer reply = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to dismiss ``name``?",
            "Confirm Dismissal", JOptionPane.yesNoOption);
        if (reply == JOptionPane.yesOption) {
            for (listener in changeListeners) {
                listener.dismissUnitMember(fixture);
            }
            mutationListener();
        }
    }

    addMenuItem(JMenuItem("Dismiss", KeyEvent.vkD), silentListener(dismissHandler),
        fixture is UnitMember);

    todo("Generalize splitting to HasPopulation more generally")
    void splitAnimalHandler() {
        assert (is Animal fixture);
        if (exists result = JOptionPane.showInputDialog(parent,
                    "Number of animals to split to new population:",
                    "Split Animal Population", JOptionPane.plainMessage, null, null,
                    Types.nativeString("0")),
                is Integer num = Integer.parse(result.string.trimmed), num > 0,
                num < fixture.population) {
            Integer orig = fixture.population;
            Integer remaining = orig - num;
            Animal split = fixture.reduced(num, idf.createID());
            Animal remainder = fixture.reduced(remaining);
            for (listener in changeListeners) {
                listener.addSibling(fixture, split);
                listener.dismissUnitMember(fixture);
                listener.addSibling(split, remainder);
            }
            mutationListener();
        }
    }

    Boolean isAnimalPopulation;
    if (is Animal fixture) {
        isAnimalPopulation = fixture.population > 1;
    } else {
        isAnimalPopulation = false;
    }

    addMenuItem(JMenuItem("Split animal population", KeyEvent.vkS),
        silentListener(splitAnimalHandler), isAnimalPopulation);

    void sortHandler() {
        if (is IUnit fixture) {
            fixture.sortMembers();
            for (listener in changeListeners) {
                listener.refreshChildren(fixture);
            }
            mutationListener();
        }
        // TODO: Allow sorting fortresses as well.
    }

    addMenuItem(JMenuItem("Sort", KeyEvent.vkR), silentListener(sortHandler), fixture is IUnit);

    Boolean isEmptyUnit;
    if (is IUnit fixture) {
        isEmptyUnit = fixture.empty;
    } else {
        isEmptyUnit = false;
    }

    void removeUnitHandler() {
        assert (is IUnit fixture);
        Integer reply = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to remove this ``fixture.kind`` unit, \"``fixture.name``\"?",
            "Confirm Removal", JOptionPane.yesNoOption);
        if (reply == JOptionPane.yesOption) {
            for (listener in changeListeners) {
                listener.removeUnit(fixture);
            }
            mutationListener();
        }
    }

    addMenuItem(JMenuItem("Remove Unit", KeyEvent.vkM), silentListener(removeUnitHandler), isEmptyUnit);
}
