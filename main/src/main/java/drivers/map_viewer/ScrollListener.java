package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Adjustable;

import common.map.MapDimensions;
import common.map.Point;

import lovelace.util.BorderedPanel;

import drivers.common.MapChangeListener;
import drivers.common.SelectionChangeListener;

import javax.swing.JScrollBar;
import javax.swing.BoundedRangeModel;

import common.map.fixtures.mobile.IUnit;

/**
 * A class to change the visible area of the map based on the user's use of the scrollbars.
 */
/* package */ class ScrollListener implements MapChangeListener, SelectionChangeListener,
		GraphicalParamsListener {
	private static int constrainToRange(final int val, final int min, final int max) {
		if (val < min) {
			return min;
		} else {
			return Math.min(val, max);
		}
	}

	private final IViewerModel model;
	private final BoundedRangeModel horizontalBarModel;
	private final BoundedRangeModel verticalBarModel;
	private MapDimensions mapDimensions;
	private VisibleDimensions visibleDimensions;

	// Can't take scroll-bar models instead of scroll bars because we have
	// to set up input verifiers as well.
	public ScrollListener(final IViewerModel mapModel, final JScrollBar horizontal, final JScrollBar vertical) {
		model = mapModel;
		visibleDimensions = mapModel.getVisibleDimensions();
		mapDimensions = mapModel.getMapDimensions();
		Point cursor = mapModel.getCursor();
		horizontalBarModel = horizontal.getModel();
		horizontalBarModel.setRangeProperties(constrainToRange(cursor.getColumn(), 0,
				mapDimensions.getColumns() - 1),
			Math.min(mapDimensions.getColumns(), visibleDimensions.getWidth()), 0,
			mapDimensions.getColumns(), false);
		horizontal.setInputVerifier(
			ScrollInputVerifier.horizontal(mapModel::getMapDimensions));
		verticalBarModel = vertical.getModel();
		verticalBarModel.setRangeProperties(constrainToRange(cursor.getRow(), 0,
				mapDimensions.getRows() - 1),
			Math.min(mapDimensions.getRows(), visibleDimensions.getHeight()), 0,
			mapDimensions.getRows(), false);
		vertical.setInputVerifier(ScrollInputVerifier.vertical(
				mapModel::getMapDimensions));

		ScrollAdjustmentListener adjustmentListener = new ScrollAdjustmentListener(model);

		horizontalBarModel.addChangeListener(adjustmentListener::horizontalScroll);
		verticalBarModel.addChangeListener(adjustmentListener::verticalScroll);
	}

	/**
	 * Alternate constructor that adds (new) scroll-bars to an existing
	 * component. This only works if that component is laid out using a
	 * {@link BorderLayout} and doesn't already have members at page-end and line-end.
	 */
	private ScrollListener(final IViewerModel mapModel, final BorderedPanel component, final JScrollBar horizontalBar,
	                       final JScrollBar verticalBar) {
		this(mapModel, horizontalBar, verticalBar);
		if (component.getPageEnd() != null || component.getLineEnd() != null) {
			throw new IllegalArgumentException(
				"We don't want to replace existing components with scrollbars");
		}
		component.setPageEnd(horizontalBar);
		component.setLineEnd(verticalBar);
	}

	public static ScrollListener createScrollBars(final IViewerModel mapModel, final BorderedPanel component) {
		return new ScrollListener(mapModel, component, new JScrollBar(Adjustable.HORIZONTAL),
			new JScrollBar(Adjustable.VERTICAL));
	}

	private int countChanges(final BoundedRangeModel model, final int val, final int extent,
	                         final int minimum, final int maximum) {
		int retval = 0;
		if (model.getValue() != val) {
			retval++;
		}
		if (model.getMinimum() != minimum) {
			retval++;
		}
		if (model.getExtent() != extent) {
			retval++;
		}
		if (model.getMaximum() != maximum) {
			retval++;
		}
		return retval;
	}

	private void setRangeProperties(final BoundedRangeModel model, final int val, final int extent,
	                                final int minimum, final int maximum) {
		int differences = countChanges(model, val, minimum, extent, maximum);
		if (differences <= 0) {
			return;
		} else if (differences < 3) {
			// TODO: Set ScrollAdjustmentListener.adjusting?
			boolean oldVIA = model.getValueIsAdjusting();
			model.setValueIsAdjusting(true);
			if (model.getValue() != val) {
				model.setValue(val);
			}
			if (model.getMinimum() != minimum) {
				model.setMinimum(minimum);
			}
			if (model.getMaximum() != maximum) {
				model.setMaximum(maximum);
			}
			if (model.getExtent() != extent) {
				model.setExtent(extent);
			}
			model.setValueIsAdjusting(oldVIA);
		} else {
			model.setRangeProperties(val, extent, minimum, maximum, false);
		}
	}

	private boolean mutex = true;

	/**
	 * Handle a change in visible dimensions.
	 */
	@Override
	public void dimensionsChanged(final VisibleDimensions oldDimensions, final VisibleDimensions newDimensions) {
		if (mutex) {
			mutex = false;
			visibleDimensions = newDimensions;
			setRangeProperties(horizontalBarModel, Math.max(model.getCursor().getColumn(), 0),
				Math.min(newDimensions.getWidth(), mapDimensions.getColumns()),
				0, mapDimensions.getColumns());
			setRangeProperties(verticalBarModel, Math.max(model.getCursor().getRow(), 0),
				Math.min(newDimensions.getHeight(), mapDimensions.getRows()), 0,
				mapDimensions.getRows());
			mutex = true;
		}
	}

	/**
	 * Ignored; other listeners will adjust the dimensions, causing {@link
	 * dimensionsChanged} to be called.
	 */
	@Override
	public void tileSizeChanged(final int oldSize, final int newSize) { }

	/**
	 * Handle a change to the cursor location.
	 */
	@Override
	public void cursorPointChanged(@Nullable final Point previous, final Point newCursor) {
		VisibleDimensions temp = model.getVisibleDimensions();
		if (!temp.getColumns().contains(newCursor.getColumn()) &&
				horizontalBarModel.getValue() != Math.max(newCursor.getColumn(), 0)) {
			horizontalBarModel.setValue(Math.max(newCursor.getColumn(), 0));
		}
		if (!temp.getRows().contains(newCursor.getRow()) &&
				verticalBarModel.getValue() != Math.max(newCursor.getRow(), 0)) {
			verticalBarModel.setValue(Math.max(newCursor.getRow(), 0));
		}
	}

	/**
	 * Scrolling deals only with the cursor location, not with the selection.
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point previousSelection, final Point newSelection) {}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		mapDimensions = model.getMapDimensions();
		visibleDimensions = model.getVisibleDimensions();
		horizontalBarModel.setRangeProperties(0,
			Math.min(visibleDimensions.getWidth(), mapDimensions.getColumns()), 0,
			mapDimensions.getColumns(), false);
		verticalBarModel.setRangeProperties(0,
			Math.min(visibleDimensions.getHeight(), mapDimensions.getRows()), 0,
			mapDimensions.getRows(), false);
	}

	@Override
	public void mapMetadataChanged() {}

	@Override
	public void selectedUnitChanged(@Nullable final IUnit oldSelection, @Nullable final IUnit newSelection) {}

	@Override
	public void interactionPointChanged() {}
}
