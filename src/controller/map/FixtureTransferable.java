package controller.map;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import model.viewer.TileFixture;
/**
 * A class to transfer a TileFixture.
 * @author Jonathan Lovelace
 *
 */
public class FixtureTransferable implements Transferable {
	/**
	 * Constructor.
	 * @param theData the object
	 */
	public FixtureTransferable(final TileFixture theData) {
		data = theData;
		flavor = new DataFlavor(TileFixture.class, "TileFixture");
	}
	/**
	 * The object we're transfering.
	 */
	private final TileFixture data;
	/**
	 * a DataFlavor representing its class.
	 */
	private final DataFlavor flavor;
	/**
	 * @return the supported DataFlavors.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { flavor };
	}
	/**
	 * 
	 * @param dflavor a DataFlavor
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(final DataFlavor dflavor) {
		return dflavor.equals(flavor);  
	}
	/**
	 * @param dflavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 * @throws IOException required by spec but not thrown
	 */
	@Override
	public Object getTransferData(final DataFlavor dflavor)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(dflavor)) {
			return data;
		} else {
			throw new UnsupportedFlavorException(dflavor);
		}
	}
}
