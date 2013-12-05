package model.exploration;

import static model.map.PointFactory.point;
import static model.map.TileType.Ocean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.map.IMap;
import model.map.ITile;
import model.map.MapDimensions;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;

/**
 * A class to facilitate a better hunting/fishing driver.
 *
 * TODO: Use MultiMaps once we add the Guava dependency.
 *
 * @author Jonathan Lovelace
 *
 */
public class HuntingModel {
	/**
	 * The "nothing" value we insert.
	 */
	public static final String NOTHING = "Nothing ...";
	/**
	 * The non-aquatic animals in the map.
	 */
	private final Map<Point, List<String>> animals = new HashMap<>();
	/**
	 * The aquatic animals in the map.
	 */
	private final Map<Point, List<String>> fish = new HashMap<>();
	/**
	 * The plants in the map.
	 */
	private final Map<Point, List<String>> plants = new HashMap<>();
	/**
	 * The size of the map.
	 */
	protected final MapDimensions dims;
	/**
	 * Constructor.
	 * @param map the map to hunt in
	 */
	public HuntingModel(final IMap map) {
		dims = map.getDimensions();
		final Set<String> fishKinds = new HashSet<>();
		for (final Point point : map.getTiles()) {
			if (point == null) {
				continue;
			}
			final ITile tile = map.getTile(point);
			if (Ocean.equals(tile.getTerrain())) {
				for (final TileFixture fix : tile) {
					if (fix instanceof Animal) {
						fishKinds.add(((Animal) fix).getKind());
					}
				}
			}
		}
		for (final Point point : map.getTiles()) {
			if (point == null) {
				continue;
			}
			final ITile tile = map.getTile(point);
			for (final TileFixture fix : tile) {
				if (fix instanceof Animal && !((Animal) fix).isTalking()
						&& !((Animal) fix).isTraces()) {
					if (fishKinds.contains(((Animal) fix).getKind())) {
						addToMap(fish, point, ((Animal) fix).getKind());
					} else {
						addToMap(animals, point, ((Animal) fix).getKind());
					}
				} else if (fix instanceof Grove || fix instanceof Meadow
						|| fix instanceof Shrub) {
					final String str = fix.toString();
					assert str != null;
					addToMap(plants, point, str);
				}
			}
			addToMap(plants, point, NOTHING);
			final List<String> plantList = plants.get(point);
			assert plantList != null;
			final int len = plantList.size() - 1;
			// ESCA-JAVA0177:
			final int nothings;
			switch (tile.getTerrain()) {
			case Desert:
			case Tundra:
				nothings = len * 3;
				break;
			case Jungle:
				nothings = len / 2;
				break;
			default:
				nothings = len;
				break;
			}
			for (int i = 0; i < nothings; i++) {
				plantList.add(NOTHING);
			}
		}
	}
	/**
	 * @param map one of the mappings
	 * @param point a point
	 * @param string a string to put in the map at that point.
	 */
	private static void addToMap(final Map<Point, List<String>> map,
			final Point point, final String string) {
		// ESCA-JAVA0177:
		final List<String> list;
		if (map.containsKey(point)) {
			list = map.get(point);
			assert list != null;
		} else {
			list =  new ArrayList<>();
			map.put(point, list);
		}
		list.add(string);
	}
	/**
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of hunting results from the surrounding area. About half will be "nothing"
	 */
	public List<String> hunt(final Point point, final int items) {
		return chooseFromMap(point, items, animals);
	}
	/**
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of fishing results from the surrounding area. About half will be "nothing"
	 */
	public List<String> fish(final Point point, final int items) {
		return chooseFromMap(point, items, fish);
	}

	/**
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of gathering results from the surrounding area. Many will
	 *         be "nothing," especially from desert and tundra tiles and less
	 *         from jungle tiles.
	 */
	public List<String> gather(final Point point, final int items) {
		final List<String> choices = new ArrayList<>();
		for (final Point local : new TwentyFivePointIterable(point)) {
			if (plants.containsKey(local)) {
				choices.addAll(plants.get(local));
			}
		}
		for (final Point local : new NinePointIterable(point)) {
			if (plants.containsKey(local)) {
				choices.addAll(plants.get(local));
			}
		}
		for (int i = 0; i < 14; i++) {
			if (plants.containsKey(point)) {
				choices.addAll(plants.get(point));
			}
		}
		final List<String> retval = new ArrayList<>();
		for (int i = 0; i < items; i++) {
			Collections.shuffle(choices);
			retval.add(choices.get(0));
		}
		return retval;
	}
	/**
	 * A helper method for hunting or fishing.
	 * @param point what point to look around
	 * @param items how many items to limit the results to
	 * @param chosenMap which map to look in
	 * @return a list of results, about one eighth of which will be "nothing."
	 */
	private List<String> chooseFromMap(final Point point, final int items,
			final Map<Point, List<String>> chosenMap) {
		final List<String> choices = new ArrayList<>();
		for (final Point local : new TwentyFivePointIterable(point)) {
			if (chosenMap.containsKey(local)) {
				choices.addAll(chosenMap.get(local));
			}
		}
		for (final Point local : new NinePointIterable(point)) {
			if (chosenMap.containsKey(local)) {
				choices.addAll(chosenMap.get(local));
			}
		}
		for (int i = 0; i < 14; i++) {
			if (chosenMap.containsKey(point)) {
				choices.addAll(chosenMap.get(point));
			}
		}
		int nothings = choices.size();
		for (int i = 0; i < nothings; i++) {
			choices.add(NOTHING);
		}
		final List<String> retval = new ArrayList<>();
		for (int i = 0; i < items; i++) {
			Collections.shuffle(choices);
			retval.add(choices.get(0));
		}
		return retval;
	}
	// ESCA-JAVA0043:
	// ESCA-JAVA0011:
	/**
	 * An iterator over the several points surrounding a point.
	 */
	protected abstract class AbstractMultiPointIterable implements Iterable<Point> {
		/**
		 * Round a column number to fit within the map.
		 * @param col the column number
		 * @return its equivalent that's actually within the map
		 */
		protected int roundCol(final int col) {
			if (col < 0) {
				return dims.cols + col;
			} else {
				return col % dims.cols;
			}
		}
		/**
		 * Round a row number to fit within the map.
		 * @param row the row number
		 * @return its equivalent that's actually within the map
		 */
		protected int roundRow(final int row) {
			if (row < 0) {
				return dims.rows + row;
			} else {
				return row % dims.rows;
			}
		}
	}
	/**
	 * An iterator over the twenty-five points (including itself) surrounding a point.
	 */
	private class TwentyFivePointIterable extends AbstractMultiPointIterable {
		/**
		 * the list of points.
		 */
		private final List<Point> points = new ArrayList<>();
		/**
		 * @param starting the starting point.
		 */
		protected TwentyFivePointIterable(final Point starting) {
			for (int row = -2; row < 3; row++) {
				for (int col = -2; col < 3; col++) {
					points.add(point(roundRow(starting.row + row),
							roundCol(starting.col + col)));
				}
			}
		}
		/**
		 * @return an iterator over the points
		 */
		@Override
		public Iterator<Point> iterator() {
			final Iterator<Point> iter = points.iterator();
			assert iter != null;
			return iter;
		}
	}
	/**
	 * An iterator over the nine points (including itself) surrounding a point.
	 */
	private class NinePointIterable extends AbstractMultiPointIterable {
		/**
		 * the list of points.
		 */
		private final List<Point> points = new ArrayList<>();
		/**
		 * @param starting the starting point.
		 */
		protected NinePointIterable(final Point starting) {
			for (int row = -1; row < 2; row++) {
				for (int col = -1; col < 2; col++) {
					points.add(point(roundRow(starting.row + row),
							roundCol(starting.col + col)));
				}
			}
		}
		/**
		 * @return an iterator over the points
		 */
		@Override
		public Iterator<Point> iterator() {
			final Iterator<Point> iter = points.iterator();
			assert iter != null;
			return iter;
		}
	}
}
