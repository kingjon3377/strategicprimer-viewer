package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.ThrowingConsumer;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Implement;
import common.map.fixtures.FortressMember;
import common.map.fixtures.mobile.IUnit;

/**
 * A report generator for equipment and resources.
 */
public class FortressMemberReportGenerator extends AbstractReportGenerator<FortressMember> {

	private final Player currentPlayer;
	private final MapDimensions dimensions;
	private final int currentTurn;
	@Nullable
	private final Point hq;

	public FortressMemberReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                                     final MapDimensions dimensions, final int currentTurn) {
		this(comp, currentPlayer, dimensions, currentTurn, null);
	}

	public FortressMemberReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                                     final MapDimensions dimensions, final int currentTurn, @Nullable final Point hq) {
		super(comp, dimensions, hq);
		this.currentPlayer = currentPlayer;
		this.dimensions = dimensions;
		this.currentTurn = currentTurn;
		this.hq = hq;
	}

	/**
	 * Produces a sub-report on a resource or piece of equipment. All
	 * fixtures referred to in this report are removed from the collection.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final ThrowingConsumer<String, IOException> ostream, final FortressMember item, final Point loc)
			throws IOException {
	//	assert (is IUnit|IResourcePile|Implement item);
		if (item instanceof IUnit) {
			// TODO: Should be a field, right? Or else a constructor parameter?
			new UnitReportGenerator(pairComparator, currentPlayer, dimensions,
				currentTurn, hq).produceSingle(fixtures, map, ostream, (IUnit) item, loc);
		} else if (item instanceof Implement) {
			fixtures.remove(item.getId());
			ostream.accept("Equipment: ");
			ostream.accept(((Implement) item).getKind());
			if (((Implement) item).getCount() > 1) {
				ostream.accept(" (");
				ostream.accept(Integer.toString(((Implement) item).getCount()));
				ostream.accept(")");
			}
		} else if (item instanceof IResourcePile) {
			fixtures.remove(item.getId());
			ostream.accept("A pile of ");
			ostream.accept(((IResourcePile) item).getQuantity().toString());
			if (((IResourcePile) item).getQuantity().getUnits().isEmpty()) {
				ostream.accept(" ");
			} else {
				ostream.accept(" of ");
			}
			ostream.accept(((IResourcePile) item).getContents());
			ostream.accept(" (");
			ostream.accept(((IResourcePile) item).getKind());
			ostream.accept(")");
			if (((IResourcePile) item).getCreated() >= 0) {
				ostream.accept(" from turn ");
				ostream.accept(Integer.toString(((IResourcePile) item).getCreated()));
			}
		}
	}

	/**
	 * Produces a sub-report on all fortress members. All fixtures referred
	 * to in this report are removed from the collection. This method
	 * should probably never actually be called, since nearly all resources
	 * will be in fortresses and should be reported as such, but we'll
	 * handle this properly anyway.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final ThrowingConsumer<String, IOException> ostream) throws IOException {
		HeadedMap<Implement, Point> equipment = new HeadedMapImpl<>("<li>Equipment:",
			Comparator.comparing(Implement::getKind)
				.thenComparing(Comparator.comparing(Implement::getCount,
					Comparator.reverseOrder()))
				.thenComparing(Comparator.comparing(Implement::getId)));
		Map<String, HeadedMap<IResourcePile, Point>> resources = new HashMap<>();
		for (Pair<Point, FortressMember> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof IResourcePile ||
					p.getValue1() instanceof Implement)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (FortressMember) p.getValue1()))
				.collect(Collectors.toList())) {
			Point loc = pair.getValue0();
			FortressMember item = pair.getValue1();
			if (item instanceof IResourcePile) {
				IResourcePile resource = (IResourcePile) item;
				final HeadedMap<IResourcePile, Point> pileMap;
				if (resources.containsKey(resource.getKind())) {
					pileMap = resources.get(resource.getKind());
				} else {
					pileMap = new HeadedMapImpl<>(resource.getKind() + ":",
						Comparator.comparing(IResourcePile::getKind)
							.thenComparing(IResourcePile::getContents)
							.thenComparing(IResourcePile::getQuantity,
								Comparator.reverseOrder())
							.thenComparing(IResourcePile::getCreated)
							.thenComparing(IResourcePile::getId));
					resources.put(resource.getKind(), pileMap);
				}
				pileMap.put(resource, loc);
				fixtures.remove(resource.getId());
			} else if (item instanceof Implement) {
				Implement implement = (Implement) item;
				equipment.put(implement, loc); // TODO: Ensure it's not displacing anything (i.e. no duplicate equipment fixtures)
				fixtures.remove(implement.getId());
			}
		}
		if (!equipment.isEmpty() || !resources.isEmpty()) {
			ostream.accept("<h4>Resources and Equipment</h4>");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			writeMap(ostream, equipment, defaultFormatter(fixtures, map));
			if (!resources.isEmpty()) {
				ostream.accept("<li>Resources:<ul>");
				ostream.accept(System.lineSeparator());
				for (Map.Entry<String, HeadedMap<IResourcePile, Point>> entry :
						resources.entrySet()) {
					String kind = entry.getKey();
					HeadedMap<IResourcePile, Point> mapping = entry.getValue();
					ostream.accept("<li>");
					writeMap(ostream, mapping, defaultFormatter(fixtures, map));
					ostream.accept("</li>");
					ostream.accept(System.lineSeparator());
				}
				ostream.accept("</ul>");
				ostream.accept(System.lineSeparator());
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
	}
}
