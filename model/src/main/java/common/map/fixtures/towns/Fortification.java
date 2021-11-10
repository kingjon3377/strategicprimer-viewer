package common.map.fixtures.towns;

import common.map.Player;

/**
 * An abandoned, ruined, or burned-out fortification.
 *
 * FIXME: We want this to share a tag, and model code, with Fortress. Maybe an
 * active Fortification is a Fortress, and a non-active Fortress is a
 * Fortification?
 */
public class Fortification extends AbstractTown {
	/**
	 * @param townStatus The status of the fortification
	 * @param size The size of the fortification
	 * @param discoverDC The DC to discover the fortification
	 * @param townName The name of the fortification
	 * @param id The fortification's ID number
	 * @param player The owner of the fortification
	 */
	public Fortification(TownStatus townStatus, TownSize size, int discoverDC,
			String townName, int id, Player player) {
		super(townStatus, size, townName, player, discoverDC);
		this.id = id;
	}

	/**
	 * The fortification's ID number
	 */
	private final int id;

	/**
	 * The fortification's ID number
	 */
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getDefaultImage() {
		return "fortification.png";
	}

	@Override
	public String getPlural() {
		return "Fortifications";
	}

	@Override
	public String getKind() {
		return "fortification";
	}

	@Override
	public Fortification copy(boolean zero) {
		final Fortification retval = new Fortification(getStatus(), getTownSize(),
			(zero) ? 0 : getDC(), getName(), id, getOwner());
		retval.setImage(getImage());
		if (!zero) {
			retval.setPopulation(getPopulation());
		}
		return retval;
	}
}
