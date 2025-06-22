package legacy.map.fixtures.towns;

import org.jetbrains.annotations.Nullable;

/**
 * An interface to add the 'population details' setter to town classes.
 *
 * @author Jonathan Lovelace
 */
public interface HasMutablePopulation extends ITownFixture {
	/**
	 * The contents of the town.
	 */
	void setPopulation(@Nullable CommunityStats population);
}
