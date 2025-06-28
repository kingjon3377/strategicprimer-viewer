package drivers.advancement;

import drivers.worker_mgmt.UnitSelectionListener;
import legacy.map.fixtures.mobile.IWorker;

import java.awt.event.ActionListener;

/**
 * An interface for classes that respond to the creation of new worker objects.
 *
 * @author Jonathan Lovelace
 */
public interface IWorkerCreationListener extends ActionListener, UnitSelectionListener {
	@SuppressWarnings("TypeMayBeWeakened") // Don't change public API
	void addNewWorker(IWorker worker);
}
