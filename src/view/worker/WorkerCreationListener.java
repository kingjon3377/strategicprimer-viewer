package view.worker;

import static view.util.ErrorShower.showErrorDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import model.listeners.NewWorkerListener;
import model.listeners.UnitSelectionListener;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Worker;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import util.EmptyIterator;
import util.NullCleaner;
import util.TypesafeLogger;
import controller.map.misc.IDFactory;

/**
 * A listener to keep track of the currently selected unit and listen for
 * new-worker notifications, then pass this information on to the tree model.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerCreationListener implements ActionListener,
		UnitSelectionListener, NewWorkerListener {
	/**
	 * What to say to the user when a worker is created but no unit is selected.
	 */
	private static final String NO_UNIT_TEXT =
			"As no unit was selected, the new worker wasn't added to a unit.";
	/**
	 * The string "null".
	 */
	private static final String NULL_STR = "null";
	/**
	 * A typesafe equvalent of null, for when no unit is selected.
	 */
	private static final IUnit NULL_UNIT = new IUnit() {
		private final Player owner = new Player(-1, NULL_STR);
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
		@Override
		public int getZValue() {
			return 0;
		}
		@Override
		public String shortDesc() {
			return NULL_STR;
		}
		@Override
		public int getID() {
			return -1;
		}
		@Override
		public int compareTo(final TileFixture o) {
			return o.hashCode() - hashCode();
		}
		@Override
		public String getDefaultImage() {
			return "null.png";
		}
		@Override
		public void setImage(final String image) {
			throw new IllegalStateException("setImage called on null unit");
		}
		@Override
		public String getImage() {
			return "null.png";
		}
		@Override
		public String getKind() {
			return NULL_STR;
		}
		@Override
		public void setKind(final String nKind) {
			throw new IllegalStateException("setKind called on null unit");
		}
		@Override
		public Iterator<UnitMember> iterator() {
			return new EmptyIterator<>();
		}
		@Override
		public String getName() {
			return NULL_STR;
		}
		@Override
		public void setName(final String nomen) {
			// TODO Auto-generated method stub

		}
		@Override
		public Player getOwner() {
			return owner;
		}
		@Override
		public void setOwner(final Player player) {
			throw new IllegalStateException("setOwner called on null unit");
		}
		@Override
		public boolean isSubset(final IUnit obj, final Appendable ostream)
				throws IOException {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public String getOrders() {
			return "";
		}
		@Override
		public void setOrders(final String newOrders) {
			// Do nothing
		}
		@Override
		public void removeMember(final UnitMember member) {
			// Do nothing
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
	private IUnit selUnit;
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
	public void selectUnit(@Nullable final IUnit unit) {
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
			showErrorDialog(null, NO_UNIT_TEXT);
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
