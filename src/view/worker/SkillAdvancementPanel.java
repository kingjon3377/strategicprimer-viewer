package view.worker;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import util.OnMac;
import util.SingletonRandom;
import util.TypesafeLogger;
import view.util.BoxPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;

import static util.NullCleaner.assertNotNull;

/**
 * A panel to let a user add hours to a skill.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SkillAdvancementPanel extends BoxPanel
		implements SkillSelectionListener, LevelGainSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(SkillAdvancementPanel.class);
	/**
	 * The "die" we "roll" to see whether skill advancement happens.
	 */
	private static final int SKILL_DIE = 100;
	/**
	 * The maximum height of the panel.
	 */
	private static final int MAX_PANEL_HEIGHT = 60;
	/**
	 * Parser for hours field.
	 */
	private static final NumberFormat NUM_PARSER =
			assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * The list of listeners.
	 */
	private final Collection<LevelGainListener> listeners = new ArrayList<>();
	/**
	 * Text box.
	 */
	private final JTextField hours = new JTextField(3);
	/**
	 * The skill we're dealing with. May be null if no skill is selected.
	 */
	@Nullable
	private ISkill skill = null;

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
		final ActionListener okListener = evt -> {
			final ISkill skl = skill;
			if (skl != null) {
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
		};
		final ListenedButton okButton = new ListenedButton("OK", okListener);
		hours.setActionCommand("OK");
		hours.addActionListener(okListener);
		// Clear if OK and no skill selected, on Cancel, and after
		// successfully adding skill
		final ListenedButton cancelButton =
				new ListenedButton("Cancel", evt -> hours.setText(""));
		final JPanel secondPanel;
		if (OnMac.SYSTEM_IS_MAC) {
			okButton.putClientProperty("JButton.buttonType", "segmented");
			cancelButton.putClientProperty("JButton.buttonType", "segmented");
			okButton.putClientProperty("JButton.segmentPosition", "first");
			cancelButton.putClientProperty("JButton.segmentPosition", "last");
			secondPanel = new BoxPanel(true);
			final BoxPanel boxView = (BoxPanel) secondPanel;
			boxView.addGlue();
			secondPanel.add(okButton);
			boxView.addRigidArea(2);
			secondPanel.add(cancelButton);
			boxView.addGlue();
		} else {
			secondPanel = new JPanel();
			secondPanel.setLayout(new FlowLayout());
			secondPanel.add(okButton);
			secondPanel.add(cancelButton);
		}
		add(secondPanel);
		setMinimumSize(new Dimension(200, 40));
		setPreferredSize(new Dimension(220, MAX_PANEL_HEIGHT));
		setMaximumSize(new Dimension(240, MAX_PANEL_HEIGHT));
	}

	/**
	 * @param selectedSkill the newly selected skill.
	 */
	@SuppressWarnings("VariableNotUsedInsideIf")
	@Override
	public void selectSkill(@Nullable final ISkill selectedSkill) {
		skill = selectedSkill;
		if (skill != null) {
			hours.requestFocusInWindow();
		}
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

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final @Nullable ISkill locSkill = skill;
		if (locSkill == null) {
			return "SkillAdvancementPanel displaying null";
		} else {
			return "SkillAdvancementPanel displaying " + locSkill.getName();
		}
	}
}
