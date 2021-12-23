package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.IOConsumer;
import java.io.IOException;
import java.util.Comparator;
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
	public FortressReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, int currentTurn) {
		this(comp, currentPlayer, dimensions, currentTurn, null);
	}

	public FortressReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, Integer currentTurn, @Nullable Point hq) {
		super(comp, dimensions, hq);
		urg = new UnitReportGenerator(comp, currentPlayer, dimensions, currentTurn, hq);
		memberReportGenerator = new FortressMemberReportGenerator(comp, currentPlayer, dimensions,
			currentTurn, hq);
		this.currentPlayer = currentPlayer;
	}

	private final IReportGenerator<IUnit> urg;
	private final IReportGenerator<FortressMember> memberReportGenerator;
	private final Player currentPlayer;

	// TODO: Can this be static?
	private String terrain(IMapNG map, Point point,
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures) {
		StringBuilder builder = new StringBuilder();
		builder.append("Surrounding terrain: ");
		builder.append(Optional.ofNullable(map.getBaseTerrain(point)).map(Object::toString)
			.orElse("Unknown"));
		boolean unforested = true;
		if (map.isMountainous(point)) {
			builder.append(", mountainous");
		}
		for (TileFixture fixture : map.getFixtures(point)) {
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
	 *
	 * TODO: Can this be static?
	 */
	private void riversToString(IOConsumer<String> formatter, Collection<River> rivers) throws IOException {
		Set<River> set = EnumSet.noneOf(River.class);
		set.addAll(rivers);
		if (set.contains(River.Lake)) {
			formatter.accept("<li>There is a nearby lake.</li>");
			formatter.accept(System.lineSeparator());
			set.remove(River.Lake);
		}
		if (!set.isEmpty()) {
			formatter.accept("<li>There is a river on the tile, ");
			formatter.accept("flowing through the following borders: ");
			formatter.accept(set.stream().map(River::getDescription)
				.collect(Collectors.joining(", ")));
			formatter.accept("</li>");
			formatter.accept(System.lineSeparator());
		}
	}

	private static <Type extends IFixture> void printList(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IOConsumer<String> ostream, IMapNG map, Collection<? extends Type> list,
			String header, IReportGenerator<Type> helper, Point loc) throws IOException {
		if (!list.isEmpty()) {
			ostream.accept("<li>");
			ostream.accept(header);
			ostream.accept(":<ul>");
			ostream.accept(System.lineSeparator());
			for (Type item : list) {
				ostream.accept("<li>");
				helper.produceSingle(fixtures, map, ostream, item, loc);
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			ostream.accept("</ul></li>");
			ostream.accept(System.lineSeparator());
		}
	}

	/**
	 * Produces a sub-report on a fortress. All fixtures referred to in
	 * this report are removed from the collection.
	 */
	@Override
	public void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream, IFortress item, Point loc) 
			throws IOException {
		ostream.accept("<h5>Fortress ");
		ostream.accept(item.getName());
		ostream.accept("belonging to ");
		ostream.accept((item.getOwner().equals(currentPlayer)) ? "you" : item.getOwner().toString());
		ostream.accept("</h5>");
		ostream.accept(System.lineSeparator());
		ostream.accept("<ul>");
		ostream.accept(System.lineSeparator());
		ostream.accept("    <li>Located at ");
		ostream.accept(loc.toString());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		ostream.accept("</li>");
		ostream.accept(System.lineSeparator());
		ostream.accept("    <li>");
		ostream.accept(terrain(map, loc, fixtures)); // TODO: Make it take ostream instead of returning String
		ostream.accept("</li>");
		ostream.accept(System.lineSeparator());
		riversToString(ostream, map.getRivers(loc));
		if (!map.getRoads(loc).isEmpty()) {
			ostream.accept("<li>There are roads going in the the following directions:");
			ostream.accept(map.getRoads(loc).keySet().stream().map(Direction::toString)
				.collect(Collectors.joining(", ")));
			// TODO: Report what kinds of roads they are
			ostream.accept("</li>");
			ostream.accept(System.lineSeparator());
		}
		List<IUnit> units = new ArrayList<>();
		List<Implement> equipment = new ArrayList<>();
		Map<String, List<IResourcePile>> resources = new HashMap<>(); // TODO: Use a multimap
		List<FortressMember> contents = new ArrayList<>();
		for (FortressMember member : item) {
			if (member instanceof IUnit) {
				units.add((IUnit) member);
			} else if (member instanceof Implement) {
				equipment.add((Implement) member);
			} else if (member instanceof IResourcePile) {
				List list = Optional.ofNullable(resources
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
			ostream.accept("<li>Resources:<ul>");
			ostream.accept(System.lineSeparator());
			for (Map.Entry<String, List<IResourcePile>> entry : resources.entrySet()) {
				printList(fixtures, ostream, map, entry.getValue(), entry.getKey(), memberReportGenerator, loc);
			}
			ostream.accept("</ul></li>");
			ostream.accept(System.lineSeparator());
		}
		printList(fixtures, ostream, map, contents, "Other fortress contents", memberReportGenerator, loc);
		ostream.accept("</ul>");
		ostream.accept(System.lineSeparator());
		fixtures.remove(item.getId());
	}

	/**
	 * Produces a sub-report on all fortresses. All fixtures referred to in
	 * this report are removed from the collection.
	 */
	@Override
	public void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream) throws IOException {
		Map<IFortress, Point> ours = new HashMap<>();
		Map<IFortress, Point> others = new HashMap<>();
		for (Pair<Point, IFortress> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IFortress)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IFortress) p.getValue1()))
				.collect(Collectors.toList())) {
			Point loc = pair.getValue0();
			IFortress fort = pair.getValue1();
			if (currentPlayer.equals(fort.getOwner())) {
				ours.put(fort, loc);
			} else {
				others.put(fort, loc);
			}
		}
		if (!ours.isEmpty()) {
			ostream.accept("<h4>Your fortresses in the map:</h4>");
			ostream.accept(System.lineSeparator());
			for (Map.Entry<IFortress, Point> entry : ours.entrySet()) {
				produceSingle(fixtures, map, ostream, entry.getKey(), entry.getValue());
			}
		}
		if (!others.isEmpty()) {
			ostream.accept("<h4>Other fortresses in the map:</h4>");
			ostream.accept(System.lineSeparator());
			for (Map.Entry<IFortress, Point> entry : others.entrySet()) {
				produceSingle(fixtures, map, ostream, entry.getKey(), entry.getValue());
			}
		}
	}
}
