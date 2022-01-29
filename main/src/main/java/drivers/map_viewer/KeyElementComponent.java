package drivers.map_viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * The part of the key showing a tile's color.
 */
/* package */ class KeyElementComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	public KeyElementComponent(Color color, Dimension minimum, Dimension preferred,
			Dimension maximum) {
		setMinimumSize(minimum);
		setPreferredSize(preferred);
		setMaximumSize(maximum);
		this.color = color;
	}

	private final Color color;

	@Override
	public void paint(Graphics pen) {
		Graphics context = pen.create();
		try {
			context.setColor(color);
			context.fillRect(0, 0, getWidth(), getHeight());
		} finally {
			context.dispose();
		}
	}
}