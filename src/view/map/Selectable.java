package view.map;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public abstract class Selectable extends JComponent {
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

	public Selectable() {
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