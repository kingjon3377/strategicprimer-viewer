package drivers.turnrunning.applets;

import legacy.map.IFixture;

import java.util.ArrayList;

import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.FortressMember;
import legacy.map.ILegacyMap;

import java.util.Comparator;
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

import org.jetbrains.annotations.Nullable;


// TODO: Most of these 'default' functions should probably go into a 'TurnRunningModel' interface
public abstract class AbstractTurnApplet implements TurnApplet {
	protected AbstractTurnApplet(final ITurnRunningModel model, final ICLIHelper cli) {
		this.model = model;
		this.cli = cli;
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected <Type> @Nullable Type chooseFromList(final List<Type> items, final String description, final String none,
												   final String prompt, final ICLIHelper.ListChoiceBehavior behavior) {
		return chooseFromList(items, description, none, prompt, behavior, Object::toString);
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected <Type> @Nullable Type chooseFromList(final List<Type> items, final String description, final String none,
												   final String prompt, final ICLIHelper.ListChoiceBehavior behavior,
												   final Function<? super Type, String> converter) {
		final Pair<Integer, @Nullable String> entry = cli.chooseStringFromList(
			items.stream().map(converter).collect(Collectors.toList()), description,
			none, prompt, behavior);
		// N.B. can't inline using Optional because we *test* the right side of the pair, then *use* the left side.
		if (entry.getValue1() == null) {
			return null;
		} else {
			return items.get(entry.getValue0());
		}
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected @Nullable Point confirmPoint(final String prompt) {
		final Point retval = cli.inputPoint(prompt);
		if (retval == null) {
			return null;
		}
		final Point selectedLocation = model.getSelectedUnitLocation();
		if (selectedLocation.isValid()) {
			final Boolean confirmation = cli.inputBoolean(
				String.format("%s is %.1f away. Is that right?", retval,
					model.getMapDimensions().distance(retval, selectedLocation)));
			if (Boolean.TRUE.equals(confirmation)) {
				return retval;
			} else {
				return null;
			}
		} else {
			cli.println("No base location, so can't estimate distance.");
			return retval;
		}
	}

	// TODO: These should be configurable, either by callers or the user's SPOptions
	protected static final int ENCOUNTERS_PER_HOUR = 4;
	protected static final int NO_RESULT_COST = 60 / ENCOUNTERS_PER_HOUR;

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	protected <T extends HasPopulation<? extends TileFixture> & TileFixture> void reducePopulation(
		final Point point, final T fixture, final String plural, final IFixture.CopyBehavior zero) {
		// TODO: make nullable and return null on EOF?
		final int count = Math.min(
			Optional.ofNullable(cli.inputNumber(String.format(
				"How many %s to remove: ", plural))).orElse(0), fixture.getPopulation());
		model.reducePopulation(point, fixture, zero, count);
	}

	// FIXME: Should only look at a particular unit's location
	// TODO: Move into the model?
	// FIXME: Stream-based version doesn't count food inside units inside fortresses
	protected List<IResourcePile> getFoodFor(final Player player, final int turn) {
/*		return model.getMap().streamAllFixtures()
			.filter(f -> f instanceof IFortress || f instanceof IUnit)
			.filter(f -> player.equals(((HasOwner) f).getOwner()))
			.map(FixtureIterable.class::cast)
			.flatMap(f -> f.stream())
			.filter(IResourcePile.class::isInstance)
//			.<IResourcePile>map(IResourcePile.class::cast)
			.map(x -> (IResourcePile) x)
			.filter((IResourcePile r) -> "food".equals(r.getKind()))
			.filter((IResourcePile r) -> "pounds".equals(r.getQuantity().getUnits()))
			.filter((IResourcePile r) -> r.getCreated() <= turn) // TODO: add sorting in this version
			.collect(Collectors.<IResourcePile>toList()); */ // Doesn't compile, with impossible errors
		final List<IResourcePile> retval = new ArrayList<>();
		final ILegacyMap map = model.getMap();
		for (final Point loc : map.getLocations()) {
			for (final TileFixture fix : map.getFixtures(loc)) {
				if (fix instanceof final IFortress fort) {
					for (final FortressMember member : fort) {
						if (member instanceof final IResourcePile pile && fort.owner().equals(player)) {
							if ("food".equals(pile.getKind()) &&
								"pounds".equals(pile.getQuantity()
									.units()) &&
								pile.getCreated() <= turn) {
								retval.add(pile);
							}
						} else if (member instanceof final IUnit unit && unit.owner().equals(player)) {
							for (final UnitMember inner : unit) {
								if (inner instanceof final IResourcePile pile) {
									if ("food".equals(pile.getKind()) &&
										"pounds".equals(
											pile.getQuantity()
												.units()) &&
										pile.getCreated()
											<= turn) {
										retval.add(pile);
									}
								}
							}
						}
					}
				} else if (fix instanceof final IUnit unit && unit.owner().equals(player)) {
					for (final UnitMember inner : unit) {
						if (inner instanceof final IResourcePile pile) {
							if ("food".equals(pile.getKind()) &&
								"pounds".equals(pile.getQuantity()
									.units()) &&
								pile.getCreated() <= turn) {
								retval.add(pile);
							}
						}
					}
				}
			}
		}
		retval.sort(Comparator.comparing(IResourcePile::getCreated));
		return retval;
	}
}
