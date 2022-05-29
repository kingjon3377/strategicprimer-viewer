package report.generators;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
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

	public UnitReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn) {
		this(currentPlayer, dimensions, currentTurn, null);
	}

	public UnitReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn, final @Nullable Point hq) {
		super(dimensions, hq);
		memberReportGenerator = new FortressMemberReportGenerator(currentPlayer, dimensions,
			currentTurn, hq);
		animalReportGenerator = new AnimalReportGenerator(dimensions, currentTurn, hq);
		ourWorkerReportGenerator = new WorkerReportGenerator(true, dimensions,
			currentPlayer, currentTurn, hq);
		otherWorkerReportGenerator = new WorkerReportGenerator(false, dimensions,
			currentPlayer, currentTurn, hq);
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
	private void produceOrders(final IUnit item, final Consumer<String> formatter) {
		if (!item.getAllOrders().isEmpty() || !item.getAllResults().isEmpty()) {
			println(formatter, "Orders and Results:<ul>");
			for (final int turn : Stream.concat(item.getAllOrders().keySet().stream(),
							item.getAllResults().keySet().stream())
						.mapToInt(Integer::intValue).sorted()
						.distinct().toArray()) {
				formatter.accept("<li>Turn ");
				formatter.accept(Integer.toString(turn));
				println(formatter, ":<ul>");
				final String orders = item.getOrders(turn);
				if (!orders.isEmpty()) {
					formatter.accept("<li>Orders: ");
					formatter.accept(orders);
					println(formatter, "</li>");
				}
				final String results = item.getResults(turn);
				if (!results.isEmpty()) {
					formatter.accept("<li>Results: ");
					formatter.accept(results);
					println(formatter, "</li>");
				}
				formatter.accept("""
						</ul>
						</li>
						""");
			}
			println(formatter, "</ul>");
		}
	}

	private static <T> @Nullable T findAndRemoveFirst(final List<T> list, final Predicate<T> predicate) {
		for (final T item : list) {
			if (predicate.test(item)) {
				list.remove(item);
				return item;
			}
		}
		return null;
	}

	private <Member extends UnitMember> void produceInner(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final Consumer<String> ostream, final String heading, final List<Member> collection,
			final Consumer<Member> generator) {
		if (!collection.isEmpty()) {
			ostream.accept("<li>");
			ostream.accept(heading);
			ostream.accept("""
					:
					<ul>
					""");
			for (final Member member : collection) {
				ostream.accept("<li>");
				generator.accept(member);
				println(ostream, "</li>");
				fixtures.remove(member.getId());
			}
			ostream.accept("""
					</ul>
					</li>
					""");
		}
	}

	/**
	 * Produce a sub-sub-report on a unit (we assume we're already in the
	 * middle of a paragraph or bullet point).
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final Consumer<String> ostream, final IUnit item, final Point loc) {
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
		if (!item.isEmpty()) {
			final List<IWorker> workers = new ArrayList<>();
			final List<Implement> equipment = new ArrayList<>();
			final Map<String, List<IResourcePile>> resources = new HashMap<>();
			final List<Animal> animals = new ArrayList<>();
			final List<UnitMember> others = new ArrayList<>();
			for (final UnitMember member : item) {
				if (member instanceof IWorker w) {
					workers.add(w);
				} else if (member instanceof Implement i) {
					equipment.add(i);
				} else if (member instanceof IResourcePile r) {
					final List<IResourcePile> list = Optional.ofNullable(resources.get(r.getKind()))
						.orElseGet(ArrayList::new);
					list.add(r);
					resources.put(r.getKind(), list);
				} else if (member instanceof Animal a) {
					final Animal existing = findAndRemoveFirst(animals,
						a::equalExceptPopulation);
					if (existing == null) {
						animals.add(a);
					} else {
						animals.add(a.combined(existing));
					}
				} else {
					others.add(member);
				}
			}
			ostream.accept("""
					Members of the unit:
					<ul>
					""");
			final IReportGenerator<IWorker> workerReportGenerator;
			if (item.getOwner().equals(currentPlayer)) {
				workerReportGenerator = ourWorkerReportGenerator;
			} else {
				workerReportGenerator = otherWorkerReportGenerator;
			}
			produceInner(fixtures, ostream, "Workers", workers, (worker) ->
				workerReportGenerator.produceSingle(fixtures, map, ostream, worker, loc));
			produceInner(fixtures, ostream, "Animals", animals,
				(animal) -> animalReportGenerator
					.produceSingle(fixtures, map, ostream, animal, loc));
			produceInner(fixtures, ostream, "Equipment", equipment, (member) ->
				memberReportGenerator.produceSingle(fixtures, map, ostream, member, loc));
			if (!resources.isEmpty()) {
				ostream.accept("""
						<li>Resources:
						<ul>""");
				for (final Map.Entry<String, List<IResourcePile>> entry : resources.entrySet()) {
					produceInner(fixtures, ostream, entry.getKey(), entry.getValue(),
						(IResourcePile member) ->
							memberReportGenerator.produceSingle(fixtures, map,
								ostream, member, loc));
				}
				ostream.accept("""
						</ul>
						</li>
						""");
			}
			produceInner(fixtures, ostream, "Others", others,
				(it) -> ostream.accept(it.toString()));
			println(ostream, "</ul>");
		}
		produceOrders(item, ostream);
		fixtures.remove(item.getId());
	}

	private void unitFormatter(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                           final IMapNG map, final IUnit unit, final Point loc, final Consumer<String> formatter) {
		formatter.accept("At ");
		formatter.accept(loc.toString());
		formatter.accept(distanceString.apply(loc));
		produceSingle(fixtures, map, formatter, unit, loc);
	}

	/**
	 * Produce the part of the report on all units not covered as part of fortresses.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final Consumer<String> ostream) {
		final HeadedMap<IUnit, Point> foreign = new HeadedMapImpl<>("<h5>Foreign Units</h5>");
		final HeadedMap<IUnit, Point> ours = new HeadedMapImpl<>("<h5>Your units</h5>");
		for (final Pair<Point, IUnit> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IUnit)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IUnit) p.getValue1()))
				.collect(Collectors.toList())) {
			final IUnit unit = pair.getValue1();
			final Point loc = pair.getValue0();
			if (currentPlayer.equals(unit.getOwner())) {
				ours.put(unit, loc);
			} else {
				foreign.put(unit, loc);
			}
		}
		if (!ours.isEmpty() || !foreign.isEmpty()) {
			ostream.accept("""
					<h4>Units in the map</h4>
					<p>(Any units listed above are not described again.)</p>
					""");
			writeMap(ostream, ours, (unit, loc, formatter) ->
				unitFormatter(fixtures, map, unit, loc, formatter));
			writeMap(ostream, foreign, (unit, loc, formatter) ->
				unitFormatter(fixtures, map, unit, loc, formatter));
		}
	}
}
