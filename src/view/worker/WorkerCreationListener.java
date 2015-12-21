package view.worker;

import controller.map.misc.IDFactory;
import model.listeners.NewWorkerListener;
import model.listeners.UnitSelectionListener;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Worker;
import model.workermgmt.IWorkerTreeModel;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import static view.util.ErrorShower.showErrorDialog;

/**
 * A listener to keep track of the currently selected unit and listen for new-worker
 * notifications, then pass this information on to the tree model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerCreationListener implements ActionListener,
		                                                     UnitSelectionListener,
		                                                     NewWorkerListener {
	/**
	 * What to say to the user when a worker is created but no unit is selected.
	 */
	private static final String NO_UNIT_TEXT =
			"As no unit was selected, the new worker wasn't added to a unit.";
	/**
	 * The tree model.
	 */
	private final IWorkerTreeModel tmodel;
	/**
	 * The logger to use for logging.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(WorkerCreationListener
					                                                .class);
	/**
	 * The current unit. May be null, if nothing is selected.
	 */
	@Nullable
	private IUnit selUnit = null;
	/**
	 * The ID factory to pass to the worker-creation window.
	 */
	private final IDFactory idf;

	/**
	 * Constructor.
	 *
	 * @param treeModel the tree model
	 * @param idFac     the ID factory to pass to the worker-creation window.
	 */
	public WorkerCreationListener(final IWorkerTreeModel treeModel,
	                              final IDFactory idFac) {
		tmodel = treeModel;
		idf = idFac;
	}

	/**
	 * @param unit the newly selected unit.
	 */
	@Override
	public void selectUnit(@Nullable final IUnit unit) {
		selUnit = unit;
	}

	/**
	 * Handle button press.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if ((evt != null) &&
				    evt.getActionCommand().toLowerCase().startsWith("add worker")) {
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
		final IUnit local = selUnit;
		if (local == null) {
			LOGGER.warning("New worker created when no unit selected");
			showErrorDialog(null, NO_UNIT_TEXT);
		} else {
			tmodel.addUnitMember(local, worker);
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
