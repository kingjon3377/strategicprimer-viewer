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
	 * @param sourceProperty the property the source component listens to
	 */
	public CurriedFixtureTransferable(final List<TileFixture> list, final String sourceProperty) {
		final List<Transferable> payloadTemp = new ArrayList<Transferable>();
		for (TileFixture fix : list) {
			payloadTemp.add(new FixtureTransferable(fix, sourceProperty)); // NOPMD
		}
		payload = Collections.unmodifiableList(payloadTemp);
		property = sourceProperty;
	}
	/**
	 * @return the supported DataFlavors
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	/**
	 * The property the source object listens to. Returned for DataFlavor
	 * "string," to prevent self-drops.
	 */
	private final String property;
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
	 * This now returns the source component's listened property for text
	 * flavors, as part of a hack to disallow intra-component drops.
	 *
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
			return payload; // NOPMD
		} else if (dFlavor.isFlavorTextType()) {
			return property;
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
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CurriedFixtureTransferable";
	}
}
