package model.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import model.map.TileFixture;
import util.NullCleaner;

/**
 * A class to transfer a list of TileFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CurriedFixtureTransferable implements Transferable {
	/**
	 * The data flavor we handle.
	 */
	public static final DataFlavor FLAVOR = new DataFlavor(
			FixtureTransferable.class, "CurriedFixtureTransferable");
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
		payload =
				NullCleaner.assertNotNull(Collections
						                          .unmodifiableList(list.stream().map(FixtureTransferable::new)
								                                            .collect(Collectors.toList())));
	}

	/**
	 * @return the supported DataFlavors
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	/**
	 *
	 * @param dFlavor a DataFlavor
	 *
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(@Nullable final DataFlavor dFlavor) {
		return FLAVOR.equals(dFlavor);
	}

	/**
	 * This now returns the source component's listened property for text
	 * flavors, as part of a hack to disallow intra-component drops.
	 *
	 * @param dFlavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 * @throws IOException required by spec but not thrown
	 */
	@Override
	public Iterable<Transferable> getTransferData(@Nullable final DataFlavor dFlavor)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(dFlavor)) {
			return payload; // NOPMD
		} else {
			throw new UnsupportedFlavorException(dFlavor);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CurriedFixtureTransferable";
	}
}
