package view.character;

import javax.swing.JFrame;
/**
 * A window (and driver) for the character-management program.
 * @author Jonathan Lovelace
 *
 */
public class CharacterFrame extends JFrame {
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
		add(new CharacterPanel());
	}
}
