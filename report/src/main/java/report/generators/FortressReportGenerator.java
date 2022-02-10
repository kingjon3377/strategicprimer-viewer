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

import common.map.Direction;
import common.map.TileFixture;
import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.River;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.FortressMember;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.IFortress;

/**
 * A report generator for fortresses.
 */
public class FortressReportGenerator extends AbstractReportGenerator<IFortress> {
	public FortressReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn) {
		this(currentPlayer, dimensions, currentTurn, null);
	}

	public FortressReportGenerator(final Player currentPlayer,
	                               final MapDimensions dimensions, final Integer currentTurn, @Nullable final Point hq) {
		super(dimensions, hq);
		urg = new UnitReportGenerator(currentPlayer, dimensions, currentTurn, hq);
		memberReportGenerator = new FortressMemberReportGenerator(currentPlayer, dimensions,
			currentTurn, hq);
		this.currentPlayer = currentPlayer;
	}

	private final IReportGenerator<IUnit> urg;
	private final IReportGenerator<FortressMember> memberReportGenerator;
	private final Player currentPlayer;

	private String terrain(final IMapNG map, final Point point,
	                       final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder();
		builder.append("Surrounding terrain: ");
		builder.append(Optional.ofNullable(map.getBaseTerrain(point)).map(Object::toString)
			.orElse("Unknown"));
		boolean unforested = true;
		if (map.isMountainous(point)) {
			builder.append(", mountainous");
		}
		for (final TileFixture fixture : map.getFixtures(point)) {
			if (unforested && fixture instanceof Forest) {
				unforested = false;
				builder.append(", forested with ");
				builder.append(((Forest) fixture).getKind());
				fixtures.remove(fixture.getId());
			} else if (fixture instanceof Hill) {
				builder.append(", hilly");
				fixtures.remove(fixture.getId());
			} else if (fixture instanceof Oasis) {
				builder.append(", with a nearby oasis");
				fixtures.remove(fixture.getId());
			}
		}
		return builder.toString();
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
			formatter.accept("<li>There is a river on the tile, ");
			formatter.accept("flowing through the following borders: ");
			formatter.accept(set.stream().map(River::getDescription)
				.collect(Collectors.joining(", ")));
			println(formatter, "</li>");
		}
	}

	private <Type extends IFixture> void printList(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final Consumer<String> ostream, final IMapNG map, final Collection<? extends Type> list,
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
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final Consumer<String> ostream, final IFortress item, final Point loc) {
		ostream.accept("<h5>Fortress ");
		ostream.accept(item.getName());
		ostream.accept("belonging to ");
		ostream.accept((item.getOwner().equals(currentPlayer)) ? "you" : item.getOwner().toString());
		println(ostream, "</h5>");
		println(ostream, "<ul>");
		ostream.accept("    <li>Located at ");
		ostream.accept(loc.toString());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		println(ostream, "</li>");
		ostream.accept("    <li>");
		ostream.accept(terrain(map, loc, fixtures)); // TODO: Make it take ostream instead of returning String
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
			if (member instanceof IUnit) {
				units.add((IUnit) member);
			} else if (member instanceof Implement) {
				equipment.add((Implement) member);
			} else if (member instanceof IResourcePile) {
				final List<IResourcePile> list = Optional.ofNullable(resources
					.get(((IResourcePile) member).getKind()))
						.orElseGet(ArrayList::new);
				list.add((IResourcePile) member);
				resources.put(((IResourcePile) member).getKind(), list);
			} else {
				contents.add(member);
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
	                    final IMapNG map, final Consumer<String> ostream) {
		final Map<IFortress, Point> ours = new HashMap<>();
		final Map<IFortress, Point> others = new HashMap<>();
		for (final Pair<Point, IFortress> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IFortress)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IFortress) p.getValue1()))
				.collect(Collectors.toList())) {
			final Point loc = pair.getValue0();
			final IFortress fort = pair.getValue1();
			if (currentPlayer.equals(fort.getOwner())) {
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
