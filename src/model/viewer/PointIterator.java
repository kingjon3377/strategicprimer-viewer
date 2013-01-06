package model.viewer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import model.map.Point;
import model.map.PointFactory;
/**
 * An view of locations on the map in order, starting at a given point.
 * @author Jonathan Lovelace
 *
 */
public class PointIterator implements Iterator<Point> {
	/**
	 * The maximum row in the map.
	 */
	private final int maxRow;
	/**
	 * The maximum column in the map.
	 */
	private final int maxCol;
	/**
	 * The row where we started.
	 */
	private final int startRow;
	/**
	 * The column where we started.
	 */
	private final int startCol;
	/**
	 * The current row.
	 */
	private int row;
	/**
	 * The current column.
	 */
	private int col;
	/**
	 * Whether we've started iterating.
	 */
	private boolean started = false;
	/**
	 * @param val a value
	 * @param wrap another
	 * @return the first, unless it's negative, in which case the second
	 */
	private static int wrap(final int val, final int wrap) {
		if (val < 0) {
			return wrap; // NOPMD
		} else {
			return val;
		}
	}
	/**
	 * Constructor.
	 *
	 * @param model the map we're helping to go through.
	 * @param startFromSel If true, we start from (the tile after) the
	 *        current selected tile; if false, we start from 0, 0.
	 */
	public PointIterator(final MapModel model, final boolean startFromSel) {
		maxRow = model.getMapDimensions().getRows() - 1;
		maxCol = model.getMapDimensions().getColumns() - 1;
		if (startFromSel) {
			startRow = wrap(model.getSelectedTile().getLocation().row, maxRow);
			startCol = wrap(model.getSelectedTile().getLocation().col, maxCol);
		} else {
			startRow = maxRow;
			startCol = maxCol;
		}
		row = startRow;
		col = startCol;
	}
	/**
	 * @return false if we've reached where we started.
	 */
	@Override
	public boolean hasNext() {
		if (started) {
			return row != startRow || col != startCol; // NOPMD
		} else {
			return true; // NOPMD
		}
	}
	/**
	 * @return the next point in the map.
	 */
	@Override
	public Point next() {
		if (hasNext()) {
			started = true;
			if (col == maxCol) {
				if (row == maxRow) {
					row = 0;
				} else {
					row++;
				}
				col = 0;
			} else {
				col++;
			}
			return PointFactory.point(row, col);
		} else {
			throw new NoSuchElementException("We've reached the end");
		}
	}
	/**
	 * Not implemented.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Can't remove a Point from a map.");
	}
}
