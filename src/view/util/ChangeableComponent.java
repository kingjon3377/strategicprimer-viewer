package view.util;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A wrapper around another component, so we can change it without removing and
 * re-adding it.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ChangeableComponent extends JPanel {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -4804570258956941305L;

	/**
	 * Constructor.
	 * 
	 * @param comp
	 *            the component to wrap
	 */
	public ChangeableComponent(final JComponent comp) {
		super();
		component = comp;
		add(component);
	}

	/**
	 * The component we're wrapping.
	 */
	private JComponent component;

	/**
	 * @return the component we're wrapping
	 */
	public JComponent getComponent() {
		return component;
	}

	/**
	 * @param comp
	 *            the new component
	 */
	public void setComponent(final JComponent comp) {
		remove(component);
		component = comp;
		add(component);
	}
}
