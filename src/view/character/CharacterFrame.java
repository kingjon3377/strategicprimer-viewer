package view.character;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import model.character.SPCharacter;
/**
 * A window (and driver) for the character-management program.
 * @author Jonathan Lovelace
 *
 */
public class CharacterFrame extends JFrame {
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
	 * Constructor.
	 */
	public CharacterFrame() {
		super();
		add(panel = new CharacterPanel());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JMenuBar menu = new JMenuBar();
		menu.add(new FileMenu(this));
		setJMenuBar(menu);
		pack();
	}
	/**
	 * The panel.
	 */
	private final CharacterPanel panel;
	/**
	 * @return the character we're editing.
	 */
	public SPCharacter getCharacter() {
		return panel.getCharacter();
	}
}
