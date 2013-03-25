package controller.map.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.map.IFixture;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import util.IntMap;
import util.Pair;
import controller.map.misc.HarvestableComparator;

/**
 * A report generator for harvestable fixtures (other than caves and
 * battlefields, which aren't really).
 *
 * @author Jonathan Lovelace
 *
 */
public class HarvestableReportGenerator extends // NOPMD
		AbstractReportGenerator<HarvestableFixture> {

	/**
	 * Produce the sub-reports dealing with "harvestable" fixtures.
	 * All fixtures referred to in this report are to be removed from the
	 * collection. Caves and battlefields, though HarvestableFixtures, are
	 * presumed to have been handled already.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @return the part of the report listing things that can be harvested.
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures, // NOPMD
			final TileCollection tiles) {
		final StringBuilder builder = new StringBuilder("<h4>Resource Sources</h4>\n");
		final List<Pair<Point, HarvestableFixture>> list = new ArrayList<Pair<Point, HarvestableFixture>>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof HarvestableFixture) {
				list.add(Pair.of(pair.first(), (HarvestableFixture) pair.second()));
			}
		}
		Collections.sort(list, new PairComparator());
		final HtmlList caches = new HtmlList("<h5>Caches collected by your explorers and workers:</h5>\n");
		final HtmlList groves = new HtmlList("<h5>Groves and orchards</h5>");
		final HtmlList meadows = new HtmlList("<h5>Meadows and fields</h5>");
		final HtmlList mines = new HtmlList("<h5>Mines</h5>");
		final HtmlList minerals = new HtmlList("<h5>Mineral deposits</h5>");
		final Map<String, List<Point>> shrubs = new HashMap<String, List<Point>>();
		final HtmlList stone = new HtmlList("<h5>Exposed stone deposits</h5>");
		for (final Pair<Point, HarvestableFixture> pair : list) {
			final HarvestableFixture harvestable = pair.second();
			final Point point = pair.first();
			if (harvestable instanceof CacheFixture) {
				caches.add(produce(fixtures, tiles, harvestable, point));
			} else if (harvestable instanceof Grove) {
				groves.add(produce(fixtures, tiles, harvestable, point));
			} else if (harvestable instanceof Meadow) {
				meadows.add(produce(fixtures, tiles, harvestable, point));
			} else if (harvestable instanceof Mine) {
				mines.add(produce(fixtures, tiles, harvestable, point));
			} else if (harvestable instanceof MineralVein) {
				// TODO: Handle these like shrubs.
				minerals.add(produce(fixtures, tiles, harvestable, point));
			} else if (harvestable instanceof Shrub) {
				// ESCA-JAVA0177:
				final List<Point> shrubPoints; // NOPMD
				if (shrubs.containsKey(((Shrub) harvestable).getKind())) {
					shrubPoints = shrubs.get(((Shrub) harvestable).getKind());
				} else {
					shrubPoints = new ArrayList<Point>(); // NOPMD
					shrubs.put(((Shrub) harvestable).getKind(), shrubPoints);
				}
				shrubPoints.add(point);
				fixtures.remove(Integer.valueOf(harvestable.getID()));
			} else if (harvestable instanceof StoneDeposit) {
				// TODO: Handle these like shrubs.
				stone.add(produce(fixtures, tiles, harvestable, point));
			}
		}
		final HtmlList shrubsText = new HtmlList("<h5>Shrubs, small trees, and such</h5>");
		for (final Entry<String, List<Point>> entry : shrubs.entrySet()) {
			shrubsText.add(new StringBuilder(entry.getKey())// NOPMD
					.append(": at ").append(pointCSL(entry.getValue())).toString());
		}
		builder.append(caches.toString()).append(groves.toString())
				.append(meadows.toString()).append(mines.toString())
				.append(minerals.toString()).append(stone.toString())
				.append(shrubsText.toString());
		return caches.isEmpty() && groves.isEmpty() && meadows.isEmpty()
				&& mines.isEmpty() && minerals.isEmpty() && stone.isEmpty()
				&& shrubs.isEmpty() ? "" : builder.toString();
	}
	/**
	 * Produce the sub-sub-report dealing with a harvestable fixture.
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param item the fixture to report on
	 * @param loc its location
	 * @return a sub-report dealing with the fixture
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final HarvestableFixture item, final Point loc) {
		if (item instanceof CacheFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc)).append("A cache of ")// NOPMD
					.append(((CacheFixture) item).getKind())
					.append(", containing ")
					.append(((CacheFixture) item).getContents()).toString();
		} else if (item instanceof Grove) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc))// NOPMD
					.append("A ")
					.append(ternary(((Grove) item).isCultivated(),
							"cultivated ", "wild "))
					.append(((Grove) item).getKind())
					.append(ternary(((Grove) item).isOrchard(), " orchard",
							" grove")).toString();
		} else if (item instanceof Meadow) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc))//NOPMD
					.append("A ")
					.append(((Meadow) item).getStatus().toString())
					.append(ternary(((Meadow) item).isCultivated(),
							" cultivated ", " wild or abandoned "))
					.append(((Meadow) item).getKind())
					.append(ternary(((Meadow) item).isField(), " field",
							" meadow")).toString();
		} else if (item instanceof Mine) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc)).append(item.toString())// NOPMD
					.toString();
		} else if (item instanceof MineralVein) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc))// NOPMD
					.append("An ")
					.append(ternary(((MineralVein) item).isExposed(),
							"exposed ", "unexposed ")).append(" vein of ")
					.append(((MineralVein) item).getKind()).toString();
		} else if (item instanceof Shrub) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc)).append(// NOPMD
					((Shrub) item).getKind()).toString();
		} else if (item instanceof StoneDeposit) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder(atPoint(loc)).append("An exposed ")// NOPMD
					.append(((StoneDeposit) item).getKind()).append(" deposit")
					.toString();
		} else {
			// It's a battlefield or cave.
			return new ExplorableReportGenerator().produce(fixtures, tiles, item, loc);
		}
	}
	/**
	 * We need this to reduce the calculated complexity.
	 * @param bool a Boolean
	 * @param first what to return if true
	 * @param second what to return if false
	 * @return the result of the ternary operator.
	 */
	private static String ternary(final boolean bool, final String first, final String second) {
		return bool ? first : second;
	}
	/**
	 * A comparator for Pairs of Points and HarvestableFixtures, comparing only using the second item.
	 */
	static class PairComparator implements Comparator<Pair<Point, HarvestableFixture>>, Serializable {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The comparator that does the bulk of the logic.
		 */
		private static final HarvestableComparator HARV_COMP = new HarvestableComparator();
		/**
		 * Compare the two items.
		 * @param one one
		 * @param two another
		 * @return the result of the comparison
		 */
		@Override
		public int compare(final Pair<Point, HarvestableFixture> one,
				final Pair<Point, HarvestableFixture> two) {
			return HARV_COMP.compare(one.second(), two.second());
		}

	}
	/**
	 * A list that produces HTML in its toString().
	 */
	private static class HtmlList extends ArrayList<String> {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The header: what to print before opening the list.
		 */
		private final String header;
		/**
		 * Constructor.
		 * @param head what to print before opening the list
		 */
		HtmlList(final String head) {
			header = head;
		}
		/**
		 * @return a HTML representation of the list if there's anything in it, or the empty string otherwise.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return ""; // NOPMD
			} else {
				final StringBuilder builder = new StringBuilder(header).append(OPEN_LIST);
				for (String item : this) {
					builder.append(OPEN_LIST_ITEM).append(item).append(CLOSE_LIST_ITEM);
				}
				return builder.append(CLOSE_LIST).toString();
			}
		}
	}
}
