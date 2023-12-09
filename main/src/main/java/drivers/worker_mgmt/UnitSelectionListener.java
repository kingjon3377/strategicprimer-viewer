package drivers.worker_mgmt;

import legacy.map.fixtures.mobile.IUnit;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for objects that want to know when the user selects a Unit from a list or tree.
 *
 * TODO: Combine with other similar interfaces?
 */
public interface UnitSelectionListener {
    /**
     * Respond to the fact that the given unit is the new selected unit.
     */
    void selectUnit(@Nullable IUnit unit);
}
