package view.map.main;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import model.map.TileFixture;
import model.viewer.FixtureFilterListModel;
import model.viewer.ZOrderFilter;

/**
 * A list to let the user select which fixtures ought to be searched.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureFilterList extends JList<Class<? extends TileFixture>>
		implements ZOrderFilter, ListCellRenderer<Class<? extends TileFixture>> {
	/**
	 * A mapping from classes of fixtures to their plurals.
	 */
	private final Map<Class<? extends TileFixture>, String> plurals = new HashMap<>();
	/**
	 * The selection model.
	 */
	private final ListSelectionModel lsm;
	/**
	 * The data model.
	 */
	private final FixtureFilterListModel model;

	/**
	 * Constructor.
	 */
	public FixtureFilterList() {
		super();
		model = new FixtureFilterListModel();
		setModel(model);
		lsm = getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellRenderer(this);
	}

	/**
	 * @param fix a fixture
	 * @return whether it should be searched
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		final Class<? extends TileFixture> cls = fix.getClass();
		if (cls == null) {
			return false;
		} else if (plurals.containsKey(cls)) {
			return lsm.isSelectedIndex(model.indexOf(cls));
		} else {
			model.add(cls);
			plurals.put(cls, fix.plural());
			final int size = model.getSize();
			lsm.addSelectionInterval(size - 1, size - 1);
			return true;
		}
	}

	/**
	 * The renderer that does most of the work.
	 */
	private final ListCellRenderer<Object> lcr = new DefaultListCellRenderer();

	/**
	 *
	 * @param list this
	 * @param value the value being rendered
	 * @param index its index
	 * @param isSelected whether or not it's selected
	 * @param cellHasFocus whether or not it has the focus
	 * @return the rendered widget
	 */
	@Override
	public Component getListCellRendererComponent(
			final JList<? extends Class<? extends TileFixture>> list,
			final Class<? extends TileFixture> value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		final Component retval = lcr.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		if (retval instanceof JLabel) {
			((JLabel) retval).setText(plurals.get(value));
		} else if (retval == null) {
			throw new IllegalStateException("Default produced null");
		}
		return retval;
	}

}
