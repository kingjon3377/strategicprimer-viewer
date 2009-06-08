package view.user;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.xml.sax.SAXException;

import controller.XMLReader;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame implements WindowListener, ActionListener {
	/**
	 * A logger to replace System.err usage
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class.getName());
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5978274834544191806L;
	/**
	 * The quasi-Singleton.
	 */
	private static ViewerFrame frame;

	/**
	 * @return the quasi-Singleton objects
	 */
	public static ViewerFrame getFrame() {
		return frame;
	}

	/**
	 * Run the app.
	 * 
	 * @param args
	 *            FIXME: args[0] should be the map filename.
	 */
	public static void main(final String[] args) {
		try {
			frame = new ViewerFrame(args[0]);
			frame.setVisible(true);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE,"Error reading XML file:",e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"Error reading XML file:",e);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            The filename of an XML file describing the map
	 * @throws IOException
	 *             on I/O error
	 * @throws SAXException
	 *             on XML reading error
	 */
	private ViewerFrame(final String filename) throws SAXException, IOException {
		super();
		setLayout(new BorderLayout());
		add(new MapPanel(new XMLReader().getMap(filename)), BorderLayout.CENTER);
		final JButton quitButton = new JButton("Quit"); 
		quitButton.addActionListener(this);
		add(quitButton, BorderLayout.SOUTH);
		addWindowListener(this);
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Do nothing special on window activation.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowActivated(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Quit on window close.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowClosed(final WindowEvent event) {
		System.exit(0); 
	}

	/**
	 * Do nothing special on window-closing.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowClosing(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Do nothing special on window-deactivation.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowDeactivated(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Do nothing special when deiconified.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowDeiconified(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * do nothing special when iconified.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowIconified(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Do nothing special when opened.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowOpened(final WindowEvent event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Handle button presses and the like.
	 * 
	 * @param event
	 *            the action we're handling
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Quit".equals(event.getActionCommand())) {
			quit(0);
		}
	}
	/**
	 * Quit. (Don't halt the VM from a non-static context.)
	 * @param code The exit code.
	 */
	private static void quit(final int code) {
		System.exit(code);
	}
}
