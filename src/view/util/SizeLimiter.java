package view.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * A listener that reduces a child component's size to a specified ratio to its
 * parent's size when the parent is resized.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SizeLimiter extends ComponentAdapter {
	/**
	 * Constructor.
	 * 
	 * @param comp
	 *            the component whose size we are to limit
	 * @param widthFactor
	 *            its width compared to the parent's width
	 * @param heightFactor
	 *            its height compared to the parent's height
	 */
	public SizeLimiter(final Component comp, final double widthFactor,
			final double heightFactor) {
		super();
		component = comp;
		wFactor = widthFactor;
		hFactor = heightFactor;
	}

	/**
	 * The component whose size we are limiting.
	 */
	private final Component component;
	/**
	 * Its width compared to the parent's width.
	 */
	private final double wFactor;
	/**
	 * Its height compared to the parent's height.
	 */
	private final double hFactor;

	/**
	 * The parent's been resized--resize the component to match.
	 * 
	 * @param event
	 *            The resize event.
	 * 
	 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentResized(final ComponentEvent event) {
		component.setMaximumSize(new Dimension(((int) (event.getComponent()
				.getWidth() * wFactor)), ((int) (event.getComponent()
				.getHeight() * hFactor))));
		component.setPreferredSize(component.getMaximumSize());
	}

}
