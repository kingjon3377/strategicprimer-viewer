package model.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to transfer a list of TileFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class CurriedFixtureTransferable implements Transferable {
	/**
	 * The data flavor we handle.
	 */
	public static final DataFlavor FLAVOR = new DataFlavor(CurriedFixtureTransferable.class,
																  "CurriedTransferable");
	/**
	 * Our payload.
	 */
	private final List<Transferable> payload;

	/**
	 * Constructor.
	 *
	 * @param list a list of TileFixtures to be transferred
	 */
	public CurriedFixtureTransferable(final Collection<TileFixture> list) {
		payload = Collections.unmodifiableList(list.stream().map(FixtureTransferable::new)
													   .collect(Collectors.toList()));
	}

	/**
	 * We only support the one data flavor.
	 * @return the supported DataFlavors
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{FLAVOR};
	}

	/**
	 * We only support the one data flavor.
	 * @param flavor a DataFlavor
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(@Nullable final DataFlavor flavor) {
		return FLAVOR.equals(flavor);
	}

	/**
	 * This now returns the source component's listened property for text flavors, as
	 * part of a hack to disallow drops within the same component.
	 *
	 * @param flavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 */
	@Override
	public Iterable<Transferable> getTransferData(@Nullable final DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (isDataFlavorSupported(flavor)) {
			return new ArrayList<>(payload);
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * The String representation only gives the size of the payload, to avoid becoming
	 * too long.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CurriedFixtureTransferable with payload containing " + payload.size() +
					   " elements";
	}
}
