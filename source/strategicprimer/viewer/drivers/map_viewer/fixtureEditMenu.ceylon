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
import lovelace.util.common {
	silentListener
}
"A pop-up menu to let the user edit a fixture."
shared class FixtureEditMenu(IFixture fixture, {Player*} players, IDRegistrar idf,
		IWorkerTreeModel* changeListeners) extends JPopupMenu() {
	void addMenuItem(JMenuItem item, Anything(ActionEvent) listener) {
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
			}
		}
	}
	if (is HasMutableKind fixture) {
		addMenuItem(JMenuItem("Change kind", KeyEvent.vkK), silentListener(changeKindHandler));
	} else {
		addDisabledMenuItem(JMenuItem("Change kind", KeyEvent.vkK));
	}
	void changeOwnerHandler() {
		assert (is HasMutableOwner fixture);
		if (is Player player = JOptionPane.showInputDialog(parent,
			"Fixture's new owner:", "Change Fixture Owner",
			JOptionPane.plainMessage, null, ObjectArray.with(players),
			fixture.owner)) {
			HasMutableOwner temp = fixture;
			temp.owner = player;
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
		if (is HasName fixture) {
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
		}
	}
	if (is UnitMember fixture) {
		addMenuItem(JMenuItem("Dismiss", KeyEvent.vkD), silentListener(dismissHandler));
	} else {
		addDisabledMenuItem(JMenuItem("Dismiss", KeyEvent.vkD));
	}
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
		}
	}
	// TODO: Generalize splitting to HasPopulation more generally
	if (is Animal fixture, fixture.population > 1) {
		addMenuItem(JMenuItem("Split animal population", KeyEvent.vkS),
			silentListener(splitAnimalHandler));
	} else {
		addDisabledMenuItem(JMenuItem("Split animal population", KeyEvent.vkS));
	}
}
