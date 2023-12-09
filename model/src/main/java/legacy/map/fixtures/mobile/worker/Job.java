package legacy.map.fixtures.mobile.worker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A Job a worker can work at or have training or experience in.
 */
public final class Job implements IMutableJob {
    public Job(final String name, final int levelNum, final ISkill... skills) {
        this.name = name;
        setLevel(levelNum);
        for (final ISkill skill : skills) {
            addSkill(skill);
        }
    }

    /**
     * The worker's level in various skills associated with the Job.
     */
    private final Map<String, ISkill> skillSet = new HashMap<>();

    /**
     * The name of the Job.
     */
    private final String name;

    /**
     * The name of the Job.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * How many levels the worker has in the Job.
     */
    private int levelNum;

    /**
     * How many levels the worker has in the Job.
     */
    @Override
    public int getLevel() {
        return levelNum;
    }

    /**
     * Set how many levels the worker has in the Job.
     *
     * Job level cannot be negative.
     *
     * TODO: Remove this in Java?
     */
    @Override
    public void setLevel(final int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Job level cannot be negative");
        }
        levelNum = level;
    }

    /**
     * Add a skill. Does nothing if an equal skill was already in the collection.
     *
     * TODO: What should we do with matching but non-equal skill?
     */
    @Override
    public void addSkill(final ISkill skill) {
        if (skillSet.containsKey(skill.getName()) &&
                Objects.equals(skill, skillSet.get(skill.getName()))) {
            return;
        } else {
            skillSet.put(skill.getName(), skill);
        }
    }

    private static ISkill copySkill(final ISkill skill) {
        return skill.copy();
    }

    /**
     * Clone the Job.
     */
    @Override
    public IJob copy() {
        return new Job(name, levelNum,
                skillSet.values().stream().map(ISkill::copy).toArray(ISkill[]::new));
    }

    /**
     * An iterator over (the worker's level in) the Skills in this Job.
     */
    @Override
    public Iterator<ISkill> iterator() {
        return skillSet.values().iterator();
    }

    /**
     * A Job is equal to another object iff it is a Job with the same name
     * and level and identical skills.
     *
     * TODO: Specify IJob instead of the Job class?
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Job that) {
            return name.equals(that.getName()) && levelNum == that.getLevel() &&
                    StreamSupport.stream(that.spliterator(), true)
                            .allMatch(skillSet.values()::contains) &&
                    StreamSupport.stream(that.spliterator(), true)
                            .collect(Collectors.toSet())
                            .containsAll(skillSet.values());
        } else {
            return false;
        }
    }

    /**
     * A Job is a "subset" if it has the same name, equal or lower level,
     * and no extra or higher-level or extra-experienced Skills.
     *
     * TODO: Perhaps a lower-level Job with extra skills should still be a subset?
     */
    @Override
    public boolean isSubset(final IJob obj, final Consumer<String> report) {
        if (!name.equals(obj.getName())) {
            report.accept("Passed Jobs with different names");
            return false;
        } else if (obj.getLevel() > levelNum) {
            report.accept("Submap has higher level for Job " + name);
            return false;
        } else {
            boolean retval = true;
            final Consumer<String> presentLambda =
                    s -> report.accept(String.format("In Job %s:\t%s", name, s));
            for (final ISkill skill : obj) {
                if (skillSet.containsKey(skill.getName())) {
                    retval = skillSet.get(skill.getName()).isSubset(skill,
                            presentLambda)
                            && retval;
                } else {
                    report.accept(String.format("In Job %s:\tExtra skill %s",
                            name, skill.getName()));
                    retval = false;
                }
            }
            return retval;
        }
    }

    /**
     * For stability, only the name is used to compute the hash value.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", name, levelNum);
    }

    /**
     * A Job is "empty" if the worker has no levels in it and no experience
     * in the skills it contains.
     */
    @Override
    public boolean isEmpty() {
        return levelNum == 0 && skillSet.values().stream().allMatch(ISkill::isEmpty);
    }

    /**
     * Get a Skill by name, or a newly-constructed empty one if we didn't have one.
     */
    @Override
    public ISkill getSkill(final String skillName) {
        if (skillSet.containsKey(skillName)) {
            return skillSet.get(skillName);
        } else {
            final ISkill skill = new Skill(skillName, 0, 0);
            skillSet.put(skillName, skill);
            return skill;
        }
    }

    /**
     * Remove a Skill from the Job.
     */
    @Override
    public void removeSkill(final ISkill skill) {
        skillSet.remove(skill.getName(), skill);
    }
}
