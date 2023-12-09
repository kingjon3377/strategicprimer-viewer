package drivers.map_viewer;

import legacy.map.MapDimensions;

import javax.swing.JScrollBar;
import javax.swing.JComponent;
import javax.swing.InputVerifier;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import lovelace.util.LovelaceLogger;

/**
 * A class, formerly static within {@link ScrollListener}, to verify that
 * scroll inputs are within the valid range.
 */
/* package */ final class ScrollInputVerifier extends InputVerifier {
    private final IntSupplier mapDimension;
    private final String dimension;

    private ScrollInputVerifier(final IntSupplier mapDimension, final String dimension) {
        this.mapDimension = mapDimension;
        this.dimension = dimension;
    }

    public static ScrollInputVerifier horizontal(final Supplier<MapDimensions> mapDimsSource) {
        return new ScrollInputVerifier(() -> mapDimsSource.get().columns(), "horizontal");
    }

    public static ScrollInputVerifier vertical(final Supplier<MapDimensions> mapDimsSource) {
        return new ScrollInputVerifier(() -> mapDimsSource.get().rows(), "vertical");
    }

    /**
     * A scrollbar is valid if its value is between 0 and the size of the map.
     */
    @Override
    public boolean verify(final JComponent input) {
        if (input instanceof final JScrollBar jsb) {
            if (jsb.getValue() >= 0 && jsb.getValue() < mapDimension.getAsInt()) {
                LovelaceLogger.debug("%d is a valid %s coordinate", jsb.getValue(), dimension);
                return true;
            } else {
                LovelaceLogger.debug("%d is not a valid %s coordinate", jsb.getValue(), dimension);
                return false;
            }
        } else {
            LovelaceLogger.debug("ScrollInputVerifier called on non-scroll-bar input");
            return false;
        }
    }
}
