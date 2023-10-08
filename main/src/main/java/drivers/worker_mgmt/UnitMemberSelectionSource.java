package drivers.worker_mgmt;

/**
 * An interface for objects that notify others of the user's selection of a unit member.
 */
public interface UnitMemberSelectionSource {
    /**
     * Add a listener.
     */
    void addUnitMemberListener(UnitMemberListener listener);

    /**
     * Remove a listener.
     */
    void removeUnitMemberListener(UnitMemberListener listener);
}
