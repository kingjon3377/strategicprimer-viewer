package utility;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

import common.map.TileFixture;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.StreamSupport;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.javatuples.Pair;
import java.util.function.Consumer;
import java.nio.file.Path;
import common.map.HasKind;
import java.math.BigDecimal;

import common.map.IFixture;
import common.map.HasPopulation;
import common.map.TileFixture;
import common.map.HasExtent;
import common.map.Point;
import common.map.IMapNG;

import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Implement;

import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalImpl;

import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.FieldStatus;

import drivers.common.cli.ICLIHelper;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import common.map.fixtures.terrain.Forest;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce
 * the size it takes up on disk and the memory and CPU it takes to deal with
 * it).
 */
public class DuplicateFixtureRemoverCLI implements CLIDriver {
	private static String memberKind(@Nullable IFixture member) {
		if (member instanceof AnimalImpl || member instanceof Implement ||
				member instanceof Forest || member instanceof Grove ||
				member instanceof Meadow) {
			return ((HasKind) member).getKind();
		} else if (member == null) {
			return "null";
		} else if (member instanceof IResourcePile) {
			return ((IResourcePile) member).getContents();
		} else {
			return member.toString();
		}
	}

	/**
	 * A two-parameter wrapper around {@link HasExtent#combined}.
	 */
	private static <Type extends HasExtent<Type>> Type combineExtentImpl(Type one, Type two) {
		return one.combined(two);
	}

	/**
	 * A two-parameter wrapper around {@link HasPopulation#combined}.
	 */
	private static <Type extends HasPopulation<Type>> Type combine(Type one, Type two) {
		return one.combined(two);
	}

	/**
	 * Combine like extents into a single object. We assume all are identical except for acreage.
	 */
	private static <Type extends HasExtent<Type>> Type combineExtents(Type[] list) {
		return Stream.of(list).reduce(DuplicateFixtureRemoverCLI::combineExtentImpl)
			.orElseThrow(() -> new IllegalArgumentException("Can't combine an empty list"));
	}

	/**
	 * Combine like populations into a single object. We assume all are
	 * identical (i.e. of the same kind, and in the case of animals have
	 * the same domestication status and turn of birth) except for
	 * population.
	 */
	private static <Type extends HasPopulation<Type>> Type combinePopulations(Type[] list) {
		return Stream.of(list).reduce(DuplicateFixtureRemoverCLI::combine)
			.orElseThrow(() -> new IllegalArgumentException("Can't combine an empty list"));
	}

	// FIXME Move to lovelace.util
	private static BigDecimal decimalize(Number number) {
		if (number instanceof Integer || number instanceof Long
				|| number instanceof Short || number instanceof Byte) {
			return BigDecimal.valueOf(number.longValue());
		} else if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		} else {
			return BigDecimal.valueOf(number.doubleValue());
		}
	}

	/**
	 * Combine like resources into a single resource pile. We assume that
	 * all resources have the same kind, contents, units, and created
	 * date.
	 */
	private static IResourcePile combineResources(IResourcePile[] list) {
		if (list.length == 0) {
			throw new IllegalArgumentException("Can't combine an empty list");
		}
		IResourcePile top = list[0];
		IMutableResourcePile combined = new ResourcePileImpl(top.getId(), top.getKind(),
			top.getContents(), new Quantity(Stream.of(list).map(IResourcePile::getQuantity)
				.map(Quantity::getNumber).map(DuplicateFixtureRemoverCLI::decimalize)
				.reduce(BigDecimal.ZERO, BigDecimal::add), top.getQuantity().getUnits()));
		combined.setCreated(top.getCreated());
		return combined;
	}

	private final ICLIHelper cli;
	private final UtilityDriverModel model;
	@Override
	public UtilityDriverModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	public DuplicateFixtureRemoverCLI(ICLIHelper cli, UtilityDriverModel model) {
		this.cli = cli;
		this.model = model;
	}

	/**
	 * If {@link matching} is not null, ask the user whether to remove
	 * {@link fixture}, and return the user's answer (null on EOF). If
	 * {@link matching} is null, return false.
	 */
	@Nullable
	private Boolean approveRemoval(Point location, TileFixture fixture, @Nullable TileFixture matching) {
		if (matching != null) {
			String fCls = fixture.getClass().getName();
			String mCls = matching.getClass().getName();
			return cli.inputBooleanInSeries(
				String.format("At %s: Remove '%s', of class '%s', ID #%d, which matches '%s', of class '%s', ID #%d?",
					location, fixture.getShortDescription(), fCls, fixture.getId(),
					matching.getShortDescription(), mCls, matching.getId()),
				"duplicate``fCls````mCls``");
		} else {
			return false;
		}
	}

	/**
	 * "Remove" (at first we just report) duplicate fixtures (that is,
	 * hills, forests of the same kind, oases, etc.---we use {@link
	 * TileFixture#equalsIgnoringID}) from every tile in a map.
	 */
	private void removeDuplicateFixtures(IMapNG map) {
		for (Point location : model.getMap().getLocations()) {
			for (Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture,
					Iterable<? extends TileFixture>> q :
						model.conditionallyRemoveDuplicates(location)) {
				Consumer<TileFixture> deleteCallback = q.getValue0();
				Path file = q.getValue1();
				TileFixture fixture = q.getValue2();
				Iterable<? extends TileFixture> duplicates = q.getValue3();
				for (TileFixture duplicate : duplicates) {
					Boolean approval = approveRemoval(location, duplicate, fixture);
					if (approval == null) {
						return;
					} else if (approval) {
						deleteCallback.accept(duplicate);
					}
				}
			}
			coalesceResources(location);
		}
	}

	/**
	 * Offer to combine like resources in a unit or fortress.
	 */
	private void coalesceResources(Point location) {
		Map<Class<? extends IFixture>, CoalescedHolder<? extends IFixture, ?>> mapping =
			new HashMap<>();
		mapping.put(IResourcePile.class, new CoalescedHolder<IResourcePile,
				Quartet<String, String, String, Integer>>(IResourcePile.class,
			IResourcePile[]::new,
			pile -> Quartet.with(pile.getKind(), pile.getContents(),
				pile.getQuantity().getUnits(), pile.getCreated()),
			DuplicateFixtureRemoverCLI::combineResources));
		mapping.put(Animal.class, new CoalescedHolder<Animal, Triplet<String, String, Integer>>(
			Animal.class, Animal[]::new,
			animal -> Triplet.with(animal.getKind(), animal.getStatus(), animal.getBorn()),
			DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Implement.class, new CoalescedHolder<Implement, String>(Implement.class,
			Implement[]::new, Implement::getKind,
			DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Forest.class, new CoalescedHolder<Forest, Pair<String, Boolean>>(
			Forest.class, Forest[]::new, forest -> Pair.with(forest.getKind(), forest.isRows()),
			DuplicateFixtureRemoverCLI::combineExtents));
		mapping.put(Grove.class, new CoalescedHolder<Grove, Triplet<Boolean, Boolean, String>>(
			Grove.class, Grove[]::new,
			grove -> Triplet.with(grove.isOrchard(), grove.isCultivated(), grove.getKind()),
			DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Meadow.class, new CoalescedHolder<Meadow,
			Quartet<String, Boolean, Boolean, FieldStatus>>(Meadow.class, Meadow[]::new,
				meadow -> Quartet.with(meadow.getKind(), meadow.isField(),
					meadow.isCultivated(), meadow.getStatus()),
				DuplicateFixtureRemoverCLI::combineExtents));
		mapping.put(Shrub.class, new CoalescedHolder<Shrub, String>(Shrub.class, Shrub[]::new,
			Shrub::getKind, DuplicateFixtureRemoverCLI::combinePopulations));

		for (Quartet<Runnable, String, String, Iterable<? extends IFixture>> q :
				model.conditionallyCoalesceResources(location, mapping)) {
			Runnable callback = q.getValue0();
			String context = q.getValue1();
			String plural = q.getValue2();
			Iterable<? extends IFixture> fixtures = q.getValue3();
			cli.print(context);
			cli.println(String.format("The following %s can be combined:", plural));
			// TODO: Make model.conditionallyCoalesceResources() return Collection rather than Iterable
			StreamSupport.stream(fixtures.spliterator(), false).map(Object::toString)
				.forEach(cli::println);
			Boolean resp = cli.inputBooleanInSeries("Combine them? ",
				memberKind(fixtures.iterator().next()));
			if (resp == null) {
				return;
			} else if (resp) {
				callback.run();
			}
		}
	}

	/**
	 * Run the driver
	 */
	@Override
	public void startDriver() {
		if (model.getSubordinateMaps().iterator().hasNext()) {
			for (IMapNG map : model.getAllMaps()) {
				removeDuplicateFixtures(map);
				model.setMapModified(map, true);
			}
		} else {
			removeDuplicateFixtures(model.getMap());
			model.setMapModified(true);
		}
	}
}
