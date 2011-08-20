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
	 * Constructor.
	 * 
	 * @param comp
	 *            the component to wrap
	 */
	public ChangeableComponent(final JComponent comp) {
		super();
		wrappedComponent = comp;
		add(wrappedComponent);
	}

	/**
	 * The component we're wrapping.
	 */
	private JComponent wrappedComponent;

	/**
	 * @return the component we're wrapping
	 */
	public JComponent getComponent() {
		return wrappedComponent;
	}

	/**
	 * @param comp
	 *            the new component
	 */
	public void setComponent(final JComponent comp) {
		remove(wrappedComponent);
		wrappedComponent = comp;
		add(wrappedComponent);
	}
}
