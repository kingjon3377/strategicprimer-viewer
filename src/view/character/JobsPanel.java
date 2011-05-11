package view.character;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A panel to represent a (changing) list of Jobs (which are managed elsewhere).
 * This is really just to keep the levels of nested panels within the panel
 * managing their layout from getting too deep.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class JobsPanel extends JPanel implements ActionListener {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 6381213579509853088L;
	/**
	 * The panel most of the content actually goes on.
	 */
	private final JPanel contentPanel;
	/**
	 * The panel managing the whole character.
	 */
	private final CharacterPanel parentPanel;

	/**
	 * Constructor.
	 * 
	 * @param charPanel
	 *            the main character editing panel
	 */
	public JobsPanel(final CharacterPanel charPanel) {
		super(new BorderLayout());
		contentPanel = new JPanel(new GridLayout(0, 1));
		parentPanel = charPanel;
		super.add(contentPanel);
		final JButton addButton = new JButton("Add Job");
		addButton.addActionListener(this);
		add(addButton, BorderLayout.SOUTH);
	}

	/**
	 * Add any JobPanels to the content panel.
	 * 
	 * @param comp
	 *            the component to add
	 * @return the component we added
	 */
	@Override
	public Component add(final Component comp) {
		if (comp instanceof JobPanel) {
			contentPanel.add(comp);
		} else {
			super.add(comp);
		}
		return comp;
	}

	/**
	 * Handle button press.
	 * @param event the button press we're handling.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		// FIXME: We need to keep track of the jobs we're displaying, and become
		// an Applyable.
		if ("Add Job".equals(event.getActionCommand())) {
			add(new JobPanel(parentPanel));
		}
	}
}
