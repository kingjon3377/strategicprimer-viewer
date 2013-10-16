package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import model.listeners.CompletionListener;
import model.listeners.NewWorkerListener;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import util.TypesafeLogger;
import view.util.ErrorShower;
import controller.map.misc.IDFactory;

/**
 * A listener to keep track of the currently selected unit and listen for
 * new-worker notifications, then pass this information on to the tree
 * model.
 * @author Jonathan Lovelace
 */
public class WorkerCreationListener implements ActionListener,
		CompletionListener, NewWorkerListener {
	/**
	 * The tree model.
	 */
	private final IWorkerTreeModel tmodel;
	/**
	 * The logger to use for logging.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(WorkerCreationListener.class);
	/**
	 * The current unit. May be null, if nothing is selected.
	 */
	@Nullable private Unit selUnit;
	/**
	 * The ID factory to pass to the worker-creation window.
	 */
	private final IDFactory idf;
	/**
	 * Constructor.
	 * @param treeModel the tree model
	 * @param idFac the ID factory to pass to the worker-creation window.
	 */
	public WorkerCreationListener(final IWorkerTreeModel treeModel, final IDFactory idFac) {
		tmodel = treeModel;
		idf = idFac;
	}
	/**
	 * @param result the new value to stop waiting on (the newly selected unit, or the newly created worker)
	 */
	@Override
	public void stopWaitingOn(final Object result) {
		if ("null_unit".equals(result)) {
			selUnit = null;
		} else if (result instanceof Unit) {
			selUnit = (Unit) result;
		}
	}
	/**
	 * Handle button press.
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null && AdvancementFrame.NEW_WORKER_ACTION.equalsIgnoreCase(evt.getActionCommand())) {
			final WorkerConstructionFrame frame = new WorkerConstructionFrame(
					idf);
			frame.addNewWorkerListener(this);
			frame.setVisible(true);
		}
	}
	/**
	 * Handle a new user-created worker.
	 * @param worker the worker to handle
	 */
	@Override
	public void addNewWorker(final Worker worker) {
		final Unit locSelUnit = selUnit;
		if (locSelUnit == null) {
			LOGGER.warning("New worker created when no unit selected");
			ErrorShower.showErrorDialog(null, "The new worker was not added to a unit because no unit was selected.");
		} else {
			tmodel.addUnitMember(locSelUnit, worker);
		}
	}

}