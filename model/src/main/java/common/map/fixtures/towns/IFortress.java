package common.map.fixtures.towns;

import common.map.IFixture;
import common.map.Subsettable;
import common.map.HasName;
import common.map.HasImage;
import common.map.fixtures.FortressMember;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 *
 * FIXME: We need something about buildings yet
 */
public interface IFortress extends HasImage, ITownFixture, HasName,
		Iterable<FortressMember>, IFixture, Subsettable<IFixture> {
	/**
	 * Clone the fortress.
	 */
	@Override
	IFortress copy(boolean zero);

	/**
	 * The filename of the image to use as an icon when no per-instance icon has been specified.
	 *
	 * TODO: Should perhaps be more granular
	 */
	@Override 
	default String getDefaultImage() {
		return "fortress.png";
	}

	/**
	 * The status of the fortress.
	 *
	 * TODO: Add support for having a different status? (but leave 'active'
	 * the default) Or maybe a non-'active' fortress is a Fortification,
	 * and an active fortification is a Fortress.
	 */
	@Override
	default TownStatus getStatus() {
		return TownStatus.Active;
	}

	@Override
	default String getPlural() {
		return "Fortresses";
	}

	@Override
	default String getShortDescription() {
		if (getOwner().isCurrent()) {
			return String.format("a fortress, %s, owned by you", getName());
		} else if (getOwner().isIndependent()) {
			return "an independent fortress, " + getName();
		} else {
			return String.format("a fortress, %s, owned by %s", getName(),
				getOwner().getName());
		}
	}

	@Override
	default String getKind() {
		return "fortress";
	}
}
