package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import model.listeners.NewWorkerListener;
import model.listeners.UnitSelectionListener;
import model.map.IFixture;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.TypesafeLogger;
import view.util.ErrorShower;
import controller.map.misc.IDFactory;

/**
 * A listener to keep track of the currently selected unit and listen for
 * new-worker notifications, then pass this information on to the tree model.
 *
 * @author Jonathan Lovelace
 */
public class WorkerCreationListener implements ActionListener,
		UnitSelectionListener, NewWorkerListener {
	/**
	 * The string "null".
	 */
	private static final String NULL_STR = "null";
	/**
	 * A typesafe equvalent of null, for when no unit is selected.
	 */
	private static final Unit NULL_UNIT = new Unit(new Player(-1, NULL_STR),
			NULL_STR, NULL_STR, -1) {
		// ESCA-JAVA0025:
		@Override
		public void addMember(final UnitMember member) {
			// Do nothing
		}
		@Override
		public boolean equals(@Nullable final Object obj) {
			return this == obj;
		}
		@Override
		public int hashCode() {
			return -1;
		}
		@Override
		public String toString() {
			return NULL_STR;
		}
		@Override
		public String verbose() {
			return NULL_STR;
		}
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return this == fix;
		}
		@Override
		public String plural() {
			return NULL_STR;
		}
	};
	/**
	 * The tree model.
	 */
	private final IWorkerTreeModel tmodel;
	/**
	 * The logger to use for logging.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(WorkerCreationListener.class);
	/**
	 * The current unit. May be null, if nothing is selected.
	 */
	private Unit selUnit;
	/**
	 * The ID factory to pass to the worker-creation window.
	 */
	private final IDFactory idf;

	/**
	 * Constructor.
	 *
	 * @param treeModel the tree model
	 * @param idFac the ID factory to pass to the worker-creation window.
	 */
	public WorkerCreationListener(final IWorkerTreeModel treeModel,
			final IDFactory idFac) {
		tmodel = treeModel;
		idf = idFac;
		selUnit = NULL_UNIT;
	}

	/**
	 * @param unit the newly selected unit.
	 */
	@Override
	public void selectUnit(@Nullable final Unit unit) {
		selUnit = NullCleaner.valueOrDefault(unit, NULL_UNIT);
	}
	/**
	 * Handle button press.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null
				&& AdvancementFrame.NEW_WORKER.equalsIgnoreCase(evt
						.getActionCommand())) {
			final WorkerConstructionFrame frame = new WorkerConstructionFrame(
					idf);
			frame.addNewWorkerListener(this);
			frame.setVisible(true);
		}
	}

	/**
	 * Handle a new user-created worker.
	 *
	 * @param worker the worker to handle
	 */
	@Override
	public void addNewWorker(final Worker worker) {
		if (NULL_UNIT.equals(selUnit)) {
			LOGGER.warning("New worker created when no unit selected");
			ErrorShower
					.showErrorDialog(null,
							"The new worker was not added to a unit because no unit was selected.");
		} else {
			tmodel.addUnitMember(selUnit, worker);
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerCreationListener";
	}
}
