package common.map.fixtures.towns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.function.Consumer;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import common.map.fixtures.IResourcePile;
import common.map.Subsettable;

import org.jetbrains.annotations.Nullable;

/**
 * TODO: Convert to an interface, or otherwise provide an alternative for
 * non-active communities to suggest contents to be found there
 *
 * TODO: Allow towns to contain {@link common.map.fixtures.mobile.Worker}, so
 * inhabitants players know about can be represented
 *
 * TODO: Convert to immutable with copy-with-mutation methods?
 */
public final class CommunityStats implements Subsettable<@Nullable CommunityStats> {
	public CommunityStats(final int population) {
		setPopulation(population);
	}
	/**
	 * Approximately how many adults live in the community.
	 */
	private int population;

	/**
	 * Population cannot be negative
	 */

	/**
	 * Approximately how many adults live in the community.
	 */
	public int getPopulation() {
		return population;
	}

	/**
	 * Set the population. Cannot be negative
	 */
	public void setPopulation(final int population) {
		if (population < 0) {
			throw new IllegalArgumentException("Population cannot be negative");
		}
		this.population = population;
	}

	private final Map<String, Integer> skillLevels = new HashMap<String, Integer>();

	/**
	 * The highest Job (skill) levels in the community.
	 */
	public Map<String, Integer> getHighestSkillLevels() {
		return Collections.unmodifiableMap(skillLevels);
	}

	/**
	 * Set the highest level in the community for the given Job
	 */
	public void setSkillLevel(final String skill, final int level) {
		if (level < 0) {
			throw new IllegalArgumentException(
				"Skill level cannot be negative; zero removes the skill entirely");
		} else if (level == 0) {
			skillLevels.remove(skill);
		} else {
			skillLevels.put(skill, level);
		}
	}

	/**
	 * ID numbers of fields, orchards, and the like that this community
	 * cultivates. We don't have references to the {@link
	 * common.map.fixtures.resources.HarvestableFixture} objects
	 * themselves, because that would require XML parsing to grow an
	 * additional pass, but every number here should be the ID number of a
	 * {@link common.map.fixtures.resources.HarvestableFixture} that is not
	 * claimed by any other community.
	 *
	 * TODO: Use a BitSet/RoaringBitMap?
	 */
	private final Set<Integer> workedFieldIDs = new HashSet<Integer>();

	/**
	 * ID numbers of fields, orchards, and the like that this community
	 * cultivates. We don't have references to the {@link
	 * common.map.fixtures.resources.HarvestableFixture} objects
	 * themselves, because that would require XML parsing to grow an
	 * additional pass, but every number here should be the ID number of a
	 * {@link common.map.fixtures.resources.HarvestableFixture} that is not
	 * claimed by any other community.
	 *
	 * TODO: Use a BitSet/RoaringBitMap?
	 */
	public Collection<Integer> getWorkedFields() {
		return Collections.unmodifiableSet(workedFieldIDs);
	}

	/**
	 * Add a field (or orchard, or other harvestable resource source) (ID
	 * number) to the collection of worked fields.
	 */
	public void addWorkedField(final int fieldID) {
		workedFieldIDs.add(fieldID);
	}

	/**
	 * Remove a harvestable resource source (ID number) from the collection
	 * of such sources worked by this community
	 */
	public void removeWorkedField(final int fieldID) {
		workedFieldIDs.remove(fieldID);
	}

	/**
	 * The set of resources produced each year.
	 *
	 * FIXME: Provide necessary mutator methods, so getter can wrap in unmodifiableSet
	 */
	private final Set<IResourcePile> yearlyProduction = new HashSet<IResourcePile>();

	/**
	 * The set of resources produced each year.
	 */
	public Set<IResourcePile> getYearlyProduction() {
//		return Collections.unmodifiableSet(yearlyProduction);
		return yearlyProduction;
	}

	/**
	 * The set of resources consumed each year. (Though substitutions of
	 * like resources are to be expected.)
	 *
	 * FIXME: Provide necessary mutator methods, so getter can wrap in unmodifiableSet
	 */
	private final Set<IResourcePile> yearlyConsumption = new HashSet<IResourcePile>();

	/**
	 * The set of resources consumed each year. (Though substitutions of
	 * like resources are to be expected.)
	 */
	public Set<IResourcePile> getYearlyConsumption() {
//		return Collections.unmodifiableSet(yearlyConsumption);
		return yearlyConsumption;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Community stats:");
		builder.append(System.lineSeparator());
		builder.append("Population: ").append(population);
		builder.append(System.lineSeparator());
		builder.append("Skills: ");
		for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
			builder.append("- ").append(entry.getKey()).append(": Level ")
				.append(entry.getValue());
			builder.append(System.lineSeparator());
		}
		builder.append("ID #s of worked fields: ");
		builder.append(String.join(", ",
			workedFieldIDs.stream().map(Object::toString).toArray(String[]::new)));
		builder.append(System.lineSeparator());
		builder.append("Yearly Resource Production:");
		for (IResourcePile resource : yearlyProduction) {
			builder.append("- ").append(resource);
			builder.append(System.lineSeparator());
		}
		builder.append("Yearly Resource Consumption:");
		builder.append(System.lineSeparator());
		for (IResourcePile resource : yearlyConsumption) {
			builder.append("- ").append(resource);
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof CommunityStats) {
			return population == ((CommunityStats) that).getPopulation() &&
				skillLevels.equals(((CommunityStats) that).getHighestSkillLevels()) &&
				workedFieldIDs.equals(((CommunityStats) that).getWorkedFields()) &&
				yearlyProduction.equals(((CommunityStats) that).getYearlyProduction()) &&
				yearlyConsumption.equals(((CommunityStats) that).getYearlyConsumption());
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(@Nullable final CommunityStats other, final Consumer<String> report) {
		if (other != null) {
			if (population < other.getPopulation()) {
				report.accept("Population is larger");
				return false;
			} else if (!workedFieldIDs.containsAll(other.getWorkedFields())) {
				report.accept("Has worked fields we don't");
				return false;
			} else {
				for (IResourcePile resource : other.getYearlyProduction()) {
					if (yearlyProduction.contains(resource) ||
							yearlyProduction.stream().anyMatch(
								r -> r.isSubset(resource, s -> {}))) {
						continue;
					} else {
						report.accept(
							"Produces a resource we don't, or more than we do: " +
								resource);
						return false;
					}
				}
				for (IResourcePile resource : other.getYearlyConsumption()) {
					if (yearlyConsumption.contains(resource) ||
							yearlyConsumption.stream().anyMatch(
								r -> r.isSubset(resource, s -> {}))) {
						continue;
					} else {
						report.accept(
							"Consumes a resource we don't, or more than we do: " +
								resource);
						return false;
					}
				}
				return true;
			}
		} else {
			return true;
		}
	}

	// Going for speed rather than excellent hashing
	@Override
	public int hashCode() {
		return population;
	}
}
