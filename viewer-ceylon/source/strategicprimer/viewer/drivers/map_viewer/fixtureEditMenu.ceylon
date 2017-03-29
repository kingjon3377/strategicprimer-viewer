import ceylon.interop.java {
    javaString,
    createJavaObjectArray
}

import java.awt.event {
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JOptionPane,
    JMenuItem,
    JLabel,
    JPopupMenu
}
import strategicprimer.viewer.model.map {
    HasMutableKind,
    HasMutableName,
    HasMutableOwner
}
import model.map {
    Player,
    IFixture,
    HasName
}
import model.map.fixtures {
    UnitMember
}

import strategicprimer.viewer.drivers.worker_mgmt {
    IWorkerTreeModel
}
"A pop-up menu to let the user edit a fixture."
shared JPopupMenu fixtureEditMenu(IFixture fixture, {Player*} players,
        IWorkerTreeModel* changeListeners) {
    variable Boolean immutable = true;
    JPopupMenu retval = JPopupMenu();
    void addMenuItem(JMenuItem item, Anything(ActionEvent) listener) {
        retval.add(item);
        item.addActionListener(listener);
    }
    if (is HasMutableName fixture) {
        addMenuItem(JMenuItem("Rename", KeyEvent.vkN), (ActionEvent event) {
            String originalName = fixture.name;
            if (exists result = JOptionPane.showInputDialog(retval,
                "Fixture's new name:", "Rename Fixture",
                JOptionPane.plainMessage, null, null, javaString(originalName))) {
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
        immutable = false;
    }
    if (is HasMutableKind fixture) {
        addMenuItem(JMenuItem("Change kind", KeyEvent.vkK), (ActionEvent event) {
            String originalKind = fixture.kind;
            if (exists result = JOptionPane.showInputDialog(retval,
                "Fixture's new kind:", "Change Fixture Kind",
                JOptionPane.plainMessage, null, null, javaString(originalKind))) {
                String resultString = result.string.trimmed;
                if (resultString != originalKind.trimmed) {
                    HasMutableKind temp = fixture;
                    temp.kind = resultString;
                    for (listener in changeListeners) {
                        listener.moveItem(fixture);
                    }
                }
            }
        });
        immutable = false;
    }
    if (is HasMutableOwner fixture) {
        addMenuItem(JMenuItem("Change owner", KeyEvent.vkO), (ActionEvent event) {
            if (is Player player = JOptionPane.showInputDialog(retval,
                "Fixture's new owner:", "Change Fixture Owner",
                JOptionPane.plainMessage, null, createJavaObjectArray(players),
                fixture.owner)) {
                HasMutableOwner temp = fixture;
                temp.owner = player;
            }
        });
        immutable = false;
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
    }
    if (immutable) {
        retval.add(JLabel("Fixture is not mutable"));
    }
    return retval;
}
