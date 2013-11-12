package controller.map.report;

import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.report.AbstractReportNode;
import util.DelayedRemovalMap;
import util.Pair;
/**
 * An interface for report generators.
 * @author Jonathan Lovelace
 *
 * @param <T> the type of thing an implementer can report on
 */
public interface IReportGenerator<T> {

	/**
	 * All fixtures that this report references should be removed from the set
	 * before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	String produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			TileCollection tiles, Player currentPlayer);

	/**
	 * Produce a report on a single item. All fixtures that this report
	 * references should be removed from the set before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item the particular item we are to be reporting on.
	 * @param loc the location of that item, if it's a fixture.
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	String produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			TileCollection tiles, Player currentPlayer, T item, Point loc);

	/**
	 * All fixtures that this report references should be removed from the set
	 * before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	AbstractReportNode produceRIR(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			TileCollection tiles, Player currentPlayer);

	/**
	 * Produce a report on a single item. All fixtures that this report
	 * references should be removed from the set before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item the particular item we are to be reporting on.
	 * @param loc the location of that item, if it's a fixture.
	 * @return the (sub-)report, or null if nothing to report.
	 */
	AbstractReportNode produceRIR(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			TileCollection tiles, Player currentPlayer, T item, Point loc);

}
