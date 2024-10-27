package drivers.advancement;

import drivers.worker_mgmt.UnitSelectionListener;
import legacy.map.fixtures.mobile.IWorker;

import java.awt.event.ActionListener;

/**
 * TODO: explain this class
 *
 * @author Jonathan Lovelace
 */
public interface IWorkerCreationListener extends ActionListener, UnitSelectionListener {
	@SuppressWarnings("TypeMayBeWeakened") // Don't change public API
	void addNewWorker(IWorker worker);
}
