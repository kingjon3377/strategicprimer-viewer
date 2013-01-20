package view.worker;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.map.fixtures.mobile.worker.Skill;
import util.PropertyChangeSource;
import util.SingletonRandom;
/**
 * A panel to let a user add hours to a skill.
 * @author Jonathan Lovelace
 *
 */
public class SkillAdvancementPanel extends JPanel implements
		PropertyChangeListener, ActionListener {
	/**
	 * The skill we're dealing with. May be null if no skill is selected.
	 */
	private Skill skill = null;
	/**
	 * @param evt event indicating a property change.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("skill".equalsIgnoreCase(evt.getPropertyName())
				&& (evt.getNewValue() == null || evt.getNewValue() instanceof Skill)) {
			skill = (Skill) evt.getNewValue();
			hours.requestFocusInWindow();
		}
	}
	/**
	 * Text box.
	 */
	private final JTextField hours = new JTextField(3);
	/**
	 * Constructor.
	 * @param listener something to listen to our PropertyChangeEvents.
	 * @param sources the things we should listen to
	 */
	public SkillAdvancementPanel(final PropertyChangeListener listener,
			final PropertyChangeSource... sources) {
		addPropertyChangeListener(listener);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		final JPanel one = new JPanel();
		one.setLayout(new FlowLayout());
		one.add(new JLabel("Add "));
		one.add(hours);
		one.add(new JLabel(" hours to skill?"));
		add(one);
		final JPanel two = new JPanel();
		two.setLayout(new FlowLayout());
		two.add(listen(new JButton("OK")));
		two.add(listen(new JButton("Cancel")));
		add(two);
		for (PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
		setMinimumSize(new Dimension(200, 40));
		setPreferredSize(new Dimension(220, 60));
		setMaximumSize(new Dimension(240, 60));
	}
	/**
	 * Set this as a listener on a button.
	 * @param button the button
	 * @return the button
	 */
	private JButton listen(final JButton button) {
		button.addActionListener(this);
		return button;
	}
	/**
	 * Handle a button press.
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("OK".equalsIgnoreCase(evt.getActionCommand()) && skill != null) {
			final int level = skill.getLevel();
			skill.addHours(Integer.parseInt(hours.getText()), SingletonRandom.RANDOM.nextInt(100));
			final int newLevel = skill.getLevel();
			if (newLevel != level) {
				firePropertyChange("level", level, newLevel);
			}
		}
		// Clear if OK and no skill selected, on Cancel, and after successfully adding skill
		hours.setText("");
	}
}
