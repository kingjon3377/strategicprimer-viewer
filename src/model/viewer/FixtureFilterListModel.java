package model.viewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import model.map.TileFixture;

/**
 * The data model for a FixtureFilterList.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureFilterListModel extends
		AbstractListModel<Class<? extends TileFixture>> {
	/**
	 * The list backing this model.
	 */
	private final List<Class<? extends TileFixture>> backing = new ArrayList<>();

	/**
	 * @return the size of the list
	 */
	@Override
	public int getSize() {
		return backing.size();
	}

	/**
	 * @param index an index in the list
	 * @return the element there
	 */
	@Override
	public Class<? extends TileFixture> getElementAt(final int index) {
		final Class<? extends TileFixture> retval = backing.get(index);
		assert retval != null;
		return retval;
	}

	/**
	 * @param item an item in the list
	 * @return its index
	 */
	public int indexOf(final Class<? extends TileFixture> item) {
		return backing.indexOf(item);
	}

	/**
	 * @param item an item to add to the list
	 */
	public void add(final Class<? extends TileFixture> item) {
		backing.add(item);
		fireIntervalAdded(item.getClass(), getSize() - 1, getSize() - 1);
	}
}
