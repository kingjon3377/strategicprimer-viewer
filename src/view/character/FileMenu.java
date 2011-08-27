package view.character;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * File menu for the character management program.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class FileMenu extends JMenu implements ActionListener {
	/**
	 * Constructor.
	 * 
	 * @param parentWindow
	 *            the window we're attached to
	 */
	public FileMenu(final CharacterFrame parentWindow) {
		super("File");
		window = parentWindow;
		addMenuItem("New");
		addMenuItem("Open");
		addMenuItem("Save As");
		addSeparator();
		addMenuItem("Close");
		addMenuItem("Quit");
	}

	/**
	 * The window we're attached to.
	 */
	private final CharacterFrame window;

	/**
	 * The file-chooser dialog.
	 */
	private static final JFileChooser FILE_CHOOSER = new JFileChooser();

	/**
	 * Handle menu selections.
	 * 
	 * @param evt
	 *            the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("Save As".equals(evt.getActionCommand())
				&& FILE_CHOOSER.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
				window.save(FILE_CHOOSER.getSelectedFile().getPath());
		} else if ("Open".equals(evt.getActionCommand())
				&& FILE_CHOOSER.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
				window.open(FILE_CHOOSER.getSelectedFile().getPath());
		} else if ("Close".equals(evt.getActionCommand())) {
			window.closeElement();
		} else if ("Quit".equals(evt.getActionCommand())) {
			// FIXME: ask to save unmodified characters, etc.
			window.quit();
		} else if ("New".equals(evt.getActionCommand())) {
			window.newCharacter();
		} 
	}

	/**
	 * Add a menu item and set this as its ActionListener.
	 * 
	 * @param text
	 *            the text of the menu item
	 */
	private void addMenuItem(final String text) {
		final JMenuItem item = new JMenuItem(text);
		item.addActionListener(this);
		add(item);
	}

}
