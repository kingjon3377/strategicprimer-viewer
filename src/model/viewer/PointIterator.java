package model.viewer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import model.map.Point;
import model.map.PointFactory;
import model.misc.IDriverModel;
/**
 * An view of locations on the map in order, starting at a given point.
 * @author Jonathan Lovelace
 *
 */
public class PointIterator implements Iterator<Point> {
	/**
	 * Whether we're searching forwards (if true) or backwards (if false).
	 */
	private final boolean forwards;
	/**
	 * Whether we're searching horizontally (if true) or vertically (if false).
	 */
	private final boolean horiz;
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
	 * @param startFromSel If true, we start from (the tile after) the current
	 *        selected tile; if false, we start from 0, 0.
	 * @param searchForwards Whether we should search forwards (if true) or
	 *        backwards (if false)
	 * @param searchHoriz Whether we should search horizontally (if true) or
	 *        vertically (if false)
	 */
	public PointIterator(final IDriverModel model, final boolean startFromSel,
			final boolean searchForwards, final boolean searchHoriz) {
		horiz = searchHoriz;
		forwards = searchForwards;
		maxRow = model.getMapDimensions().getRows() - 1;
		maxCol = model.getMapDimensions().getColumns() - 1;
		if (startFromSel && model instanceof IViewerModel) {
			startRow = wrap(((IViewerModel) model).getSelectedPoint().row, maxRow);
			startCol = wrap(((IViewerModel) model).getSelectedPoint().col, maxCol);
		} else if (forwards) {
			startRow = maxRow;
			startCol = maxCol;
		} else {
			startRow = 0;
			startCol = 0;
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
			if (horiz) {
				if (forwards) {
					return horizNext(); // NOPMD
				} else {
					return horizPrev(); // NOPMD
				}
			} else {
				if (forwards) {
					return vertNext(); // NOPMD
				} else {
					return vertPrev();
				}
			}
		} else {
			throw new NoSuchElementException("We've reached the end");
		}
	}
	/**
	 * @return the next point, searching horizontally.
	 */
	private Point horizNext() {
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
	}
	/**
	 * @return the previous point, searching horizontally.
	 */
	private Point horizPrev() {
		if (col == 0) {
			if (row == 0) {
				row = maxRow;
			} else {
				row--;
			}
			col = maxCol;
		} else {
			col--;
		}
		return PointFactory.point(row, col);
	}
	/**
	 * @return the next point, searching vertically.
	 */
	private Point vertNext() {
		if (row == maxRow) {
			if (col == maxCol) {
				col = 0;
			} else {
				col++;
			}
			row = 0;
		} else {
			row++;
		}
		return PointFactory.point(row, col);
	}
	/**
	 * @return the previous point, searching vertically.
	 */
	private Point vertPrev() {
		if (row == 0) {
			if (col == 0) {
				col = maxCol;
			} else {
				col--;
			}
			row = maxRow;
		} else {
			row--;
		}
		return PointFactory.point(row, col);
	}
	/**
	 * Not implemented.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Can't remove a Point from a map.");
	}
}
