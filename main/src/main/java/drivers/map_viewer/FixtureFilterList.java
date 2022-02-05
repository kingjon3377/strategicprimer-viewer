package drivers.map_viewer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.DropMode;

import lovelace.util.ReorderableListModel;

import common.map.TileFixture;
import common.map.fixtures.Ground;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import drivers.common.FixtureMatcher;
import java.util.function.Predicate;

/**
 * A list to let the user select which fixtures ought to be searched.
 */
/* package */ class FixtureFilterList extends JList<FixtureMatcher> implements ZOrderFilter {
	public FixtureFilterList() {
		matcherListModel = new ReorderableListModel<FixtureMatcher>(
				FixtureMatcher.simpleMatcher(Ground.class, Ground::isExposed,
					"Ground (exposed)"),
				FixtureMatcher.simpleMatcher(Ground.class, not(Ground::isExposed), "Ground"),
				FixtureMatcher.simpleMatcher(Grove.class, Grove::isOrchard, "Orchards"),
				FixtureMatcher.simpleMatcher(Grove.class, not(Grove::isOrchard), "Groves"),
				FixtureMatcher.simpleMatcher(Meadow.class, Meadow::isField, "Fields"),
				FixtureMatcher.simpleMatcher(Meadow.class, not(Meadow::isField), "Meadows"));
		setModel(matcherListModel);
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getSelectionModel().addListSelectionListener(ignored -> selectionChanged());
		setCellRenderer(this::renderCell);

		setTransferHandler(new FixtureFilterTransferHandler());
		setDropMode(DropMode.INSERT);
		setDragEnabled(true);
	}

	private static final <T> Predicate<T> not(final Predicate<T> p) {
		return t -> !p.test(t);
	}

	private final DefaultListModel<FixtureMatcher> matcherListModel;

	@Override
	public boolean shouldDisplay(final TileFixture fixture) {
		for (int i = 0; i < matcherListModel.getSize(); i++) {
			FixtureMatcher matcher = matcherListModel.getElementAt(i);
			if (matcher.matches(fixture)) {
				return matcher.isDisplayed();
			}
		}
		Class<?> cls = fixture.getClass();
		matcherListModel.addElement(new FixtureMatcher(cls::isInstance, fixture.getPlural()));
		int size = matcherListModel.getSize();
		getSelectionModel().addSelectionInterval(size - 1, size - 1);
		return true;
	}

	private void selectionChanged() {
		for (int i = 0; i < matcherListModel.getSize(); i++) {
			matcherListModel.getElementAt(i).setDisplayed(
				getSelectionModel().isSelectedIndex(i));
		}
	}

	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	private Component renderCell(final JList<? extends FixtureMatcher> list, final FixtureMatcher item,
	                             final int index, final boolean isSelected, final boolean cellHasFocus) {
		Component retval = DEFAULT_RENDERER.getListCellRendererComponent(list, item,
			index, isSelected, cellHasFocus);
		if (retval instanceof JLabel) {
			((JLabel) retval).setText(item.getDescription());
		}
		return retval;
	}
}
