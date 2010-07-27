package view.user;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.xml.sax.SAXException;

import controller.XMLReader;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame implements WindowListener,
		ActionListener {
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 640;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 480;
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
	 *            Command-line arguments: args[0] is the map filename, others
	 *            are ignored. TODO: Add option handling.
	 * 
	 */
	public static void main(final String[] args) {
		final Logger LOGGER = Logger.getLogger(ViewerFrame.class.getName());
		try {
			String filename;
			if (args.length > 0) {
				filename = args[0];
			} else {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new MapFileFilter());
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					filename = chooser.getSelectedFile().getPath();
				} else {
					return;
				}
			}
			frame = new ViewerFrame(filename);
			frame.setVisible(true);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, "Error reading XML file:", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error reading XML file:", e);
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
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		this.setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		this.setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		addWindowListener(this);
		final JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(this);
		add(quitButton, BorderLayout.SOUTH);
		add(new JScrollPane(new MapPanel(new XMLReader().getMap(filename))),
				BorderLayout.CENTER);
		// final MapComponent map = new MapComponent(new
		// XMLReader().getMap(filename));
		// final JScrollPane scrollPane = new JScrollPane(map);
		// map.createImage();
		// scrollPane.setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT
		// - 5));
		// scrollPane.setPreferredSize(scrollPane.getMaximumSize());
		// add(scrollPane, BorderLayout.CENTER);
		pack();
		repaint();
	}

	/**
	 * Do nothing special on window activation.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowActivated(final WindowEvent event) {
		repaint();
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
		// Do nothing
	}

	/**
	 * Do nothing special on window-deactivation.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowDeactivated(final WindowEvent event) {
		// Do nothing
	}

	/**
	 * Do nothing special when deiconified.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowDeiconified(final WindowEvent event) {
		repaint();
	}

	/**
	 * do nothing special when iconified.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowIconified(final WindowEvent event) {
		// Do nothing
	}

	/**
	 * Do nothing special when opened.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void windowOpened(final WindowEvent event) {
		repaint();
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
	 * 
	 * @param code
	 *            The exit code.
	 */
	private static void quit(final int code) {
		System.exit(code);
	}
}
