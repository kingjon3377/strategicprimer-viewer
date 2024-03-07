package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;
import drivers.common.IFixtureEditingModel;
import drivers.common.SelectionChangeSource;

/**
 * An interface for a model behind the map viewer, handling the selected tile and visible dimensions.
 */
public interface IViewerModel extends SelectionChangeSource, GraphicalParamsSource,
		IFixtureEditingModel {
	/**
	 * The coordinates of the currently selected tile.
	 */
	Point getSelection();

	/**
	 * Set the coordinates of the currently selected tile.
	 */
	void setSelection(Point selection);

	/**
	 * The coordinates of the tile currently pointed to by the scroll-bars.
	 */
	Point getCursor();

	/**
	 * Set the coordinates of the tile currently pointed to by the scroll-bars.
	 */
	void setCursor(Point cursor);

	/**
	 * The coordinates of the tile the user is currently interacting with,
	 * if any. This should be set when the user right-clicks (or
	 * equivalent) on a tile, and unset at the end of the operation
	 * handling that click.
	 */
	@Nullable Point getInteraction();

	/**
	 * Set the coordinates of the tile the user is currently interacting with,
	 * if any. This should be set when the user right-clicks (or
	 * equivalent) on a tile, and unset at the end of the operation
	 * handling that click.
	 */
	void setInteraction(@Nullable Point interaction);

	/**
	 * The visible dimensions of the map.
	 */
	VisibleDimensions getVisibleDimensions();

	/**
	 * Set the visible dimensions of the map.
	 */
	void setVisibleDimensions(VisibleDimensions visibleDimensions);

	/**
	 * The current zoom level.
	 */
	int getZoomLevel();

	/**
	 * Zoom in.
	 */
	void zoomIn();

	/**
	 * Zoom out.
	 */
	void zoomOut();

	/**
	 * Reset the zoom level to the default.
	 */
	void resetZoom();

	/**
	 * Set whether a tile is mountainous.
	 */
	void setMountainous(Point location, boolean mountainous);

	/**
	 * Add a fixture to the map at a point.
	 */
	void addFixture(Point location, TileFixture fixture);

	/**
	 * Remove a fixture from the map at a point.
	 */
	void removeMatchingFixtures(Point location, Predicate<TileFixture> condition);

	/**
	 * Add a bookmark at the given location.
	 */
	void addBookmark(Point location);

	/**
	 * Remove a bookmark at the current location.
	 */
	void removeBookmark(Point location);

	/**
	 * Add a river at a location.
	 */
	void addRiver(Point location, River river);

	/**
	 * Remove a river at a location.
	 */
	void removeRiver(Point location, River river);

	/**
	 * Set the map's terrain type at the given point.
	 */
	void setBaseTerrain(Point location, @Nullable TileType terrain);
}
