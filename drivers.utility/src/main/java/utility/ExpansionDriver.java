package utility;

import java.util.stream.Collectors;
import java.util.function.Predicate;

import legacy.map.Player;
import legacy.map.HasOwner;
import legacy.map.Point;
import legacy.map.ILegacyMap;

import legacy.map.fixtures.towns.ITownFixture;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import static java.util.function.Predicate.not;

/**
 * A driver to update a player's map to include a certain minimum distance around allied villages.
 *
 * FIXME: Write GUI for map-expanding driver
 */
public final class ExpansionDriver implements CLIDriver {
	public ExpansionDriver(final SPOptions options, final UtilityDriverModel model) {
		this.options = options;
		this.model = model;
	}

	private final SPOptions options;
	private final UtilityDriverModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public UtilityDriverModel getModel() {
		return model;
	}

	private static Predicate<Point> containsSwornVillage(final ILegacyMap map, final Player currentPlayer) {
		return (point) -> map.getFixtures(point).stream().filter(ITownFixture.class::isInstance)
				.map(ITownFixture.class::cast).map(HasOwner::owner)
				.anyMatch(currentPlayer::equals);
	}

	@Override
	public void startDriver() {
		for (final Player player : model.streamSubordinateMaps()
				.map(ILegacyMap::getCurrentPlayer).filter(not(Player::isIndependent)).collect(Collectors.toSet())) {
			for (final Point point : model.getMap().streamLocations()
					.filter(containsSwornVillage(model.getMap(), player)).toList()) {
				model.expandAroundPoint(point, player);
			}
		}
	}
}
