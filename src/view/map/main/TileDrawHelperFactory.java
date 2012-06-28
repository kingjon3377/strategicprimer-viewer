package view.map.main;

import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for TileDrawHelpers.
 * @author Jonathan Lovelace
 *
 */
public class TileDrawHelperFactory {
	/**
	 * An instance of this class, for callers who don't want to create their own.
	 */
	public static final TileDrawHelperFactory INSTANCE = new TileDrawHelperFactory();
	/**
	 * A version-1 tile draw helper.
	 */
	private final TileDrawHelper verOneHelper = new DirectTileDrawHelper();
	/**
	 * A mapping from ImageObservers to version-2 helpers.
	 */
	private final Map<ImageObserver, TileDrawHelper> verTwoHelpers = new HashMap<ImageObserver, TileDrawHelper>();
	/**
	 * @param version the version of the map that'll be drawn
	 * @param iobs the object to be notified when images finish drawing
	 * @return a draw helper for the specified map version
	 */
	public TileDrawHelper factory(final int version, final ImageObserver iobs) {
		if (version == 1) {
			return verOneHelper; // NOPMD
		} else if (version == 2) {
			if (!verTwoHelpers.containsKey(iobs)) {
				verTwoHelpers.put(iobs, new Ver2TileDrawHelper(iobs));
			}
			return verTwoHelpers.get(iobs); 
		} else {
			throw new IllegalArgumentException("Unsupported map version");
		}
	}
}
