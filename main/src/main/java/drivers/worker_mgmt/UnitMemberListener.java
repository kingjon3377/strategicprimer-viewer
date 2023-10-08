package drivers.worker_mgmt;

import java.util.EventListener;

import common.map.fixtures.UnitMember;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for objects that want to know when a new unit member (usually a worker) is selected.
 *
 * TODO: combine with similar interfaces?
 */
public interface UnitMemberListener extends EventListener {
    /**
     * Handle a change in the selected member.
     */
    void memberSelected(@Nullable UnitMember previousSelection, @Nullable UnitMember selected);
}
