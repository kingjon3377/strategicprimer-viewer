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
        // TODO: What is this for?
        Anything() mutationListener,
        "Listeners to notify when something is renamed or changes kind."
        IWorkerTreeModel* changeListeners) extends JPopupMenu() {
    void addMenuItem(JMenuItem item, Anything(ActionEvent) listener) { // TODO: Make a combined addMenuItem(item, handlerIfEnabled, conditionToEnable), to condense the below code
        add(item);
        item.addActionListener(listener);
    }

    void addDisabledMenuItem(JMenuItem item) {
        add(item);
        item.enabled = false;
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

    if (is HasMutableName fixture) {
        addMenuItem(JMenuItem("Rename", KeyEvent.vkN), silentListener(renameHandler));
    } else {
        addDisabledMenuItem(JMenuItem("Rename", KeyEvent.vkN));
    }

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

    if (is HasMutableKind fixture) {
        addMenuItem(JMenuItem("Change kind", KeyEvent.vkK),
            silentListener(changeKindHandler));
    } else {
        addDisabledMenuItem(JMenuItem("Change kind", KeyEvent.vkK));
    }

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

    if (is HasMutableOwner fixture) {
        addMenuItem(JMenuItem("Change owner", KeyEvent.vkO),
            silentListener(changeOwnerHandler));
    } else {
        addDisabledMenuItem(JMenuItem("Change owner", KeyEvent.vkO));
    }

    void dismissHandler() {
        assert (is UnitMember fixture);
        String name;
        if (is HasName fixture) { // TODO: Condense to as<HasName>(fixture)?.name else "this ``fixture``"
            name = fixture.name;
        } else {
            name = "this ``fixture``";
        }
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

    if (is UnitMember fixture) {
        addMenuItem(JMenuItem("Dismiss", KeyEvent.vkD), silentListener(dismissHandler));
    } else {
        addDisabledMenuItem(JMenuItem("Dismiss", KeyEvent.vkD));
    }

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

    if (is Animal fixture, fixture.population > 1) {
        addMenuItem(JMenuItem("Split animal population", KeyEvent.vkS),
            silentListener(splitAnimalHandler));
    } else {
        addDisabledMenuItem(JMenuItem("Split animal population", KeyEvent.vkS));
    }
    // TODO: Add "Sort" for units and fortresses
}
