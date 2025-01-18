package drivers.gui.common;

import common.xmlio.SPFormatException;

import javax.swing.TransferHandler;
import javax.xml.stream.XMLStreamException;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;

/**
 * An interface for top-level windows in assistive programs.
 */
public interface ISPWindow {
	/**
	 * The name of this window. This method should <em>not</em> return a
	 * string including the loaded file, since it is used only in the About
	 * dialog to "personalize" it for the particular app.
	 */
	String getWindowName();

	boolean supportsDroppedFiles();
	/**
	 * Handle a dropped file.
	 */
	void acceptDroppedFile(final Path file) throws SPFormatException, IOException, XMLStreamException;
	void showWindow();

	// In JFrame or some superclass; added to the interface to appease 'method not exposed via interface' warning
	void setTitle(final String title);
	void setDefaultCloseOperation(final int operation);
	void setMinimumSize(final Dimension minimumSize);
	void setTransferHandler(final TransferHandler newHandler);
}
