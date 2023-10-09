package common.map.fixtures.mobile;

import java.util.NavigableMap;

import common.map.HasImage;
import common.map.IFixture;
import common.map.HasKind;
import common.map.HasName;
import common.map.HasOwner;
import common.map.HasPortrait;
import common.map.fixtures.UnitMember;
import common.map.fixtures.FortressMember;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.function.Consumer;

import common.map.fixtures.FixtureIterable;

/**
 * An interface for units.
 */
public interface IUnit extends MobileFixture, HasImage, HasKind, HasName,
        FixtureIterable<UnitMember>, FortressMember, HasOwner, HasPortrait {
    /**
     * The unit's orders history, a mapping from turns to the orders for those turns.
     */
    NavigableMap<Integer, String> getAllOrders();

    /**
     * Get the unit's orders for the given turn.
     */
    String getOrders(int turn);

    /**
     * The unit's results for the given turn.
     */
    String getResults(int turn);

    /**
     * The unit's results history, a mapping from turns to the results for those turns.
     */
    NavigableMap<Integer, String> getAllResults();

    /**
     * The unit's latest orders as of the given turn.
     */
    default String getLatestOrders(final int turn) {
        // map(i -> turn - i - 1) should reverse the range's order:
        // e.g. it maps (-1, 0, 1, 2, 3) to (3, 2, 1, 0, -1)
        return IntStream.rangeClosed(-1, turn).map(i -> turn - i - 1)
                .mapToObj(getAllOrders()::get).filter(Objects::nonNull)
                .map(String::trim).filter(s -> !s.isEmpty()).findFirst()
                .orElse("");
    }

    /**
     * The unit's latest results as of the given turn.
     */
    default String getLatestResults(final int turn) {
        // Reversing the order of the range as in getLatestOrders()
        return IntStream.rangeClosed(-1, turn).map(i -> turn - i - 1)
                .mapToObj(getAllResults()::get).filter(Objects::nonNull)
                .map(String::trim).filter(s -> !s.isEmpty()).findFirst()
                .orElse("");
    }

    /**
     * Get the latest turn that the given orders were the current orders.
     */
    default int getOrdersTurn(final String orders) {
        int retval = -1;
        for (final Map.Entry<Integer, String> entry : getAllOrders().entrySet()) {
            if (orders.equals(entry.getValue()) && entry.getKey() > retval) {
                retval = entry.getKey();
            }
        }
        return retval;
    }

    /**
     * A verbose description of the unit.
     */
    String getVerbose();

    /**
     * Clone the unit.
     */
    @Override
    IUnit copy(CopyBehavior zero);

    /**
     * The plural of Unit is Units
     */
    @Override
    default String getPlural() {
        return "Units";
    }

    /**
     * Whether there are any members.
     */
    boolean isEmpty();

    /**
     * A fixture is a subset if it is a unit with the same ID and no extra
     * members, and all corresponding (by ID, presumably) members are
     * either equal or themselves subsets.
     */
    @Override
    default boolean isSubset(final IFixture obj, final Consumer<String> report) {
        if (obj.getId() == getId()) {
            if (obj instanceof final IUnit that) {
                final Consumer<String> localSimpleReport =
                        s -> report.accept(String.format("In Unit of ID #%d:\t%s",
                                getId(), s));
                if (owner().getPlayerId() != that.owner().getPlayerId()) {
                    localSimpleReport.accept("Owners differ");
                    return false;
                } else if (!getName().equals(that.getName())) {
                    localSimpleReport.accept("Names differ");
                    return false;
                } else if (!getKind().equals(that.getKind())) {
                    localSimpleReport.accept("Kinds differ");
                    return false;
                }
                final Map<Integer, UnitMember> ours =
                        stream().collect(Collectors.toMap(IFixture::getId, m -> m));
                boolean retval = true;
                final Consumer<String> localReport =
                        s -> report.accept(String.format("In unit of %s (%s) (ID #%d):\t%s",
                                getName(), getKind(), getId(), s));
                for (final UnitMember member : that) {
                    if (ours.containsKey(member.getId())) {
                        if (!ours.get(member.getId()).isSubset(member, localReport)) {
                            retval = false;
                        }
                    } else {
                        localReport.accept(String.format("Extra member: %s, ID #%d",
                                member, member.getId()));
                        retval = false;
                    }
                }
                if (retval) {
                    if (("unassigned".equals(getName()) || "unassigned".equals(getKind()))
                            && !isEmpty() && that.isEmpty()) {
                        localReport.accept(
                                "Non-empty \"unassigned\" when submap has it empty");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                report.accept("Different kinds of fixtures for ID #" + getId());
                return false;
            }
        } else {
            report.accept("Fixtures have different IDs");
            return false;
        }
    }
}
