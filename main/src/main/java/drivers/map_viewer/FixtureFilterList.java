package drivers.map_viewer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.DropMode;

import legacy.map.fixtures.resources.ExposureStatus;
import lovelace.util.ReorderableListModel;

import legacy.map.TileFixture;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import drivers.common.FixtureMatcher;

import java.io.Serial;
import java.util.function.Predicate;

/**
 * A list to let the user select which fixtures ought to be searched.
 */
/* package */ final class FixtureFilterList extends JList<FixtureMatcher> implements ZOrderFilter {
	@Serial
	private static final long serialVersionUID = 1L;

	public FixtureFilterList() {
		matcherListModel = new ReorderableListModel<>(
				FixtureMatcher.simpleMatcher(Ground.class, g -> g.getExposure() == ExposureStatus.EXPOSED,
						"Ground (exposed)"),
				FixtureMatcher.simpleMatcher(Ground.class, g -> g.getExposure() == ExposureStatus.HIDDEN, "Ground"),
				FixtureMatcher.simpleMatcher(Grove.class, g -> g.getType() == Grove.GroveType.ORCHARD, "Orchards"),
				FixtureMatcher.simpleMatcher(Grove.class, g -> g.getType() == Grove.GroveType.GROVE, "Groves"),
				FixtureMatcher.simpleMatcher(Meadow.class, m -> m.getType() == Meadow.MeadowType.FIELD, "Fields"),
				FixtureMatcher.simpleMatcher(Meadow.class, m -> m.getType() == Meadow.MeadowType.MEADOW, "Meadows"));
		setModel(matcherListModel);
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getSelectionModel().addListSelectionListener(ignored -> selectionChanged());
		setCellRenderer(FixtureFilterList::renderCell);

		setTransferHandler(new FixtureFilterTransferHandler());
		setDropMode(DropMode.INSERT);
		setDragEnabled(true);
	}

	private static <T> Predicate<T> not(final Predicate<T> p) {
		return t -> !p.test(t);
	}

	private final DefaultListModel<FixtureMatcher> matcherListModel;

	@Override
	public boolean shouldDisplay(final TileFixture fixture) {
		for (int i = 0; i < matcherListModel.getSize(); i++) {
			final FixtureMatcher matcher = matcherListModel.getElementAt(i);
			if (matcher.matches(fixture)) {
				return matcher.isDisplayed();
			}
		}
		final Class<?> cls = fixture.getClass();
		matcherListModel.addElement(new FixtureMatcher(cls::isInstance, fixture.getPlural()));
		final int size = matcherListModel.getSize();
		getSelectionModel().addSelectionInterval(size - 1, size - 1);
		return true;
	}

	private void selectionChanged() {
		for (int i = 0; i < matcherListModel.getSize(); i++) {
			matcherListModel.getElementAt(i).setDisplayed(
					getSelectionModel().isSelectedIndex(i));
		}
	}

	private static final ListCellRenderer<Object> DEFAULT_RENDERER = new DefaultListCellRenderer();

	private static Component renderCell(final JList<? extends FixtureMatcher> list, final FixtureMatcher item,
										final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component retval = DEFAULT_RENDERER.getListCellRendererComponent(list, item,
				index, isSelected, cellHasFocus);
		if (retval instanceof final JLabel label) {
			label.setText(item.getDescription());
		}
		return retval;
	}
}
