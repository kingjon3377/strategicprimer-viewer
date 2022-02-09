package drivers.map_viewer;

import common.map.MapDimensions;

import javax.swing.JScrollBar;
import javax.swing.JComponent;
import javax.swing.InputVerifier;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import java.util.logging.Logger;

/**
 * A class, formerly static within {@link ScrollListener}, to verify that
 * scroll inputs are within the valid range.
 */
/* package */ class ScrollInputVerifier extends InputVerifier {
	private static final Logger LOGGER = Logger.getLogger(ScrollInputVerifier.class.getName());
	private final IntSupplier mapDimension;
	private final String dimension;

	private ScrollInputVerifier(final IntSupplier mapDimension, final String dimension) {
		this.mapDimension = mapDimension;
		this.dimension = dimension;
	}

	public static ScrollInputVerifier horizontal(final Supplier<MapDimensions> mapDimsSource) {
		return new ScrollInputVerifier(() -> mapDimsSource.get().getColumns(), "horizontal");
	}

	public static ScrollInputVerifier vertical(final Supplier<MapDimensions> mapDimsSource) {
		return new ScrollInputVerifier(() -> mapDimsSource.get().getRows(), "vertical");
	}

	/**
	 * A scrollbar is valid if its value is between 0 and the size of the map.
	 */
	@Override
	public boolean verify(final JComponent input) {
		if (input instanceof JScrollBar) {
			final JScrollBar jsb = (JScrollBar) input;
			if (jsb.getValue() >= 0 && jsb.getValue() < mapDimension.getAsInt()) {
				LOGGER.fine(String.format("%d is a valid %s coordinate",
					jsb.getValue(), dimension));
				return true;
			} else {
				LOGGER.fine(String.format("%d is not a valid %s coordinate",
					jsb.getValue(), dimension));
				return false;
			}
		} else {
			LOGGER.fine("ScrollInputVerifier called on non-scroll-bar input");
			return false;
		}
	}
}
