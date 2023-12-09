package legacy.map.fixtures.towns;

import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.TownStatus;
import legacy.map.HasKind;
import org.jetbrains.annotations.Nullable;

import legacy.map.TileFixture;
import common.map.HasPortrait;
import common.map.HasName;
import legacy.map.HasOwner;

/**
 * An interface for towns and similar fixtures.
 */
public interface ITownFixture extends TileFixture, HasName, HasOwner, HasPortrait, HasKind {
	/**
	 * The status of the town.
	 */
	TownStatus getStatus();

	/**
	 * The size of the town.
	 */
	TownSize getTownSize();

	/**
	 * A description of what kind of "town" this is.
	 */
	@Override
	String getKind();

	/**
	 * A summary of the town's contents.
	 */
	@Nullable
	CommunityStats getPopulation();
}
