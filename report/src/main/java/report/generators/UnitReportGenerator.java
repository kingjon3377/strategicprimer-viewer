package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.IOConsumer;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Optional;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.FortressMember;
import common.map.fixtures.Implement;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalOrTracks;

/**
 * A report generator for units.
 */
public class UnitReportGenerator extends AbstractReportGenerator<IUnit> {

	public UnitReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, int currentTurn) {
		this(comp, currentPlayer, dimensions, currentTurn, null);
	}

	public UnitReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, int currentTurn, @Nullable Point hq) {
		super(comp, dimensions, hq);
		memberReportGenerator = new FortressMemberReportGenerator(comp, currentPlayer, dimensions,
			currentTurn, hq);
		animalReportGenerator = new AnimalReportGenerator(comp, dimensions, currentTurn, hq);
		ourWorkerReportGenerator = new WorkerReportGenerator(comp, true, dimensions,
			currentPlayer, hq);
		otherWorkerReportGenerator = new WorkerReportGenerator(comp, false, dimensions,
			currentPlayer, hq);
		this.currentPlayer = currentPlayer;
	}

	private final IReportGenerator<FortressMember> memberReportGenerator; 
	private final IReportGenerator</*Animal|AnimalTracks*/AnimalOrTracks> animalReportGenerator;
	private final IReportGenerator<IWorker> ourWorkerReportGenerator;
	private final IReportGenerator<IWorker> otherWorkerReportGenerator;
	private final Player currentPlayer;

	/**
	 * Produce the sub-sub-report about a unit's orders and results.
	 */
	private void produceOrders(IUnit item, IOConsumer<String> formatter) throws IOException {
		if (!item.getAllOrders().isEmpty() || !item.getAllResults().isEmpty()) {
			formatter.accept("Orders and Results:<ul>");
			formatter.accept(System.lineSeparator());
			for (int turn : Stream.concat(item.getAllOrders().keySet().stream(),
							item.getAllResults().keySet().stream())
						.mapToInt(Integer::intValue).sorted()
						.distinct().toArray()) {
				formatter.accept("<li>Turn ");
				formatter.accept(Integer.toString(turn));
				formatter.accept(":<ul>");
				formatter.accept(System.lineSeparator());
				String orders = item.getOrders(turn);
				if (!orders.isEmpty()) {
					formatter.accept("<li>Orders: ");
					formatter.accept(orders);
					formatter.accept("</li>");
					formatter.accept(System.lineSeparator());
				}
				String results = item.getResults(turn);
				if (!results.isEmpty()) {
					formatter.accept("<li>Results: ");
					formatter.accept(results);
					formatter.accept("</li>");
					formatter.accept(System.lineSeparator());
				}
				formatter.accept("</ul>");
				formatter.accept(System.lineSeparator());
				formatter.accept("</li>");
				formatter.accept(System.lineSeparator());
			}
			formatter.accept("</ul>");
			formatter.accept(System.lineSeparator());
		}
	}

	private static <T> @Nullable T findAndRemoveFirst(List<T> list, Predicate<T> predicate) {
		for (T item : list) {
			if (predicate.test(item)) {
				list.remove(item);
				return item;
			}
		}
		return null;
	}

	private <Member extends UnitMember> void produceInner(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IOConsumer<String> ostream, String heading, List<Member> collection,
			IOConsumer<Member> generator) throws IOException {
		if (!collection.isEmpty()) {
			ostream.accept("<li>");
			ostream.accept(heading);
			ostream.accept(":");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			for (Member member : collection) {
				ostream.accept("<li>");
				generator.accept(member);
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
				fixtures.remove(member.getId());
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
			ostream.accept("</li>");
			ostream.accept(System.lineSeparator());
		}
	}

	/**
	 * Produce a sub-sub-report on a unit (we assume we're already in the
	 * middle of a paragraph or bullet point).
	 */
	@Override
	public void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream, IUnit item, Point loc) 
			throws IOException {
		ostream.accept("Unit ");
		ostream.accept(item.getName());
		ostream.accept(" (");
		ostream.accept(item.getKind());
		ostream.accept("), ");
		if (item.getOwner().isIndependent()) {
			ostream.accept("independent");
		} else if (item.getOwner().equals(currentPlayer)) {
			ostream.accept("owned by you");
		} else {
			ostream.accept("owned by ");
			ostream.accept(item.getOwner().toString());
		}
		if (item.iterator().hasNext()) { // TODO: Change back to !item.isEmpty() if IUnit has that method with the meaning "no members"
			List<IWorker> workers = new ArrayList<>();
			List<Implement> equipment = new ArrayList<>();
			Map<String, List<IResourcePile>> resources = new HashMap<>();
			List<Animal> animals = new ArrayList<>();
			List<UnitMember> others = new ArrayList<>();
			for (UnitMember member : item) {
				if (member instanceof IWorker) {
					workers.add((IWorker) member);
				} else if (member instanceof Implement) {
					equipment.add((Implement) member);
				} else if (member instanceof IResourcePile) {
					List<IResourcePile> list = Optional.ofNullable(
							resources.get(((IResourcePile) member).getKind()))
						.orElseGet(ArrayList::new);
					list.add((IResourcePile) member);
					resources.put(((IResourcePile) member).getKind(), list);
				} else if (member instanceof Animal) {
					Animal existing = findAndRemoveFirst(animals,
						((Animal) member)::equalExceptPopulation);
					if (existing == null) {
						animals.add((Animal) member);
					} else {
						animals.add(((Animal) member).combined(existing));
					}
				} else {
					others.add(member);
				}
			}
			ostream.accept(". Members of the unit:");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			IReportGenerator<IWorker> workerReportGenerator;
			if (item.getOwner().equals(currentPlayer)) {
				workerReportGenerator = ourWorkerReportGenerator;
			} else {
				workerReportGenerator = otherWorkerReportGenerator;
			}
			this.<IWorker>produceInner(fixtures, ostream, "Workers", workers, (worker) ->
				workerReportGenerator.produceSingle(fixtures, map, ostream, worker, loc));
			this.<Animal>produceInner(fixtures, ostream, "Animals", animals,
				(animal) -> animalReportGenerator
					.produceSingle(fixtures, map, ostream, animal, loc));
			this.<Implement>produceInner(fixtures, ostream, "Equipment", equipment, (member) ->
				memberReportGenerator.produceSingle(fixtures, map, ostream, member, loc));
			if (!resources.isEmpty()) {
				ostream.accept("<li>Resources:");
				ostream.accept(System.lineSeparator());
				ostream.accept("<ul>");
				ostream.accept(System.lineSeparator());
				for (Map.Entry<String, List<IResourcePile>> entry : resources.entrySet()) {
					produceInner(fixtures, ostream, entry.getKey(), entry.getValue(),
						(IResourcePile member) ->
							memberReportGenerator.produceSingle(fixtures, map,
								ostream, member, loc));
				}
				ostream.accept("</ul>");
				ostream.accept(System.lineSeparator());
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			this.<UnitMember>produceInner(fixtures, ostream, "Others", others,
				(it) -> ostream.accept(it.toString()));
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
		produceOrders(item, ostream);
		fixtures.remove(item.getId());
	}

	private void unitFormatter(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IUnit unit, Point loc, IOConsumer<String> formatter) 
			throws IOException {
		formatter.accept("At ");
		formatter.accept(loc.toString());
		formatter.accept(distanceString.apply(loc));
		produceSingle(fixtures, map, formatter, unit, loc);
	}

	/**
	 * Produce the part of the report on all units not covered as part of fortresses.
	 */
	@Override
	public void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream) throws IOException {
		HeadedMap<IUnit, Point> foreign = new HeadedMapImpl<>("<h5>Foreign Units</h5>");
		HeadedMap<IUnit, Point> ours = new HeadedMapImpl<>("<h5>Your units</h5>");
		for (Pair<Point, IUnit> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IUnit)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IUnit) p.getValue1()))
				.collect(Collectors.toList())) {
			IUnit unit = pair.getValue1();
			Point loc = pair.getValue0();
			if (currentPlayer.equals(unit.getOwner())) {
				ours.put(unit, loc);
			} else {
				foreign.put(unit, loc);
			}
		}
		if (!ours.isEmpty() || !foreign.isEmpty()) {
			ostream.accept("<h4>Units in the map</h4>");
			ostream.accept(System.lineSeparator());
			ostream.accept("<p>(Any units listed above are not described again.)</p>");
			ostream.accept(System.lineSeparator());
			writeMap(ostream, ours, (unit, loc, formatter) ->
				unitFormatter(fixtures, map, unit, loc, formatter));
			writeMap(ostream, foreign, (unit, loc, formatter) ->
				unitFormatter(fixtures, map, unit, loc, formatter));
		}
	}
}