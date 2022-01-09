package worker.common;

import common.map.fixtures.mobile.IUnit;
import java.util.EventListener;

/**
 * An interface for things that want to accept a new user-created unit.
 */
@FunctionalInterface
public interface NewUnitListener extends EventListener {
	/**
	 * Add the new unit.
	 */
	void addNewUnit(IUnit unit);
}
