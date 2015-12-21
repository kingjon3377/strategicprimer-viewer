package view.worker;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.UnitMemberTransferable;
import model.workermgmt.UnitMemberTransferable.UnitMemberPair;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A replacement transfer handler to make drag-and-drop work properly.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Based on the tutorial found at http://www.javaprogrammingforums.com/java-swing
 * -tutorials/3141-drag-drop-jtrees.html
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 * @author helloworld922
 * @author Jonathan Lovelace
 */
public final class WorkerTreeTransferHandler extends TransferHandler {
	/**
	 * The tree's selection model.
	 */
	private final TreeSelectionModel smodel;
	/**
	 * The tree's data model.
	 */
	private final IWorkerTreeModel model;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(
					                                     WorkerTreeTransferHandler
							                                     .class);

	/**
	 * Constructor.
	 *
	 * @param selmodel the tree's selection model
	 * @param tmodel   the tree's data model
	 */
	protected WorkerTreeTransferHandler(final TreeSelectionModel selmodel,
	                                    final IWorkerTreeModel tmodel) {
		smodel = selmodel;
		model = tmodel;
	}

	/**
	 * @param component ignored
	 * @return the actions we support
	 */
	@Override
	public int getSourceActions(@Nullable final JComponent component) {
		return TransferHandler.MOVE;
	}

	/**
	 * @param component the component being dragged from? In any case, ignored.
	 * @return a Transferable representing the selected node, or null if none selected
	 */
	@Override
	@Nullable
	protected UnitMemberTransferable createTransferable(
			                                                   @Nullable
			                                                   final JComponent
					                                                   component) {
		final TreePath path = smodel.getSelectionPath();
		final Object last = path
				                    .getLastPathComponent();
		final Object parentPath = path.getPathComponent(path
				                                                .getPathCount() - 2);
		if ((last == null) || (parentPath == null)) {
			return null; // NOPMD
		}
		final Object selection = model.getModelObject(last);
		final Object parent = model.getModelObject(parentPath);
		if ((selection instanceof UnitMember) && (parent instanceof IUnit)) {
			return new UnitMemberTransferable((UnitMember) selection, // NOPMD
					                                 (IUnit) parent);
		} else {
			return null;
		}
	}

	/**
	 * @param support the object containing the detail of the transfer
	 * @return whether the drop is possible
	 */
	@Override
	public boolean canImport(@Nullable final TransferSupport support) {
		if ((support != null)
				    && support.isDataFlavorSupported(UnitMemberTransferable.FLAVOR)) {
			final DropLocation dloc = support.getDropLocation();
			if (!(dloc instanceof JTree.DropLocation)) {
				return false; // NOPMD
			}
			final TreePath path = ((JTree.DropLocation) dloc).getPath();
			if (path == null) {
				return false; // NOPMD
			} else {
				final Object pathLast = path.getLastPathComponent();
				return (pathLast != null) // NOPMD
						       && (model.getModelObject(pathLast) instanceof IUnit);
			}
		} else {
			return false;
		}
	}

	/**
	 * @param support the object containing the details of the transfer
	 * @return whether the transfer succeeded
	 */
	@Override
	public boolean importData(@Nullable final TransferSupport support) {
		if ((support != null) && canImport(support)) {
			final DropLocation dloc = support.getDropLocation();
			if (!(dloc instanceof JTree.DropLocation)) {
				return false; // NOPMD
			}
			final TreePath path = ((JTree.DropLocation) dloc).getPath();
			final Object pathLast = path.getLastPathComponent();
			if (pathLast == null) {
				return false; // NOPMD
			}
			final Object tempTarget = model.getModelObject(pathLast);
			if (tempTarget instanceof IUnit) {
				try {
					final Transferable trans = support.getTransferable();
					final UnitMemberTransferable.UnitMemberPair pair =
							(UnitMemberPair) trans
									                 .getTransferData(
											                 UnitMemberTransferable
													                 .FLAVOR);
					model.moveMember(pair.member, pair.unit, (IUnit) tempTarget);
					return true; // NOPMD
				} catch (final UnsupportedFlavorException except) {
					LOGGER.log(Level.SEVERE,
							"Impossible unsupported data flavor", except);
					return false; // NOPMD
				} catch (final IOException except) {
					LOGGER.log(
							Level.SEVERE,
							"I/O error in transfer after we checked",
							except);
					return false; // NOPMD
				}
			} else {
				return false; // NOPMD
			}
		} else {
			return false;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerTreeTransferHandler";
	}
}
