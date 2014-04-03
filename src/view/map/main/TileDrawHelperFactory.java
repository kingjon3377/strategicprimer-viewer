package view.map.main;

import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

import model.viewer.ZOrderFilter;
import util.NullCleaner;

/**
 * A factory for TileDrawHelpers.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TileDrawHelperFactory {
	/**
	 * An instance of this class, for callers who don't want to create their
	 * own.
	 */
	public static final TileDrawHelperFactory INSTANCE = new TileDrawHelperFactory();
	/**
	 * A version-1 tile draw helper.
	 */
	private final TileDrawHelper verOneHelper = new DirectTileDrawHelper();
	/**
	 * A mapping from ImageObservers to version-2 helpers.
	 */
	private final Map<ImageObserver, TileDrawHelper> verTwoHelpers = new HashMap<>();

	/**
	 * Constructor.
	 */
	private TileDrawHelperFactory() {
		// Do nothing.
	}

	/**
	 * @param version the version of the map that'll be drawn
	 * @param iobs the object to be notified when images finish drawing
	 * @param zof a filter to tell a ver-two helper which tiles to draw
	 * @return a draw helper for the specified map version
	 */
	public TileDrawHelper factory(final int version, final ImageObserver iobs,
			final ZOrderFilter zof) {
		switch (version) {
		case 1:
			return verOneHelper; // NOPMD
		case 2:
			if (!verTwoHelpers.containsKey(iobs)) {
				verTwoHelpers.put(iobs, new Ver2TileDrawHelper(iobs, zof));
			}
			return NullCleaner.assertNotNull(verTwoHelpers.get(iobs));
		default:
			throw new IllegalArgumentException("Unsupported map version");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileDrawHelperFactory";
	}
}
