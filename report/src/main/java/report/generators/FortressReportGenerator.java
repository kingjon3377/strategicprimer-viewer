package report.generators;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.EnumSet;
import java.util.stream.Collectors;

import lovelace.util.DelayedRemovalMap;

import legacy.map.Direction;
import legacy.map.TileFixture;
import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.towns.IFortress;

/**
 * A report generator for fortresses.
 */
public class FortressReportGenerator extends AbstractReportGenerator<IFortress> {
	public FortressReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn) {
		this(currentPlayer, dimensions, currentTurn, null);
	}

	public FortressReportGenerator(final Player currentPlayer,
								   final MapDimensions dimensions, final Integer currentTurn,
								   final @Nullable Point hq) {
		super(dimensions, hq);
		urg = new UnitReportGenerator(currentPlayer, dimensions, currentTurn, hq);
		memberReportGenerator = new FortressMemberReportGenerator(currentPlayer, dimensions,
				currentTurn, hq);
		this.currentPlayer = currentPlayer;
	}

	private final IReportGenerator<IUnit> urg;
	private final IReportGenerator<FortressMember> memberReportGenerator;
	private final Player currentPlayer;

	private static void terrain(final Consumer<String> ostream, final ILegacyMap map, final Point point,
								final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures) {
		ostream.accept("Surrounding terrain: ");
		ostream.accept(Optional.ofNullable(map.getBaseTerrain(point)).map(Object::toString)
				.orElse("Unknown"));
		boolean unforested = true;
		if (map.isMountainous(point)) {
			ostream.accept(", mountainous");
		}
		for (final TileFixture fixture : map.getFixtures(point)) {
			if (unforested && fixture instanceof final Forest f) {
				unforested = false;
				ostream.accept(", forested with ");
				ostream.accept(f.getKind());
				fixtures.remove(fixture.getId());
			} else if (fixture instanceof Hill) {
				ostream.accept(", hilly");
				fixtures.remove(fixture.getId());
			} else if (fixture instanceof Oasis) {
				ostream.accept(", with a nearby oasis");
				fixtures.remove(fixture.getId());
			}
		}
	}

	/**
	 * Write HTML representing a collection of rivers.
	 */
	private void riversToString(final Consumer<String> formatter, final Collection<River> rivers) {
		final Set<River> set = EnumSet.noneOf(River.class);
		set.addAll(rivers);
		if (set.contains(River.Lake)) {
			println(formatter, "<li>There is a nearby lake.</li>");
			set.remove(River.Lake);
		}
		if (!set.isEmpty()) {
			formatter.accept("""
					<li>There is a river on the tile, flowing through the following borders:\s""");
			formatter.accept(set.stream().map(River::getDescription)
					.collect(Collectors.joining(", ")));
			println(formatter, "</li>");
		}
	}

	private <Type extends IFixture> void printList(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final Consumer<String> ostream, final ILegacyMap map, final Collection<? extends Type> list,
			final String header, final IReportGenerator<Type> helper, final Point loc) {
		if (!list.isEmpty()) {
			ostream.accept("<li>");
			ostream.accept(header);
			println(ostream, ":<ul>");
			for (final Type item : list) {
				ostream.accept("<li>");
				helper.produceSingle(fixtures, map, ostream, item, loc);
				println(ostream, "</li>");
			}
			println(ostream, "</ul></li>");
		}
	}

	/**
	 * Produces a sub-report on a fortress. All fixtures referred to in
	 * this report are removed from the collection.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final ILegacyMap map,
							  final Consumer<String> ostream, final IFortress item, final Point loc) {
		ostream.accept("<h5>Fortress ");
		ostream.accept(item.getName());
		ostream.accept("belonging to ");
		ostream.accept((item.owner().equals(currentPlayer)) ? "you" : item.owner().toString());
		ostream.accept("""
				</h5>
				<ul>
					<li>Located at\s""");
		ostream.accept(loc.toString());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		ostream.accept("""
				</li>
					<li>""");
		terrain(ostream, map, loc, fixtures);
		println(ostream, "</li>");
		riversToString(ostream, map.getRivers(loc));
		if (!map.getRoads(loc).isEmpty()) {
			ostream.accept("<li>There are roads going in the the following directions:");
			ostream.accept(map.getRoads(loc).keySet().stream().map(Direction::toString)
					.collect(Collectors.joining(", ")));
			// TODO: Report what kinds of roads they are
			println(ostream, "</li>");
		}
		final List<IUnit> units = new ArrayList<>();
		final List<Implement> equipment = new ArrayList<>();
		final Map<String, List<IResourcePile>> resources = new HashMap<>(); // TODO: Use a multimap
		final List<FortressMember> contents = new ArrayList<>();
		for (final FortressMember member : item) {
			switch (member) {
				case final IUnit u -> units.add(u);
				case final Implement i -> equipment.add(i);
				case final IResourcePile r -> {
					final List<IResourcePile> list = Optional.ofNullable(resources.get(r.getKind()))
							.orElseGet(ArrayList::new);
					list.add(r);
					resources.put(r.getKind(), list);
				}
				case null, default -> contents.add(member);
			}
			fixtures.remove(item.getId());
		}
		printList(fixtures, ostream, map, units, "Units in the fortress", urg, loc);
		printList(fixtures, ostream, map, equipment, "Equipment", memberReportGenerator, loc);
		if (!resources.isEmpty()) {
			println(ostream, "<li>Resources:<ul>");
			for (final Map.Entry<String, List<IResourcePile>> entry : resources.entrySet()) {
				printList(fixtures, ostream, map, entry.getValue(), entry.getKey(), memberReportGenerator, loc);
			}
			println(ostream, "</ul></li>");
		}
		printList(fixtures, ostream, map, contents, "Other fortress contents", memberReportGenerator, loc);
		println(ostream, "</ul>");
		fixtures.remove(item.getId());
	}

	/**
	 * Produces a sub-report on all fortresses. All fixtures referred to in
	 * this report are removed from the collection.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
						final ILegacyMap map, final Consumer<String> ostream) {
		final Map<IFortress, Point> ours = new HashMap<>();
		final Map<IFortress, Point> others = new HashMap<>();
		for (final Pair<Point, IFortress> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IFortress)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IFortress) p.getValue1())).toList()) {
			final Point loc = pair.getValue0();
			final IFortress fort = pair.getValue1();
			if (currentPlayer.equals(fort.owner())) {
				ours.put(fort, loc);
			} else {
				others.put(fort, loc);
			}
		}
		if (!ours.isEmpty()) {
			println(ostream, "<h4>Your fortresses in the map:</h4>");
			for (final Map.Entry<IFortress, Point> entry : ours.entrySet()) {
				produceSingle(fixtures, map, ostream, entry.getKey(), entry.getValue());
			}
		}
		if (!others.isEmpty()) {
			println(ostream, "<h4>Other fortresses in the map:</h4>");
			for (final Map.Entry<IFortress, Point> entry : others.entrySet()) {
				produceSingle(fixtures, map, ostream, entry.getKey(), entry.getValue());
			}
		}
	}
}
