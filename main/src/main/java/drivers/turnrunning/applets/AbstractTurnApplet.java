package drivers.turnrunning.applets;

import java.util.ArrayList;
import common.map.fixtures.UnitMember;
import common.map.fixtures.FortressMember;
import common.map.IMapNG;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import java.util.List;
import drivers.common.cli.ICLIHelper;
import common.idreg.IDRegistrar;
import common.map.Point;
import common.map.Player;
import common.map.TileFixture;
import common.map.HasPopulation;

import common.map.fixtures.towns.IFortress;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.mobile.IUnit;

import drivers.turnrunning.ITurnRunningModel;

import org.jetbrains.annotations.Nullable;


// TODO: Most of these 'default' functions should probably go into a 'TurnRunningModel' interface
public abstract class AbstractTurnApplet implements TurnApplet {
	protected AbstractTurnApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		this.model = model;
		this.cli = cli;
		this.idf = idf;
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected <Type> @Nullable Type chooseFromList(final List<Type> items, final String description, final String none,
	                                               final String prompt, final boolean auto) {
		return chooseFromList(items, description, none, prompt, auto, Object::toString);
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	protected <Type> @Nullable Type chooseFromList(final List<Type> items, final String description, final String none,
	                                               final String prompt, final boolean auto, final Function<? super Type, String> converter) {
		final Pair<Integer, @Nullable String> entry = cli.chooseStringFromList(
			items.stream().map(converter).collect(Collectors.toList()), description,
			none, prompt, auto);
		// N.B. can't inline using Optional because we *test* the right side of the pair, then *use* the left side.
		if (entry.getValue1() != null) {
			return items.get(entry.getValue0());
		} else {
			return null;
		}
	}

	// This was "shared" in Ceylon, but I expect only subclasses will be able to use it.
	@Nullable
	protected Point confirmPoint(final String prompt) {
		final Point retval = cli.inputPoint(prompt);
		if (retval == null) {
			return null;
		}
		final Point selectedLocation = model.getSelectedUnitLocation();
		if (selectedLocation.isValid()) {
			final Boolean confirmation = cli.inputBoolean(
				String.format("%s is %.1f away. Is that right?", retval,
					model.getMapDimensions().distance(retval, selectedLocation)));
			if (confirmation != null && confirmation) {
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
	protected final int encountersPerHour = 4;
	protected final int noResultCost = 60 / encountersPerHour;

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	protected <T extends HasPopulation<? extends TileFixture>&TileFixture> void reducePopulation(
			final Point point, final T fixture, final String plural, final Boolean zero) {
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
			.filter((IResourcePile r) -> r.getCreated() <= turn)
			.collect(Collectors.<IResourcePile>toList()); */ // Doesn't compile, with impossible errors
		final List<IResourcePile> retval = new ArrayList<>();
		final IMapNG map = model.getMap();
		for (final Point loc : map.getLocations()) {
			for (final TileFixture fix : map.getFixtures(loc)) {
				if (fix instanceof IFortress) {
					for (final FortressMember member : (IFortress) fix) {
						if (member instanceof IResourcePile) {
							final IResourcePile pile = (IResourcePile) member;
							if ("food".equals(pile.getKind()) &&
									"pounds".equals(pile.getQuantity()
										.getUnits()) &&
									pile.getCreated() <= turn) {
								retval.add(pile);
							}
						} else if (member instanceof IUnit) {
							for (final UnitMember inner : (IUnit) member) {
								if (inner instanceof IResourcePile) {
									final IResourcePile pile =
										(IResourcePile) inner;
									if ("food".equals(pile.getKind()) &&
											"pounds".equals(
												pile.getQuantity()
													.getUnits()) &&
											pile.getCreated()
												<= turn) {
										retval.add(pile);
									}
								}
							}
						}
					}
				} else if (fix instanceof IUnit) {
					for (final UnitMember inner : (IUnit) fix) {
						if (inner instanceof IResourcePile) {
							final IResourcePile pile = (IResourcePile) inner;
							if ("food".equals(pile.getKind()) &&
									"pounds".equals(pile.getQuantity()
											.getUnits()) &&
									pile.getCreated() <= turn) {
								retval.add(pile);
							}
						}
					}
				}
			}
		}
		return retval;
	}
}
