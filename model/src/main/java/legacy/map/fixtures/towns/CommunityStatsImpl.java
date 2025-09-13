package legacy.map.fixtures.towns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.function.Consumer;

import legacy.map.IFixture;
import legacy.map.fixtures.IResourcePile;

import legacy.map.fixtures.resources.HarvestableFixture;
import org.jspecify.annotations.Nullable;

/**
 * TODO: Convert to an interface, or otherwise provide an alternative for
 * non-active communities to suggest contents to be found there
 *
 * TODO: Allow towns to contain {@link legacy.map.fixtures.mobile.Worker}, so
 * inhabitants players know about can be represented
 *
 * TODO: Convert to immutable with copy-with-mutation methods?
 */
public final class CommunityStatsImpl implements CommunityStats {
	public CommunityStatsImpl(final int population) {
		setPopulation(population);
	}

	/**
	 * Approximately how many adults live in the community.
	 */
	private int population;

	/**
	 * Approximately how many adults live in the community.
	 */
	@Override
	public int getPopulation() {
		return population;
	}

	/**
	 * Set the population. Cannot be negative
	 */
	@Override
	public void setPopulation(final int population) {
		if (population < 0) {
			throw new IllegalArgumentException("Population cannot be negative");
		}
		this.population = population;
	}

	private final Map<String, Integer> skillLevels = new HashMap<>();

	/**
	 * The highest Job (skill) levels in the community.
	 */
	@Override
	public Map<String, Integer> getHighestSkillLevels() {
		return Collections.unmodifiableMap(skillLevels);
	}

	/**
	 * Set the highest level in the community for the given Job
	 */
	@Override
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
	 * HarvestableFixture} objects
	 * themselves, because that would require XML parsing to grow an
	 * additional pass, but every number here should be the ID number of a
	 * {@link HarvestableFixture} that is not
	 * claimed by any other community.
	 *
	 * TODO: Use a BitSet/RoaringBitMap?
	 */
	private final Set<Integer> workedFieldIDs = new HashSet<>();

	/**
	 * ID numbers of fields, orchards, and the like that this community
	 * cultivates. We don't have references to the {@link
	 * HarvestableFixture} objects
	 * themselves, because that would require XML parsing to grow an
	 * additional pass, but every number here should be the ID number of a
	 * {@link HarvestableFixture} that is not
	 * claimed by any other community.
	 *
	 * TODO: Use a BitSet/RoaringBitMap?
	 */
	@Override
	public Collection<Integer> getWorkedFields() {
		return Collections.unmodifiableSet(workedFieldIDs);
	}

	/**
	 * Add a field (or orchard, or other harvestable resource source) (ID
	 * number) to the collection of worked fields.
	 */
	@Override
	public void addWorkedField(final int fieldID) {
		workedFieldIDs.add(fieldID);
	}

	/**
	 * Remove a harvestable resource source (ID number) from the collection
	 * of such sources worked by this community
	 */
	@Override
	public void removeWorkedField(final int fieldID) {
		workedFieldIDs.remove(fieldID);
	}

	/**
	 * The set of resources produced each year.
	 */
	private final Set<IResourcePile> yearlyProduction = new HashSet<>();

	/**
	 * The set of resources produced each year.
	 */
	@Override
	public Set<IResourcePile> getYearlyProduction() {
		return Collections.unmodifiableSet(yearlyProduction);
	}

	@Override
	public void addYearlyProduction(final IResourcePile resource) {
		yearlyProduction.add(resource);
	}

	@Override
	public void removeYearlyProduction(final IResourcePile resource) {
		yearlyProduction.remove(resource);
	}

	@Override
	public void replaceYearlyProduction(final IResourcePile oldResource, final IResourcePile newResource) {
		if (yearlyProduction.remove(oldResource)) {
			yearlyProduction.add(newResource);
		}
	}

	/**
	 * The set of resources consumed each year. (Though substitutions of
	 * like resources are to be expected.)
	 */
	private final Set<IResourcePile> yearlyConsumption = new HashSet<>();

	/**
	 * The set of resources consumed each year. (Though substitutions of
	 * like resources are to be expected.)
	 */
	@Override
	public Set<IResourcePile> getYearlyConsumption() {
		return Collections.unmodifiableSet(yearlyConsumption);
	}

	@Override
	public void addYearlyConsumption(final IResourcePile resource) {
		yearlyConsumption.add(resource);
	}

	@Override
	public void removeYearlyConsumption(final IResourcePile resource) {
		yearlyConsumption.remove(resource);
	}

	@Override
	public void replaceYearlyConsumption(final IResourcePile oldResource, final IResourcePile newResource) {
		if (yearlyConsumption.remove(oldResource)) {
			yearlyConsumption.add(newResource);
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Community stats:");
		builder.append(System.lineSeparator());
		builder.append("Population: ").append(population);
		builder.append(System.lineSeparator());
		builder.append("Skills: ");
		for (final Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
			builder.append("- ").append(entry.getKey()).append(": Level ")
					.append(entry.getValue());
			builder.append(System.lineSeparator());
		}
		builder.append("ID #s of worked fields: ");
		builder.append(String.join(", ",
				workedFieldIDs.stream().map(Object::toString).toArray(String[]::new)));
		builder.append(System.lineSeparator());
		builder.append("Yearly Resource Production:");
		for (final IResourcePile resource : yearlyProduction) {
			builder.append("- ").append(resource);
			builder.append(System.lineSeparator());
		}
		builder.append("Yearly Resource Consumption:");
		builder.append(System.lineSeparator());
		for (final IResourcePile resource : yearlyConsumption) {
			builder.append("- ").append(resource);
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof final CommunityStats it) {
			return population == it.getPopulation() &&
					skillLevels.equals(it.getHighestSkillLevels()) &&
					workedFieldIDs.equals(it.getWorkedFields()) &&
					yearlyProduction.equals(it.getYearlyProduction()) &&
					yearlyConsumption.equals(it.getYearlyConsumption());
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final @Nullable CommunityStats other, final Consumer<String> report) {
		if (Objects.isNull(other)) {
			return true;
		} else {
			if (population < other.getPopulation()) {
				report.accept("Population is larger");
				return false;
			} else if (!workedFieldIDs.containsAll(other.getWorkedFields())) {
				report.accept("Has worked fields we don't");
				return false;
			} else {
				for (final IResourcePile resource : other.getYearlyProduction()) {
					if (!yearlyProduction.contains(resource) &&
							yearlyProduction.stream().noneMatch(
									r -> r.isSubset(resource, s -> {
									}))) {
						report.accept(
								"Produces a resource we don't, or more than we do: " +
										resource);
						return false;
					}
				}
				for (final IResourcePile resource : other.getYearlyConsumption()) {
					if (!yearlyConsumption.contains(resource) &&
							yearlyConsumption.stream().noneMatch(
									r -> r.isSubset(resource, s -> {
									}))) {
						report.accept(
								"Consumes a resource we don't, or more than we do: " +
										resource);
						return false;
					}
				}
				return true;
			}
		}
	}

	// Going for speed rather than excellent hashing
	@Override
	public int hashCode() {
		return population;
	}

	@Override
	public CommunityStatsImpl copy() {
		final CommunityStatsImpl retval = new CommunityStatsImpl(population);
		for (final Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
			retval.setSkillLevel(entry.getKey(), entry.getValue());
		}
		workedFieldIDs.forEach(retval::addWorkedField);
		yearlyProduction.stream().map(pile -> pile.copy(IFixture.CopyBehavior.KEEP))
				.forEach(retval::addYearlyProduction);
		yearlyConsumption.stream().map(pile -> pile.copy(IFixture.CopyBehavior.KEEP))
				.forEach(retval::addYearlyConsumption);
		return retval;
	}
}
