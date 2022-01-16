package utility;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import common.map.Player;
import common.map.HasOwner;
import common.map.Point;
import common.map.IMapNG;

import common.map.fixtures.towns.ITownFixture;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import drivers.common.cli.ICLIHelper;

/**
 * A driver to update a player's map to include a certain minimum distance around allied villages.
 *
 * FIXME: Write GUI for map-expanding driver
 */
public class ExpansionDriver implements CLIDriver {
	public ExpansionDriver(ICLIHelper cli, SPOptions options, UtilityDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;
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

	private Predicate<Point> containsSwornVillage(IMapNG map, Player currentPlayer) {
		return (point) -> map.getFixtures(point).stream().filter(ITownFixture.class::isInstance)
			.map(ITownFixture.class::cast).map(HasOwner::getOwner)
			.anyMatch(currentPlayer::equals);
	}
	
	private static <T> Predicate<T> not(Predicate<T> pred) {
		return t -> !pred.test(t);
	}

	@Override
	public void startDriver() {
		for (Player player : StreamSupport.stream(model.getSubordinateMaps().spliterator(), false)
				.map(IMapNG::getCurrentPlayer).filter(not(Player::isIndependent))
				.collect(Collectors.toList())) { // TODO: Why not distinct()?
			for (Point point : StreamSupport.stream(model.getMap().getLocations().spliterator(),
					false).filter(containsSwornVillage(model.getMap(), player))
					.collect(Collectors.toList())) {
				model.expandAroundPoint(point, player);
			}
		}
	}
}