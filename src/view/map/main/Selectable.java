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
	 * Paint a rectangle around the item if it's selected.
	 * 
	 * @param pen the graphics context.
	 */
	@Override
	public void paint(final Graphics pen) {
		if (selected) {
			final Graphics context = pen.create();
			try {
				context.setColor(Color.BLACK);
				context.drawRect(0, 0, getWidth(), getHeight());
				context.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
			} finally {
				context.dispose();
			}
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
	 * 
	 * @return whether this is the currently selected tile
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param sel whether this is the currently selected tile
	 */
	public void setSelected(final boolean sel) {
		selected = sel;
		repaint();
	}

}
