package view.map.main;

import java.awt.Graphics;

/**
 * A class to replace all cases where a Selectable variable is null.
 * @author Jonathan Lovelace
 */
public final class NullSelection extends Selectable {
	/**
	 * Singleton.
	 */
	public static final NullSelection EMPTY = new NullSelection();
	/**
	 * Singleton.
	 */
	private NullSelection() {
		super();
	}
	/**
	 * Don't waste resources doing any painting.
	 * @param pen ignored
	 */
	@Override
	// ESCA-JAVA0025:
	public void paint(final Graphics pen) {
		// Do nothing.
	}
}