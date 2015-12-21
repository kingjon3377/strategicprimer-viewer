package view.worker;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import model.listeners.LevelGainListener;
import model.listeners.LevelGainSource;
import model.listeners.SkillSelectionListener;
import model.map.fixtures.mobile.worker.ISkill;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.SingletonRandom;
import util.TypesafeLogger;
import view.util.BoxPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;

/**
 * A panel to let a user add hours to a skill.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SkillAdvancementPanel extends BoxPanel implements ActionListener,
		                                                                     SkillSelectionListener,
		                                                                     LevelGainSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(SkillAdvancementPanel.class);
	/**
	 * The list of listeners.
	 */
	private final Collection<LevelGainListener> listeners = new ArrayList<>();

	/**
	 * The "die" we "roll" to see whether skill advancement happens.
	 */
	private static final int SKILL_DIE = 100;
	/**
	 * The maximum height of the panel.
	 */
	private static final int MAX_PANEL_HEIGHT = 60;
	/**
	 * The skill we're dealing with. May be null if no skill is selected.
	 */
	@Nullable
	private ISkill skill = null;

	/**
	 * @param nSkill the newly selected skill.
	 */
	@Override
	public void selectSkill(@Nullable final ISkill nSkill) {
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
	 */
	public SkillAdvancementPanel() {
		super(false);
		final JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new FlowLayout());
		firstPanel.add(new JLabel("Add "));
		firstPanel.add(hours);
		firstPanel.add(new JLabel(" hours to skill?"));
		add(firstPanel);
		final JPanel secondPanel = new JPanel();
		secondPanel.setLayout(new FlowLayout());
		secondPanel.add(new ListenedButton("OK", this));
		hours.setActionCommand("OK");
		hours.addActionListener(this);
		secondPanel.add(new ListenedButton("Cancel", this));
		add(secondPanel);
		setMinimumSize(new Dimension(200, 40));
		setPreferredSize(new Dimension(220, MAX_PANEL_HEIGHT));
		setMaximumSize(new Dimension(240, MAX_PANEL_HEIGHT));
	}

	/**
	 * Parser for hours field.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			                                               .assertNotNull(NumberFormat
					                                                              .getIntegerInstance());

	/**
	 * Handle a button press.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		}
		if ("OK".equalsIgnoreCase(evt.getActionCommand()) && (skill != null)) {
			final ISkill skl = skill;
			final int level = skl.getLevel();
			try {
				skl.addHours(NUM_PARSER.parse(hours.getText()).intValue(),
						SingletonRandom.RANDOM.nextInt(SKILL_DIE));
			} catch (final ParseException e) {
				LOGGER.log(Level.FINE, "Non-numeric input", e);
				ErrorShower.showErrorDialog(this, "Hours to add must be a number");
				return;
			}
			final int newLevel = skl.getLevel();
			if (newLevel != level) {
				listeners.forEach(LevelGainListener::level);
			}
		}
		// Clear if OK and no skill selected, on Cancel, and after
		// successfully adding skill
		hours.setText("");
	}

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
