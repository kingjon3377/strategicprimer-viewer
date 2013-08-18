package view.util;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.PropertyChangeSource;

/**
 * A panel to be the GUI to add or remove items from a list. We'll fire the
 * "add" property with a String value to add if adding is completed, and the
 * "remove" property if removal is selected.
 *
 * @author Jonathan Lovelace
 *
 */
public class AddRemovePanel extends JPanel implements ActionListener, PropertyChangeSource {
	/**
	 * The layout.
	 */
	private final CardLayout layout;
	/**
	 * The text box.
	 */
	private final JTextField field = new JTextField(10);
	/**
	 * Constructor.
	 * @param removalPossible whether we should put in a "remove" button.
	 */
	public AddRemovePanel(final boolean removalPossible) {
		layout = new CardLayout();
		setLayout(layout);
		setPanelSizes(this);
		final BoxPanel first = new BoxPanel(true);
		first.add(listen(new JButton("+")));
		if (removalPossible) {
			first.add(listen(new JButton("-")));
		}
		setPanelSizes(first);
		add(first);
		final BoxPanel second = new BoxPanel(false);
		second.add(field);
		field.addActionListener(this);
		field.setActionCommand("OK");
		final BoxPanel okPanel = new BoxPanel(true);
		okPanel.add(listen(new JButton("OK")));
		okPanel.add(listen(new JButton("Cancel")));
		second.add(okPanel);
		setPanelSizes(second);
		add(second);
	}
	/**
	 * The maximum height of the widget.
	 */
	private static final int MAX_HEIGHT = 50;
	/**
	 * Set the sizes we want on a panel.
	 * @param panel the panel in question
	 */
	private static void setPanelSizes(final JPanel panel) {
		panel.setMinimumSize(new Dimension(60, 40));
		panel.setPreferredSize(new Dimension(80, MAX_HEIGHT));
		panel.setMaximumSize(new Dimension(90, MAX_HEIGHT));
	}
	/**
	 * Listen to a button.
	 * @param button the button to listen to
	 * @return it
	 */
	private JButton listen(final JButton button) {
		button.addActionListener(this);
		return button;
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("+".equals(evt.getActionCommand())) {
			layout.next(this);
			field.requestFocusInWindow();
		} else if ("-".equals(evt.getActionCommand())) {
			firePropertyChange("remove", null, null);
		} else if ("OK".equals(evt.getActionCommand())) {
			firePropertyChange("add", null, field.getText());
			layout.first(this);
			field.setText("");
		} else if ("Cancel".equals(evt.getActionCommand())) {
			layout.first(this);
			field.setText("");
		}
	}
}
