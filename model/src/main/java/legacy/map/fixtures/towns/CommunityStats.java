package legacy.map.fixtures.towns;

import legacy.map.Subsettable;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.resources.HarvestableFixture;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * TODO: explain this class
 *
 * @author Jonathan Lovelace
 */
public interface CommunityStats extends Subsettable<@Nullable CommunityStats> {
	/**
	 * Approximately how many adults live in the community.
	 */
	int getPopulation();

	/**
	 * Set the population. Cannot be negative
	 */
	void setPopulation(int population);

	/**
	 * The highest Job (skill) levels in the community.
	 */
	Map<String, Integer> getHighestSkillLevels();

	/**
	 * Set the highest level in the community for the given Job
	 */
	void setSkillLevel(String skill, int level);

	/**
	 * ID numbers of fields, orchards, and the like that this community
	 * cultivates. We don't have references to the {@link
	 * HarvestableFixture} objects
	 * themselves, because that would require XML parsing to grow an
	 * additional pass, but every number here should be the ID number of a
	 * {@link HarvestableFixture} that is not
	 * claimed by any other community.
	 * <p>
	 * TODO: Use a BitSet/RoaringBitMap?
	 */
	Collection<Integer> getWorkedFields();

	/**
	 * Add a field (or orchard, or other harvestable resource source) (ID
	 * number) to the collection of worked fields.
	 */
	void addWorkedField(int fieldID);

	/**
	 * Remove a harvestable resource source (ID number) from the collection
	 * of such sources worked by this community
	 */
	void removeWorkedField(int fieldID);

	/**
	 * The set of resources produced each year.
	 */
	Set<IResourcePile> getYearlyProduction();

	void addYearlyProduction(IResourcePile resource);

	void removeYearlyProduction(IResourcePile resource);

	void replaceYearlyProduction(IResourcePile oldResource, IResourcePile newResource);

	/**
	 * The set of resources consumed each year. (Though substitutions of
	 * like resources are to be expected.)
	 */
	Set<IResourcePile> getYearlyConsumption();

	void addYearlyConsumption(IResourcePile resource);

	void removeYearlyConsumption(IResourcePile resource);

	void replaceYearlyConsumption(IResourcePile oldResource, IResourcePile newResource);
}
