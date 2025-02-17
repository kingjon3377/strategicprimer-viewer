package utility;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import lovelace.util.Decimalize;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.javatuples.Pair;

import java.util.function.Consumer;
import java.nio.file.Path;

import legacy.map.HasKind;

import java.math.BigDecimal;

import legacy.map.IFixture;
import legacy.map.HasPopulation;
import legacy.map.TileFixture;
import legacy.map.HasExtent;
import legacy.map.Point;
import legacy.map.ILegacyMap;

import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.Implement;

import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalImpl;

import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Shrub;

import drivers.common.cli.ICLIHelper;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import legacy.map.fixtures.terrain.Forest;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce
 * the size it takes up on disk and the memory and CPU it takes to deal with
 * it).
 */
public final class DuplicateFixtureRemoverCLI implements CLIDriver {
	private static String memberKind(final @Nullable IFixture member) {
		if (member instanceof AnimalImpl || member instanceof Implement ||
				member instanceof Forest || member instanceof Grove ||
				member instanceof Meadow) {
			return ((HasKind) member).getKind();
		} else if (Objects.isNull(member)) {
			return "null";
		} else if (member instanceof final IResourcePile rp) {
			return rp.getContents();
		} else {
			return member.toString();
		}
	}

	/**
	 * A two-parameter wrapper around {@link HasExtent#combined}.
	 */
	private static <Type extends HasExtent<Type>> Type combineExtentImpl(final Type one, final Type two) {
		return one.combined(two);
	}

	/**
	 * A two-parameter wrapper around {@link HasPopulation#combined}.
	 */
	private static <Type extends HasPopulation<Type>> Type combine(final Type one, final Type two) {
		return one.combined(two);
	}

	/**
	 * Combine like extents into a single object. We assume all are identical except for acreage.
	 */
	private static <Type extends HasExtent<Type>> Type combineExtents(final Type[] list) {
		return Stream.of(list).reduce(DuplicateFixtureRemoverCLI::combineExtentImpl)
				.orElseThrow(() -> new IllegalArgumentException("Can't combine an empty list"));
	}

	/**
	 * Combine like populations into a single object. We assume all are
	 * identical (i.e. of the same kind, and in the case of animals have
	 * the same domestication status and turn of birth) except for
	 * population.
	 */
	private static <Type extends HasPopulation<Type>> Type combinePopulations(final Type[] list) {
		return Stream.of(list).reduce(DuplicateFixtureRemoverCLI::combine)
				.orElseThrow(() -> new IllegalArgumentException("Can't combine an empty list"));
	}

	/**
	 * Combine like resources into a single resource pile. We assume that
	 * all resources have the same kind, contents, units, and created
	 * date.
	 */
	private static IResourcePile combineResources(final IResourcePile[] list) {
		if (list.length == 0) {
			throw new IllegalArgumentException("Can't combine an empty list");
		}
		final IResourcePile top = list[0];
		return new ResourcePileImpl(top.getId(), top.getKind(),
				top.getContents(), new LegacyQuantity(Stream.of(list).map(IResourcePile::getQuantity)
				.map(LegacyQuantity::number).map(Decimalize::decimalize)
				.reduce(BigDecimal.ZERO, BigDecimal::add), top.getQuantity().units()), top.getCreated());
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

	public DuplicateFixtureRemoverCLI(final ICLIHelper cli, final UtilityDriverModel model) {
		this.cli = cli;
		this.model = model;
	}

	/**
	 * If "matching" is not null, ask the user whether to remove
	 * the fixture, and return the user's answer. If
	 * "matching" is null, return false.
	 */
	private ICLIHelper.BooleanResponse approveRemoval(final Point location, final TileFixture fixture,
	                                                  final @Nullable TileFixture matching) {
		if (Objects.isNull(matching)) {
			return ICLIHelper.BooleanResponse.EOF;
		} else {
			final String fCls = fixture.getClass().getName();
			final String mCls = matching.getClass().getName();
			return cli.inputBooleanInSeries(
							"At %s: Remove '%s', of class '%s', ID #%d, which matches '%s', of class '%s', ID #%d?"
									.formatted(location, fixture.getShortDescription(), fCls, fixture.getId(),
											matching.getShortDescription(), mCls, matching.getId()),
					"duplicate%s%s".formatted(fCls, mCls));
		}
	}

	/**
	 * "Remove" (at first we just report) duplicate fixtures (that is,
	 * hills, forests of the same kind, oases, etc.---we use {@link
	 * TileFixture#equalsIgnoringID}) from every tile in a map.
	 */
	private void removeDuplicateFixtures(final ILegacyMap map) {
		for (final Point location : model.getMap().getLocations()) {
			for (final Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture,
					Iterable<? extends TileFixture>> q :
					model.conditionallyRemoveDuplicates(location)) {
				final Consumer<TileFixture> deleteCallback = q.getValue0();
				final Path file = q.getValue1();
				final TileFixture fixture = q.getValue2();
				final Iterable<? extends TileFixture> duplicates = q.getValue3();
				for (final TileFixture duplicate : duplicates) {
					switch (approveRemoval(location, duplicate, fixture)) {
						case YES -> {
							deleteCallback.accept(duplicate);
						}
						case NO -> { // Do nothing
						}
						case QUIT -> {
							return;
						}
						case EOF -> {
							return; // TODO: Somehow signal EOF to callers
						}
					}
				}
			}
			coalesceResources(location);
		}
	}

	/**
	 * Offer to combine like resources in a unit or fortress.
	 */
	private void coalesceResources(final Point location) {
		final Map<Class<? extends IFixture>, CoalescedHolder<? extends IFixture, ?>> mapping =
				new HashMap<>();
		mapping.put(IResourcePile.class, new CoalescedHolder<>(IResourcePile.class,
				IResourcePile[]::new,
				pile -> Quartet.with(pile.getKind(), pile.getContents(),
						pile.getQuantity().units(), pile.getCreated()),
				DuplicateFixtureRemoverCLI::combineResources));
		mapping.put(Animal.class, new CoalescedHolder<>(
				Animal.class, Animal[]::new,
				animal -> Triplet.with(animal.getKind(), animal.getStatus(), animal.getBorn()),
				DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Implement.class, new CoalescedHolder<>(Implement.class,
				Implement[]::new, Implement::getKind,
				DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Forest.class, new CoalescedHolder<>(
				Forest.class, Forest[]::new, forest -> Pair.with(forest.getKind(), forest.isRows()),
				DuplicateFixtureRemoverCLI::combineExtents));
		mapping.put(Grove.class, new CoalescedHolder<>(
				Grove.class, Grove[]::new,
				grove -> Triplet.with(grove.getType(), grove.getCultivation(), grove.getKind()),
				DuplicateFixtureRemoverCLI::combinePopulations));
		mapping.put(Meadow.class, new CoalescedHolder<>(Meadow.class, Meadow[]::new,
				meadow -> Quartet.with(meadow.getKind(), meadow.getType(),
						meadow.getCultivation(), meadow.getStatus()),
				DuplicateFixtureRemoverCLI::combineExtents));
		mapping.put(Shrub.class, new CoalescedHolder<>(Shrub.class, Shrub[]::new,
				Shrub::getKind, DuplicateFixtureRemoverCLI::combinePopulations));

		final Consumer<String> println = cli::println;
		for (final Quartet<Runnable, String, String, Collection<? extends IFixture>> q :
				model.conditionallyCoalesceResources(location, mapping)) {
			final Runnable callback = q.getValue0();
			final String context = q.getValue1();
			final String plural = q.getValue2();
			final Collection<? extends IFixture> fixtures = q.getValue3();
			cli.print(context);
			cli.printf("The following %s can be combined:%n", plural);
			fixtures.stream().map(Object::toString).forEach(println);
			switch (cli.inputBooleanInSeries("Combine them? ",
					memberKind(fixtures.iterator().next()))) {
				case YES -> {
					callback.run();
				}
				case NO -> { // Do nothing
				}
				case QUIT -> {
					return;
				}
				case EOF -> {
					return; // TODO: Somehow signal EOF to callers
				}
			}
		}
	}

	/**
	 * Run the driver
	 */
	@Override
	public void startDriver() {
		if (model.getSubordinateMaps().iterator().hasNext()) {
			for (final ILegacyMap map : model.getAllMaps()) {
				removeDuplicateFixtures(map);
				model.setMapModified(map, true);
			}
		} else {
			removeDuplicateFixtures(model.getMap());
			model.setMapModified(true);
		}
	}
}
