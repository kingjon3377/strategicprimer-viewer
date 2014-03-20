package view.util;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import model.listeners.AddRemoveListener;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A panel to be the GUI to add or remove items from a list.
 *
 * @author Jonathan Lovelace
 *
 */
public class AddRemovePanel extends JPanel implements ActionListener {
	/**
	 * The layout.
	 */
	private final CardLayout layout;
	/**
	 * The text box.
	 */
	private final JTextField field = new JTextField(10);
	/**
	 * What we're adding or removing.
	 */
	private final String category;

	/**
	 * Constructor.
	 *
	 * @param removalPossible whether we should put in a "remove" button.
	 * @param what what we're adding or removing
	 */
	public AddRemovePanel(final boolean removalPossible, final String what) {
		layout = new CardLayout();
		category = what;
		setLayout(layout);
		setPanelSizes(this);
		final BoxPanel first = new BoxPanel(true);
		first.add(new ListenedButton("+", this));
		if (removalPossible) {
			first.add(new ListenedButton("-", this));
		}
		setPanelSizes(first);
		add(first);
		final BoxPanel second = new BoxPanel(false);
		second.add(field);
		field.addActionListener(this);
		field.setActionCommand("OK");
		final BoxPanel okPanel = new BoxPanel(true);
		okPanel.add(new ListenedButton("OK", this));
		okPanel.add(new ListenedButton("Cancel", this));
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
	 *
	 * @param panel the panel in question
	 */
	private static void setPanelSizes(final JPanel panel) {
		panel.setMinimumSize(new Dimension(60, 40));
		panel.setPreferredSize(new Dimension(80, MAX_HEIGHT));
		panel.setMaximumSize(new Dimension(90, MAX_HEIGHT));
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public final void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("+".equals(evt.getActionCommand())) {
			layout.next(this);
			field.requestFocusInWindow();
		} else if ("-".equals(evt.getActionCommand())) {
			for (final AddRemoveListener list : arListeners) {
				list.remove(category);
			}
		} else if ("OK".equals(evt.getActionCommand())) {
			final String text = field.getText();
			for (final AddRemoveListener list : arListeners) {
				list.add(category, NullCleaner.valueOrDefault(text, "null"));
			}
			layout.first(this);
			field.setText("");
		} else if ("Cancel".equals(evt.getActionCommand())) {
			layout.first(this);
			field.setText("");
		}
	}

	/**
	 * A list of listeners to notify of addition or removal.
	 */
	private final List<AddRemoveListener> arListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	public final void addAddRemoveListener(final AddRemoveListener list) {
		arListeners.add(list);
	}

	/**
	 * @param list a list to remove
	 */
	public final void removeAddRemoveListener(final AddRemoveListener list) {
		arListeners.remove(list);
	}
}
