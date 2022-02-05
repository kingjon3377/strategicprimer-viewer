package common.map.fixtures.towns;

import common.map.Player;

/**
 * An abandoned, ruined, or burned-out (or active) town.
 */
public class Town extends AbstractTown {
	/**
	 * @param townStatus The status of the town
	 * @param size The size of the town
	 * @param discoverDC The DC to discover the town
	 * @param townName The name of the town
	 * @param id The town's ID number
	 * @param player The owner of the town
	 */
	public Town(final TownStatus townStatus, final TownSize size, final int discoverDC,
	            final String townName, final int id, final Player player) {
		super(townStatus, size, townName, player, discoverDC);
		this.id = id;
	}

	/**
	 * The town's ID number
	 */
	private final int id;

	/**
	 * The town's ID number
	 */
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getPlural() {
		return "Towns";
	}

	@Override
	public String getKind() {
		return "town";
	}

	@Override
	public String getDefaultImage() {
		return "town.png";
	}

	@Override
	public Town copy(final boolean zero) {
		final Town retval = new Town(getStatus(), getTownSize(),
			(zero) ? 0 : getDC(), getName(), id, getOwner());
		retval.setImage(getImage());
		if (!zero) {
			retval.setPopulation(getPopulation());
		}
		return retval;
	}
}
