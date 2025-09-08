package drivers.turnrunning.applets;

import legacy.map.HasOwner;
import legacy.map.IFixture;

import legacy.map.fixtures.FixtureIterable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import java.util.List;

import drivers.common.cli.ICLIHelper;
import legacy.map.Point;
import legacy.map.Player;
import legacy.map.TileFixture;
import legacy.map.HasPopulation;

import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.mobile.IUnit;

import drivers.turnrunning.ITurnRunningModel;

import org.jspecify.annotations.Nullable;


// TODO: Most of these 'default' functions should probably go into a 'TurnRunningModel' interface
public abstract class AbstractTurnApplet implements TurnApplet {
	protected AbstractTurnApplet(final ITurnRunningModel model, final ICLIHelper cli) {
		this.model = model;
		this.cli = cli;
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected final <Type> @Nullable Type chooseFromList(final List<Type> items, final String description,
	                                                     final String none, final String prompt,
	                                                     final ICLIHelper.ListChoiceBehavior behavior) {
		return chooseFromList(items, description, none, prompt, behavior, Object::toString);
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected final <Type> @Nullable Type chooseFromList(final List<Type> items, final String description,
	                                                     final String none, final String prompt,
	                                                     final ICLIHelper.ListChoiceBehavior behavior,
	                                                     final Function<? super Type, String> converter) {
		final Pair<Integer, @Nullable String> entry = cli.chooseStringFromList(
				items.stream().map(converter).collect(Collectors.toList()), description,
				none, prompt, behavior);
		// N.B. can't use Optional to inline because we *test* the right side of the pair, then *use* the left side.
		if (Objects.isNull(entry.getValue1())) {
			return null;
		} else {
			return items.get(entry.getValue0());
		}
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected final @Nullable Point confirmPoint(final String prompt) {
		final Point selectedLocation = model.getSelectedUnitLocation();
		while (true) {
			final Point retval = cli.inputPoint(prompt);
			if (Objects.isNull(retval)) {
				return null;
			}
			if (selectedLocation.isValid()) {
				switch (cli.inputBoolean(
						"%s is %.1f away. Is that right?".formatted(retval,
								model.getMapDimensions().distance(retval, selectedLocation)))) {
					case YES -> {
						return retval;
					}
					case NO -> {
						continue;
					}
					case QUIT -> {
						return null;
					}
					case EOF -> {
						return null; // TODO: signal EOF to callers
					}
				}
			} else {
				cli.println("No base location, so can't estimate distance.");
				return retval;
			}
		}
	}

	// TODO: These should be configurable, either by callers or the user's SPOptions
	private static final int ENCOUNTERS_PER_HOUR = 4;
	protected static final int NO_RESULT_COST = 60 / ENCOUNTERS_PER_HOUR;

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	protected final <T extends HasPopulation<? extends TileFixture> & TileFixture> void reducePopulation(
			final Point point, final T fixture, final String plural, final IFixture.CopyBehavior zero) {
		// TODO: make nullable and return null on EOF?
		final int count = Math.min(
				Optional.ofNullable(cli.inputNumber("How many %s to remove: ".formatted(plural))).orElse(0),
				fixture.getPopulation());
		model.reducePopulation(point, fixture, zero, count);
	}

	// FIXME: Should only look at a particular unit's location
	// TODO: Move into the model?
	// FIXME: Stream-based version doesn't count food inside units inside fortresses
	protected final List<IResourcePile> getFoodFor(final Player player, final int turn) {
		return model.getMap().streamAllFixtures()
			.filter(f -> f instanceof IFortress || f instanceof IUnit)
			.filter(f -> player.equals(((HasOwner) f).owner()))
			.map(f -> (FixtureIterable<?>) f)
			.flatMap(FixtureIterable::stream)
			.filter(IResourcePile.class::isInstance)
			.map(IResourcePile.class::cast)
			.filter(r -> "food".equals(r.getKind()))
			.filter(r -> "pounds".equals(r.getQuantity().units()))
			.filter(r -> r.getCreated() <= turn) // TODO: add sorting in this version
			.collect(Collectors.<IResourcePile>toList());
	}
}
