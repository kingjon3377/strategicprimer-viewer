package legacy.map.fixtures.mobile;

import legacy.map.fixtures.Implement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lovelace.util.ArraySet;
import legacy.map.IFixture;
import legacy.map.Player;

import common.map.fixtures.mobile.worker.WorkerStats;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.IJob;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

/**
 * A worker (or soldier) in a unit. This is deliberately not a
 * {@link common.map.TileFixture TileFixture}: these
 * should only be part of a unit, not as a top-level tag.
 *
 * TODO: Convert some other {@link MobileFixture}s similarly?
 */
public class Worker implements IMutableWorker {
    /**
     * Whether neither of two collections of Jobs contains a nonempty Job the other does not.
     *
     * We can't take Collection because what's passed in is actually IWorker.
     */
    private static boolean jobSetsEqual(final Iterable<IJob> first, final Iterable<IJob> second) {
        final Collection<IJob> firstFiltered = StreamSupport.stream(first.spliterator(), true)
                .filter(j -> !j.isEmpty()).collect(Collectors.toSet());
        final Collection<IJob> secondFiltered = StreamSupport.stream(second.spliterator(), true)
                .filter(j -> !j.isEmpty()).collect(Collectors.toSet());
        return firstFiltered.containsAll(secondFiltered) &&
                secondFiltered.containsAll(firstFiltered);
    }

    /**
     * The set of Jobs the worker is trained or experienced in.
     */
    private final Set<IJob> jobSet;

    /**
     * The notes players have associaed with this worker
     */
    private final Map<Integer, String> notesImpl = new HashMap<>();

    private final List<Implement> equipmentImpl = new ArrayList<>();

    /**
     * The worker's ID number.
     */
    private final int id;

    /**
     * The worker's ID number.
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * The worker's name.
     */
    private final String name;

    /**
     * The worker's name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * The worker's race (elf, dwarf, human, etc.)
     */
    private final String race;

    /**
     * The worker's race (elf, dwarf, human, etc.)
     */
    public String getRace() {
        return race;
    }

    public Worker(final String name, final String race, final int id, final IJob... jobs) {
        this.name = name;
        this.race = race;
        this.id = id;
        jobSet = new ArraySet<>(Arrays.asList(jobs));
    }

    /**
     * The worker's stats.
     */
    private @Nullable WorkerStats stats = null;

    /**
     * The worker's stats.
     */
    @Override
    public @Nullable WorkerStats getStats() {
        return stats;
    }

    /**
     * Set the worker's stats.
     */
    public void setStats(final @Nullable WorkerStats stats) {
        this.stats = stats;
    }

    /**
     * The filename of an image to use as an icon for this instance.
     */
    private String image = "";

    /**
     * The filename of an image to use as an icon for this instance.
     */
    @Override
    public String getImage() {
        return image;
    }

    /**
     * Set the filename of an image to use as an icon for this instance.
     */
    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    /**
     * The filename of an image to use as a portrait for the worker.
     */
    private String portrait = "";

    /**
     * The filename of an image to use as a portrait for the worker.
     */
    @Override
    public String getPortrait() {
        return portrait;
    }

    /**
     * Set the filename of an image to use as a portrait for the worker.
     */
    @Override
    public void setPortrait(final String portrait) {
        this.portrait = portrait;
    }

    /**
     * Add a Job.
     */
    @Override
    public boolean addJob(final IJob job) {
        final int size = jobSet.size();
        jobSet.add(job);
        return size != jobSet.size();
    }

    /**
     * An iterator over the worker's Jobs.
     */
    @Override
    public Iterator<IJob> iterator() {
        return jobSet.iterator();
    }

    private @Nullable Animal mount = null;

    @Override
    public @Nullable Animal getMount() {
        return mount;
    }

    @Override
    public void setMount(final @Nullable Animal mount) {
        this.mount = mount;
    }

    @Override
    public boolean equalsIgnoringID(final IFixture fixture) {
        if (fixture instanceof final IWorker that) {
            return that.getName().equals(name) &&
                    jobSetsEqual(jobSet, that) &&
                    that.getRace().equals(race) &&
                    Objects.equals(stats, that.getStats()) &&
                    equipmentImpl.containsAll(that.getEquipment()) &&
                    that.getEquipment().containsAll(equipmentImpl) &&
                    Objects.equals(mount, that.getMount());
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IWorker that) {
            return that.getId() == id && equalsIgnoringID(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     * We only use the worker's name and race for {@link #toString}
     */
    @Override
    public String toString() {
        return ("human".equals(race)) ? name : String.format("%s, a %s", name, race);
    }

    /**
     * The filename of the icon to use by default. This is just for icons
     * in lists and such, not the map, since this isn't a {@link
     * common.map.TileFixture}.
     */
    @Override
    public String getDefaultImage() {
        return "worker.png";
    }

    /**
     * A fixture is a subset if it is a worker with the same ID, name,
     * race, and stats, and no Jobs we don't have, and its Jobs are subsets
     * of our corresponding Jobs.
     */
    @Override
    public boolean isSubset(final IFixture obj, final Consumer<String> report) {
        if (obj.getId() == id) {
            if (obj instanceof final IWorker that) {
                final Consumer<String> localReport =
                        s -> report.accept(String.format("In worker %s (ID #%d):\t%s",
                                name, id, s));
                if (!name.equals(that.getName())) {
                    localReport.accept("Names differ");
                    return false;
                } else if (!race.equals(that.getRace())) {
                    localReport.accept("Races differ");
                    return false;
                } else if (!Objects.equals(stats, that.getStats())) {
                    localReport.accept("Stats differ");
                    return false;
                }
                final Map<String, IJob> ours =
                        jobSet.stream().collect(Collectors.toMap(IJob::getName,
                                Function.identity()));
                boolean retval = true;
                for (final IJob job : that) {
                    if (ours.containsKey(job.getName())) {
                        if (!ours.get(job.getName()).isSubset(job, localReport)) {
                            retval = false;
                        }
                    } else if (!job.isEmpty()) {
                        localReport.accept("Extra Job: " + job.getName());
                        retval = false;
                    }
                }
                final Animal theirMount = that.getMount();
                if (theirMount != null) {
                    if (mount != null && !theirMount.equals(mount)) { // TODO: Use isSubset() instead?
                        localReport.accept("Mounts differ");
                        retval = false;
                    } else if (mount == null) {
                        localReport.accept("Has mount we don't");
                        retval = false;
                    }
                }
                for (final Implement item : that.getEquipment()) {
                    if (!equipmentImpl.contains(item)) { // TODO: Subset: a worker with 1 axe should be a subset of one with 2
                        localReport.accept("Extra equipment: " + item);
                        retval = false;
                    }
                }
                return retval;
            } else {
                report.accept(String.format("For ID #%d, different kinds of members", id));
                return false;
            }
        } else {
            report.accept(String.format("Called with different IDs, #%d and #%d",
                    id, obj.getId()));
            return false;
        }
    }

    /**
     * Clone the object.
     */
    @Override
    public Worker copy(final CopyBehavior zero) {
        final Worker retval = new Worker(name, race, id);
        retval.setImage(image);
        if (zero == CopyBehavior.KEEP) {
            final WorkerStats localStats = stats;
            if (localStats != null) {
                retval.setStats(localStats.copy());
            }
            for (final IJob job : this) {
                if (!job.isEmpty()) {
                    retval.addJob(job.copy());
                }
            }
            if (mount != null) {
                retval.setMount(mount.copy(zero));
            }
            for (final Implement item : equipmentImpl) {
                retval.addEquipment(item);
            }
            retval.notesImpl.putAll(notesImpl); // TODO: add setNote() overload taking int, so we don't have to violate encapsulation
        }
        return retval;
    }

    /**
     * Get a Job by name: the Job by that name the worker has, or a
     * newly-constructed one if it didn't have one.
     */
    @Override
    public IJob getJob(final String name) {
        final Optional<IJob> maybe = jobSet.stream().filter(j -> name.equals(j.getName())).findAny();
        if (maybe.isPresent()) {
            return maybe.get();
        } else {
            final IJob retval = new Job(name, 0);
            jobSet.add(retval);
            return retval;
        }
    }

    @Override
    public String getNote(final Player player) {
        // TODO: is "" right?
        return notesImpl.getOrDefault(player.getPlayerId(), "");
    }

    @Override
    public String getNote(final int player) {
        // TODO: is "" right?
        return notesImpl.getOrDefault(player, "");
    }

    @Override
    public void setNote(final Player player, final String note) {
        if (note.isEmpty()) {
            notesImpl.remove(player.getPlayerId());
        } else {
            notesImpl.put(player.getPlayerId(), note);
        }
    }

    @Override
    public Iterable<Integer> getNotesPlayers() {
        return notesImpl.keySet();
    }

    @Override
    public Collection<Implement> getEquipment() {
        return Collections.unmodifiableList(equipmentImpl);
    }

    @Override
    public void addEquipment(final Implement item) {
        equipmentImpl.add(item);
    }

    @Override
    public void removeEquipment(final Implement item) {
        equipmentImpl.remove(item);
    }
}
