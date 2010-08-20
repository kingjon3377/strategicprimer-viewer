package view.character;

import javax.swing.JFrame;
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
		add(new CharacterPanel());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}
}
