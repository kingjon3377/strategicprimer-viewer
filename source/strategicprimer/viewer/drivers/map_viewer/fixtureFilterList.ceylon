import ceylon.language.meta {
    type
}

import java.awt {
    Component
}

import javax.swing {
    JLabel,
    SwingList=JList,
    DefaultListModel,
    DefaultListCellRenderer,
    ListSelectionModel,
    ListCellRenderer,
    DropMode
}

import lovelace.util.jvm {
    ReorderableListModel
}

import strategicprimer.model.map {
    TileFixture
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow
}
import strategicprimer.drivers.common {
	FixtureMatcher
}
import lovelace.util.common {
	inverse,
	silentListener
}
"A list to let the user select which fixtures ought to be searched."
class FixtureFilterList() extends SwingList<FixtureMatcher>() satisfies ZOrderFilter {
	DefaultListModel<FixtureMatcher> matcherListModel =
			ReorderableListModel<FixtureMatcher>(
				FixtureMatcher.simpleMatcher<Ground>(Ground.exposed, "Ground (exposed)"),
				FixtureMatcher.simpleMatcher<Ground>(inverse(Ground.exposed), "Ground"),
				FixtureMatcher.simpleMatcher<Grove>(Grove.orchard, "Orchards"),
				FixtureMatcher.simpleMatcher<Grove>(inverse(Grove.orchard), "Groves"),
				FixtureMatcher.simpleMatcher<Meadow>(Meadow.field, "Fields"),
				FixtureMatcher.simpleMatcher<Meadow>(inverse(Meadow.field), "Meadows"));
	shared actual Boolean shouldDisplay(TileFixture fixture) {
		for (i in 0:matcherListModel.size) {
			FixtureMatcher matcher = matcherListModel.getElementAt(i);
			if (matcher.matches(fixture)) {
				return matcher.displayed;
			}
		}
		value cls = type(fixture);
		matcherListModel.addElement(
			FixtureMatcher(cls.typeOf, fixture.plural));
		Integer size = matcherListModel.size;
		selectionModel.addSelectionInterval(size - 1, size - 1);
		return true;
	}
	model = matcherListModel;
	selectionModel.selectionMode = ListSelectionModel.multipleIntervalSelection;
	void selectionChanged() {
		for (i in 0:matcherListModel.size) {
			matcherListModel.getElementAt(i).displayed =
					selectionModel.isSelectedIndex(i);
		}
	}
	selectionModel.addListSelectionListener(silentListener(selectionChanged));
	DefaultListCellRenderer defaultRenderer = DefaultListCellRenderer();
	cellRenderer = object satisfies ListCellRenderer<FixtureMatcher> {
		shared actual Component getListCellRendererComponent(
				SwingList<out FixtureMatcher> list, FixtureMatcher item,
				Integer index, Boolean isSelected, Boolean cellHasFocus) {
			value retval = defaultRenderer.getListCellRendererComponent(list, item,
				index, isSelected, cellHasFocus);
			if (is JLabel retval) {
				retval.text = item.description;
			}
			return retval;
		}
	};
	transferHandler = fixtureFilterTransferHandler;
	dropMode = DropMode.insert;
	dragEnabled = true;
}
