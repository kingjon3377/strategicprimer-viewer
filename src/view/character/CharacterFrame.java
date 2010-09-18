package view.character;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import view.util.SaveableOpenable;
import view.util.UICloseable;
import controller.character.CharacterReader;

/**
 * A window (and driver) for the character-management program.
 * @author Jonathan Lovelace
 *
 */
public class CharacterFrame extends JFrame implements SaveableOpenable,
		UICloseable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -1521685216194840568L;
	/**
	 * Driver entry point.
	 * @param args ignored at this point
	 */
	public static void main(final String args[]) {
		new CharacterFrame().setVisible(true);
	}

	/**
	 * The panel manager
	 */
	private final JTabbedPane tabber;

	/**
	 * Constructor.
	 */
	public CharacterFrame() {
		super();
		tabber = new JTabbedPane();
		add(tabber);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JMenuBar menu = new JMenuBar();
		menu.add(new FileMenu(this));
		setJMenuBar(menu);
		pack();
	}
	/**
	 * Open a character.
	 * 
	 * @param file
	 *            the filename to load from
	 * @throws IOException
	 *             on I/O error loading
	 * @throws FileNotFoundException
	 *             if the file doesn't exist
	 */
	@Override
	public void open(final String file) throws FileNotFoundException,
			IOException {
		tabber.addTab("Character", new CharacterPanel(new CharacterReader(file)
				.getCharacter()));
	}
	/**
	 * Save the current character.
	 * 
	 * @param file
	 *            the filename to save to
	 * @throws IOException
	 *             on I/O error while saving
	 */
	@Override
	public void save(final String file) throws IOException {
		final Component comp = tabber.getSelectedComponent();
		if (comp instanceof SaveableOpenable) {
			((SaveableOpenable) comp).save(file);
		} else {
			throw new IllegalStateException(
					"Told to save when the current tab can't.");
		}
	}

	/**
	 * Close the current character.
	 */
	@Override
	public void close() {
		tabber.removeTabAt(tabber.getSelectedIndex());
	}
}
