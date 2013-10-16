package view.worker;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.listeners.LevelGainListener;
import model.listeners.LevelGainSource;
import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

import util.SingletonRandom;
import view.util.BoxPanel;
import view.util.ListenedButton;

/**
 * A panel to let a user add hours to a skill.
 *
 * @author Jonathan Lovelace
 *
 */
public class SkillAdvancementPanel extends BoxPanel implements ActionListener,
		SkillSelectionListener, LevelGainSource {
	/**
	 * The maximum height of the panel.
	 */
	private static final int MAX_PANEL_HEIGHT = 60;
	/**
	 * The skill we're dealing with. May be null if no skill is selected.
	 */
	@Nullable
	private Skill skill = null;

	/**
	 * @param nSkill the newly selected skill.
	 */
	@Override
	public void selectSkill(@Nullable final Skill nSkill) {
		skill = nSkill;
		if (skill != null) {
			hours.requestFocusInWindow();
		}
	}
	/**
	 * Text box.
	 */
	private final JTextField hours = new JTextField(3);

	/**
	 * Constructor.
	 *
	 * @param listener something to listen to our level-gain notifications
	 * @param sources the things we should listen to
	 */
	public SkillAdvancementPanel(final LevelGainListener listener,
			final SkillSelectionSource... sources) {
		super(false);
		addLevelGainListener(listener);
		final JPanel one = new JPanel();
		one.setLayout(new FlowLayout());
		one.add(new JLabel("Add "));
		one.add(hours);
		one.add(new JLabel(" hours to skill?"));
		add(one);
		final JPanel two = new JPanel();
		two.setLayout(new FlowLayout());
		two.add(new ListenedButton("OK", this));
		hours.setActionCommand("OK");
		hours.addActionListener(this);
		two.add(new ListenedButton("Cancel", this));
		add(two);
		for (final SkillSelectionSource source : sources) {
			source.addSkillSelectionListener(this);
		}
		setMinimumSize(new Dimension(200, 40));
		setPreferredSize(new Dimension(220, MAX_PANEL_HEIGHT));
		setMaximumSize(new Dimension(240, MAX_PANEL_HEIGHT));
	}

	/**
	 * Handle a button press.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null) {
			if ("OK".equalsIgnoreCase(evt.getActionCommand()) && skill != null) {
				final Skill skl = skill;
				final int level = skl.getLevel();
				skl.addHours(Integer.parseInt(hours.getText()),
						SingletonRandom.RANDOM.nextInt(100));
				final int newLevel = skl.getLevel();
				if (newLevel != level) {
					for (final LevelGainListener list : listeners) {
						list.level();
					}
				}
			}
			// Clear if OK and no skill selected, on Cancel, and after
			// successfully adding skill
			hours.setText("");
		}
	}

	/**
	 * The list of listeners.
	 */
	private final List<LevelGainListener> listeners = new ArrayList<>();

	/**
	 * @param list the listener to add
	 */
	@Override
	public void addLevelGainListener(final LevelGainListener list) {
		listeners.add(list);
	}

	/**
	 * @param list the listener to remove
	 */
	@Override
	public void removeLevelGainListener(final LevelGainListener list) {
		listeners.remove(list);
	}
}
