package report.generators;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Optional;

import lovelace.util.DelayedRemovalMap;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalOrTracks;

/**
 * A report generator for units.
 */
public final class UnitReportGenerator extends AbstractReportGenerator<IUnit> {

	public UnitReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn) {
		this(currentPlayer, dimensions, currentTurn, null);
	}

	public UnitReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn,
	                           final @Nullable Point hq) {
		super(dimensions, hq);
		memberReportGenerator = new FortressMemberReportGenerator(currentPlayer, dimensions,
				currentTurn, hq);
		animalReportGenerator = new AnimalReportGenerator(dimensions, currentTurn, hq);
		ourWorkerReportGenerator = new WorkerReportGenerator(WorkerReportGenerator.Verbosity.Detailed, dimensions,
				currentPlayer, currentTurn, hq);
		otherWorkerReportGenerator = new WorkerReportGenerator(WorkerReportGenerator.Verbosity.Concise, dimensions,
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

	private static <T> @Nullable T findAndRemoveFirst(final Collection<T> list, final Predicate<T> predicate) {
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
			final Consumer<String> ostream, final String heading, final Collection<Member> collection,
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
							  final ILegacyMap map, final Consumer<String> ostream, final IUnit item, final Point loc) {
		ostream.accept("Unit ");
		ostream.accept(item.getName());
		ostream.accept(" (");
		ostream.accept(item.getKind());
		ostream.accept("), ");
		if (item.isIndependent()) {
			ostream.accept("independent");
		} else if (item.owner().equals(currentPlayer)) {
			ostream.accept("owned by you");
		} else {
			ostream.accept("owned by ");
			ostream.accept(item.owner().toString());
		}
		if (!item.isEmpty()) {
			final Collection<IWorker> workers = new ArrayList<>();
			final Collection<Implement> equipment = new ArrayList<>();
			final Map<String, List<IResourcePile>> resources = new HashMap<>();
			final Collection<Animal> animals = new ArrayList<>();
			final Collection<UnitMember> others = new ArrayList<>();
			for (final UnitMember member : item) {
				switch (member) {
					case final IWorker w -> workers.add(w);
					case final Implement i -> equipment.add(i);
					case final IResourcePile r -> {
						final List<IResourcePile> list = Optional.ofNullable(resources.get(r.getKind()))
								.orElseGet(ArrayList::new);
						list.add(r);
						resources.put(r.getKind(), list);
					}
					case final Animal a -> {
						final Animal existing = findAndRemoveFirst(animals,
								a::equalExceptPopulation);
						if (Objects.isNull(existing)) {
							animals.add(a);
						} else {
							animals.add(a.combined(existing));
						}
					}
					default -> others.add(member);
				}
			}
			ostream.accept("""
					Members of the unit:
					<ul>
					""");
			final IReportGenerator<IWorker> workerReportGenerator;
			if (item.owner().equals(currentPlayer)) {
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
				final Consumer<IResourcePile> lambda =
						member -> memberReportGenerator.produceSingle(fixtures, map, ostream, member, loc);
				for (final Map.Entry<String, List<IResourcePile>> entry : resources.entrySet()) {
					produceInner(fixtures, ostream, entry.getKey(), entry.getValue(),
							lambda);
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
							   final ILegacyMap map, final IUnit unit, final Point loc,
							   final Consumer<String> formatter) {
		atPoint(formatter, loc);
		formatter.accept(distanceString.apply(loc));
		produceSingle(fixtures, map, formatter, unit, loc);
	}

	/**
	 * Produce the part of the report on all units not covered as part of fortresses.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
						final ILegacyMap map, final Consumer<String> ostream) {
		final HeadedMap<IUnit, Point> foreign = new HeadedMapImpl<>("<h5>Foreign Units</h5>");
		final HeadedMap<IUnit, Point> ours = new HeadedMapImpl<>("<h5>Your units</h5>");
		for (final Pair<Point, IUnit> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IUnit)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (IUnit) p.getValue1())).toList()) {
			final IUnit unit = pair.getValue1();
			final Point loc = pair.getValue0();
			if (currentPlayer.equals(unit.owner())) {
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
