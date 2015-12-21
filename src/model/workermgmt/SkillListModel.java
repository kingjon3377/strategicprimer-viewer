package model.workermgmt;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.JobSelectionListener;
import model.listeners.LevelGainListener;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Skill;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A list model for a list of the skills associated with a Job.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class SkillListModel extends DefaultListModel<ISkill> implements
		CompletionSource, AddRemoveListener, JobSelectionListener,
				LevelGainListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * The current Job.
	 */
	@Nullable
	private IJob job = null;

	/**
	 * Handle level-up notification.
	 */
	@Override
	public void level() {
		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * @param nJob the newly selected Job.
	 */
	@Override
	public void selectJob(@Nullable final IJob nJob) {
		handleNewJob(nJob);
	}

	/**
	 * @param category what kind of thing is being added; if not a Skill we ignore it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		final IJob local = job;
		if ("skill".equals(category) && local != null) {
			final ISkill skill = new Skill(addendum, 0, 0);
			local.addSkill(skill);
			addElement(skill);
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(true);
			}
		}
	}

	/**
	 * @param category ignored
	 */
	@Override
	public void remove(final String category) {
		// Not implemented
	}

	/**
	 * Handle the "job" property changing.
	 *
	 * @param newValue the new value
	 */
	private void handleNewJob(@Nullable final IJob newValue) {
		if (!Objects.equals(job, newValue)) {
			clear();
			job = newValue;
			if (newValue != null) {
				newValue.forEach(this::addElement);
			}
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(true);
			}
		}
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addCompletionListener(final CompletionListener list) {
		cListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeCompletionListener(final CompletionListener list) {
		cListeners.remove(list);
	}
}
