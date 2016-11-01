package view.map.main;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import model.viewer.FixtureMatcher;
import util.IntTransferable;

/**
 * A transfer-handler to let the user drag items in the list to control Z-order.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class FixtureFilterTransferHandler extends TransferHandler {
	/**
	 * The type of data we support.
	 */
	private static final DataFlavor FLAVOR =
			new DataFlavor(FixtureMatcher.class, "FixtureMatcher");

	/**
	 * Whether a given drag/drop operation is supported
	 * @param support the information about the current operation
	 * @return whether it's supported
	 */
	@Override
	public boolean canImport(final TransferSupport support) {
		if (support.isDrop() && support.isDataFlavorSupported(FLAVOR) &&
					(TransferHandler.MOVE & support.getSourceDropActions()) ==
							TransferHandler.MOVE) {
			support.setDropAction(TransferHandler.MOVE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param component the component being dragged from
	 * @return the encapsulated item
	 */
	@Override
	protected Transferable createTransferable(final JComponent component) {
		if (!(component instanceof JList)) {
			throw new IllegalStateException("Tried to create transferrable from non-list");
		}
		final JList<?> list = (JList<?>) component;
		return new IntTransferable(FLAVOR, list.getSelectedIndex());
	}

	/**
	 * We can only move.
	 * @param c ignored
	 * @return MOVE
	 */
	@Override
	public int getSourceActions(final JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override
	public boolean importData(final TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		final Component component = support.getComponent();
		if (!(component instanceof JList)) {
			return false;
		}
		final JList<?> list = (JList<?>) component;
		final ListModel tempModel = list.getModel();
		if (!(tempModel instanceof DefaultListModel)) {
			return false;
		}
		// To add the item back, we have to specify its type here.
		final DefaultListModel<FixtureMatcher> model =
				(DefaultListModel<FixtureMatcher>) tempModel;
		final DropLocation tempDropLoc = support.getDropLocation();
		if (!(tempDropLoc instanceof JList.DropLocation)) {
			return false;
		}
		final JList.DropLocation dropLocation = (JList.DropLocation) tempDropLoc;
		final int index = dropLocation.getIndex();
		final boolean insert = dropLocation.isInsert();
		final Transferable transfer = support.getTransferable();
		final Integer payload;
		try {
			payload = (Integer) transfer.getTransferData(FLAVOR);
		} catch (final Exception except) {
			return false;
		}
		final int data = payload.intValue();
		if (index == data) {
			// no-op
			return true;
		} else if (index > data) {
			final FixtureMatcher item = model.getElementAt(data);
			model.removeElementAt(data);
			model.add(index - 1, item);
			return true;
		} else {
			final FixtureMatcher item = model.getElementAt(data);
			model.removeElementAt(data);
			model.add(index, item);
			return true;
		}
	}
}
