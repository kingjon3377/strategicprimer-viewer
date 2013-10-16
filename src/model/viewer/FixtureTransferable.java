package model.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to transfer a TileFixture.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureTransferable implements Transferable, Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param theData the object
	 */
	public FixtureTransferable(final TileFixture theData) {
		data = theData;
	}

	/**
	 * The object we're transfering.
	 */
	private final TileFixture data;
	/**
	 * a DataFlavor representing its class.
	 */
	public static final DataFlavor FLAVOR = new DataFlavor(TileFixture.class,
			"TileFixture");

	/**
	 *
	 * @return the supported DataFlavors.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	/**
	 *
	 * @param dflavor a DataFlavor
	 *
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(@Nullable final DataFlavor dflavor) {
		return FLAVOR.equals(dflavor);
	}

	/**
	 * This now returns the source component's listened property for text
	 * flavors, as part of a hack to disallow intra-component drops.
	 *
	 * @param dflavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 * @throws IOException required by spec but not thrown
	 */
	@Override
	public Object getTransferData(@Nullable final DataFlavor dflavor)
			throws UnsupportedFlavorException, IOException {
		if (FLAVOR.equals(dflavor)) {
			return data; // NOPMD
		} else {
			throw new UnsupportedFlavorException(dflavor);
		}
	}

	/**
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "FixtureTransferable";
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof FixtureTransferable && data
						.equals(((FixtureTransferable) obj).data));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}

}
