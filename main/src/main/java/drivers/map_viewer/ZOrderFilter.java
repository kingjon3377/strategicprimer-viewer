package drivers.map_viewer;

import common.map.TileFixture;

/**
 * An interface for a filter to tell whether a given fixture should be displayed.
 */
@FunctionalInterface
public interface ZOrderFilter {
	/**
	 * Whether the fixture should be displayed.
	 */
	boolean shouldDisplay(TileFixture fixture);
}
