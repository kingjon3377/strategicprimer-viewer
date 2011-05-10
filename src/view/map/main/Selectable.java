package view.map.main;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * A superclass for GUI elements where one (or more) of a class can be selected,
 * and this is to be visually marked by drawing a box around it. This used to be
 * just GUITiles, but then I invented Chits.
 * 
 * @author Jonathan Lovelace
 * 
 */
// ESCA-JAVA0011:
public abstract class Selectable extends JComponent { // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 6213677364292114007L;

	/**
	 * Paint a rectangle around the item if it's selected.
	 */
	@Override
	public void paint(final Graphics pen) {
		if (selected) {
			final Color saveColor = pen.getColor();
			pen.setColor(Color.BLACK);
			pen.drawRect(0, 0, getWidth(), getHeight());
			pen.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
			pen.drawRect(2, 2, getWidth() - 3, getHeight() - 3);
			pen.setColor(saveColor);
		}
	}

	/**
	 * Whether this is the currently selected tile.
	 */
	private boolean selected;

	/**
	 * Constructor. Initially the control is not selected.
	 */
	protected Selectable() {
		super();
		selected = false;
	}

	/**
	 * @return whether this is the currently selected tile
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param sel
	 *            whether this is the currently selected tile
	 */
	public void setSelected(final boolean sel) {
		selected = sel;
		repaint();
	}

}