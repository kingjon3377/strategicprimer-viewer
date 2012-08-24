package model.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import model.map.TileFixture;

/**
 * A class to transfer a TileFixture.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureTransferable implements Transferable {
	/**
	 * Constructor.
	 *
	 * @param theData the object
	 * @param sourceProperty the property the source object listens to
	 */
	public FixtureTransferable(final TileFixture theData, final String sourceProperty) {
		data = theData;
		property = sourceProperty;
	}

	/**
	 * The property the source object listens to. Returned for DataFlavor
	 * "string," to prevent self-drops.
	 */
	private final String property;
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
	public boolean isDataFlavorSupported(final DataFlavor dflavor) {
		return dflavor.equals(FLAVOR);
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
	public Object getTransferData(final DataFlavor dflavor)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(dflavor)) {
			return data; // NOPMD
		} else if (dflavor.isFlavorTextType()) {
			return property;
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
	public boolean equals(final Object obj) {
		return obj instanceof FixtureTransferable
				&& data.equals(((FixtureTransferable) obj).data);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}

}
