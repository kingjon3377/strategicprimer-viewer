package util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * A Transferable implementation that transfers a single int.
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
public final class IntTransferable implements Transferable {
	/**
	 * The flavor the caller wants us to use.
	 */
	private final DataFlavor reportedFlavor;
	/**
	 * The payload.
	 */
	private final int payload;
	/**
	 * Constructor.
	 * @param flavor the data flavor to report
	 * @param data the payload
	 */
	public IntTransferable(final DataFlavor flavor, final int data) {
		reportedFlavor = flavor;
		payload = data;
	}
	/**
	 * @return an array of data flavors in which this data can be transferred
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{ reportedFlavor };
	}

	/**
	 * @param flavor the requested flavor for the data
	 * @return boolean indicating whether or not the data flavor is supported
	 */
	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return reportedFlavor.equals(flavor);
	}

	/**
	 * @param flavor the requested flavor for the data
	 * @throws UnsupportedFlavorException if the requested data flavor is not supported.
	 * @see DataFlavor#getRepresentationClass
	 */
	@Override
	public Object getTransferData(final DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (reportedFlavor.equals(flavor)) {
			return Integer.valueOf(payload);
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}
