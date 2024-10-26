package legacy.map.fixtures.towns;

import legacy.map.Player;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.TownStatus;

/**
 * An abandoned, ruined, or burned-out (or active) city.
 */
public final class City extends AbstractTown {
	/**
	 * @param townStatus The status of the city
	 * @param size       The size of the city
	 * @param discoverDC The DC to discover the city
	 * @param townName   The name of the city
	 * @param id         The city's ID number
	 * @param player     The owner of the city
	 */
	public City(final TownStatus townStatus, final TownSize size, final int discoverDC,
	            final String townName, final int id, final Player player) {
		super(townStatus, size, townName, player, discoverDC);
		this.id = id;
	}

	/**
	 * The city's ID number
	 */
	private final int id;

	/**
	 * The city's ID number
	 */
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getPlural() {
		return "Cities";
	}

	@Override
	public String getKind() {
		return "city";
	}

	@Override
	public String getDefaultImage() {
		return "city.png";
	}

	@Override
	public City copy(final CopyBehavior zero) {
		final City retval = new City(getStatus(), getTownSize(), (zero == CopyBehavior.ZERO) ? 0 : getDC(),
				getName(), id, owner());
		retval.setImage(getImage());
		if (zero == CopyBehavior.KEEP) {
			retval.setPopulation(getPopulation());
		}
		return retval;
	}
}
