package model.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.map.TileFixture;
/**
 * A class to transfer a list of TileFixtures.
 * @author Jonathan Lovelace
 *
 */
public class CurriedFixtureTransferable implements Transferable {
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
	 * @param list a list of TileFixtures to be transferred
	 */
	public CurriedFixtureTransferable(final List<TileFixture> list) {
		final List<Transferable> payloadTemp = new ArrayList<Transferable>();
		for (TileFixture fix : list) {
			payloadTemp.add(new FixtureTransferable(fix)); // NOPMD
		}
		payload = Collections.unmodifiableList(payloadTemp);
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
	public boolean isDataFlavorSupported(final DataFlavor dFlavor) {
		return FLAVOR.equals(dFlavor);
	}

	/**
	 * @param dFlavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 * @throws IOException required by spec but not thrown
	 */
	@Override
	public Object getTransferData(final DataFlavor dFlavor)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(dFlavor)) {
			// ESCA-JAVA0259: The collection is unmodifiable.
			return payload;
		} else {
			throw new UnsupportedFlavorException(dFlavor);
		}
	}
	/**
	 * A typesafe replacement for getTransferData.
	 * @return our payload
	 */
	public List<Transferable> getPayload() {
		return payload;
	}
}
