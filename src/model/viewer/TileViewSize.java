package model.viewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import model.map.IMap;
import util.PropertyChangeSource;

/**
 * A class to encapsulate how big the GUI representation of a tile should be.
 * Now suppoting zooming in and out (changing the size to view more tiles or see
 * the tiles more clearly, not changing what's on them yet). TODO: Even better
 * zoom support.
 *
 * @author Jonathan Lovelace
 *
 */
public class TileViewSize implements PropertyChangeSource, Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param version the map version we're being asked about
	 * @return how big each tile should be
	 */
	public static final int getDefaultSize(final int version) {
		if (version == 1) {
			return 16; // NOPMD
		} else if (version == 2) {
			return 24;
		} else {
			throw new IllegalArgumentException("Unknown version");
		}
	}
	/**
	 * The current size.
	 */
	private int size;
	/**
	 * @return the current size of a tile
	 */
	public int getSize() {
		return size;
	}
	/**
	 * Constructor.
	 * @param map the map we're dealing with
	 */
	public TileViewSize(final IMap map) {
		size = getDefaultSize(map.getDimensions().getVersion());
	}
	/**
	 * Reset---effectively construct again.
	 * @param map the map we're now dealing with.
	 */
	public void reset(final IMap map) {
		final int oldSize = size;
		size = getDefaultSize(map.getDimensions().getVersion());
		pcs.firePropertyChange("tsize", oldSize, size);
	}

	/**
	 * These are needed because checking these conditions in a loop condition
	 * makes static analysis think size is a loop variable. Checking them in one
	 * method makes it possible to get into an invalid situation and not be able
	 * to correct it by zooming the other way.
	 */
	/**
	 * Check that size isn't too big.
	 * @return true if size is valid, false otherwise
	 */
	private boolean isSizeValidTop() {
		return size < Integer.MAX_VALUE - 1;
	}
	/**
	 * Check that the size isn't too small.
	 * Remember that this is including the border lines.
	 * @return true if size is valid, false otherwise
	 */
	private boolean isSizeValidBottom() {
		return size >= 8;
	}
	/**
	 * Increase the size of a tile (zooming in).
	 * @param count how much the user wants to increase
	 */
	public void increase(final int count) {
		final int oldSize = size;
		for (int i = 0; i < count && isSizeValidTop(); i++) {
			size++;
		}
		pcs.firePropertyChange("tsize", oldSize, size);
	}
	/**
	 * Decrease the size of a tile (zooming out).
	 * @param count how much the user wants to decrease
	 */
	public void decrease(final int count) {
		final int oldSize = size;
		for (int i = 0; i < count && isSizeValidBottom(); i++) {
			size--;
		}
		pcs.firePropertyChange("tsize", oldSize, size);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileViewSize";
	}
	/**
	 * An object to handle property changes for us.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * Add a listener.
	 * @param list the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}
	/**
	 * Remove a listener.
	 * @param list the listener to remove
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
}
