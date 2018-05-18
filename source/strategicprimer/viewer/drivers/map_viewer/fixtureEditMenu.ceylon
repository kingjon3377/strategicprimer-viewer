import java.awt.event {
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JOptionPane,
    JMenuItem,
    JPopupMenu
}
import strategicprimer.model.map {
    Player,
    IFixture,
    HasMutableKind,
    HasMutableName,
    HasMutableOwner,
    HasName
}
import strategicprimer.model.map.fixtures {
    UnitMember
}

import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import strategicprimer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.model.idreg {
    IDRegistrar
}
import java.lang {
    Types,
	ObjectArray
}
"A pop-up menu to let the user edit a fixture."
shared JPopupMenu fixtureEditMenu(IFixture fixture, {Player*} players,
        IDRegistrar idf, IWorkerTreeModel* changeListeners) {
    JPopupMenu retval = JPopupMenu();
    void addMenuItem(JMenuItem item, Anything(ActionEvent) listener) {
        retval.add(item);
        item.addActionListener(listener);
    }
    void addDisabledMenuItem(JMenuItem item) {
        retval.add(item);
        item.setEnabled(false);
    }
    if (is HasMutableName fixture) {
        addMenuItem(JMenuItem("Rename", KeyEvent.vkN), (ActionEvent event) {
            String originalName = fixture.name;
            if (exists result = JOptionPane.showInputDialog(retval,
                "Fixture's new name:", "Rename Fixture",
                JOptionPane.plainMessage, null, null, Types.nativeString(originalName))) {
                String resultString = result.string.trimmed;
                if (resultString != originalName.trimmed) {
                    HasMutableName temp = fixture;
                    temp.name = resultString;
                    for (listener in changeListeners) {
                        listener.renameItem(fixture);
                    }
                }
            }
        });
    } else {
        addDisabledMenuItem(JMenuItem("Rename", KeyEvent.vkN));
    }
    if (is HasMutableKind fixture) {
        addMenuItem(JMenuItem("Change kind", KeyEvent.vkK), (ActionEvent event) {
            String originalKind = fixture.kind;
            if (exists result = JOptionPane.showInputDialog(retval,
                "Fixture's new kind:", "Change Fixture Kind",
                JOptionPane.plainMessage, null, null, Types.nativeString(originalKind))) {
                String resultString = result.string.trimmed;
                if (resultString != originalKind.trimmed) {
                    HasMutableKind temp = fixture;
                    temp.kind = resultString;
                    for (listener in changeListeners) {
                        listener.moveItem(fixture, originalKind);
                    }
                }
            }
        });
    } else {
        addDisabledMenuItem(JMenuItem("Change kind", KeyEvent.vkK));
    }
    if (is HasMutableOwner fixture) {
        addMenuItem(JMenuItem("Change owner", KeyEvent.vkO), (ActionEvent event) {
            if (is Player player = JOptionPane.showInputDialog(retval,
                "Fixture's new owner:", "Change Fixture Owner",
                JOptionPane.plainMessage, null, ObjectArray.with(players),
                fixture.owner)) {
                HasMutableOwner temp = fixture;
                temp.owner = player;
            }
        });
    } else {
        addDisabledMenuItem(JMenuItem("Change owner", KeyEvent.vkO));
    }
    if (is UnitMember fixture) {
        String name;
        if (is HasName fixture) {
            name = fixture.name;
        } else {
            name = "this ``fixture``";
        }
        addMenuItem(JMenuItem("Dismiss", KeyEvent.vkD), (ActionEvent event) {
            Integer reply = JOptionPane.showConfirmDialog(retval,
                "Are you sure you want to dismiss ``name``?",
                "Confirm Dismissal", JOptionPane.yesNoOption);
            if (reply == JOptionPane.yesOption) {
                for (listener in changeListeners) {
                    listener.dismissUnitMember(fixture);
                }
            }
        });
    } else {
        addDisabledMenuItem(JMenuItem("Dismiss", KeyEvent.vkD));
    }
    if (is Animal fixture, fixture.population > 1) { // TODO: Generalize to HasPopulation more generally
        addMenuItem(JMenuItem("Split animal population", KeyEvent.vkS),
                    (ActionEvent event) {
            if (exists result = JOptionPane.showInputDialog(retval,
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
            }
        });
    } else {
        addDisabledMenuItem(JMenuItem("Split animal population", KeyEvent.vkS));
    }
    return retval;
}
