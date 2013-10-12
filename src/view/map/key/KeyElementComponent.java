package view.map.key;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The main component of a KeyElement.
 *
 * @author Jonathan Lovelace
 *
 */
public final class KeyElementComponent extends JComponent {
	/**
	 * The color of this Component.
	 */
	private final Color color;

	/**
	 * Constructor.
	 *
	 * @param col the color to make the component.
	 */
	public KeyElementComponent(final Color col) {
		super();
		color = col;
	}

	/**
	 * @param pen the graphics context
	 */
	@Override
	public void paint(@Nullable final Graphics pen) {
		if (pen == null) {
			throw new IllegalArgumentException("Graphics cannot be null");
		}
		final Graphics context = pen.create();
		try {
			context.setColor(color);
			context.fillRect(0, 0, getWidth(), getHeight());
		} finally {
			context.dispose();
		}
	}
}
