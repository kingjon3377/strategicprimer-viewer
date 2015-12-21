package model.workermgmt;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A list model for a list of a worker's jobs.
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
public final class JobsListModel extends DefaultListModel<IJob> implements
		UnitMemberListener, CompletionSource, AddRemoveListener {
	/**
	 * The current worker.
	 */
	@Nullable
	private IWorker worker = null;

	/**
	 * @param category what kind of thing is being added; if not a Job we ignore it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		final IWorker local = worker;
		if ("job".equals(category) && local != null) {
			final IJob job = new Job(addendum, 0);
			local.addJob(job);
			addElement(job);
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(true);
			}
		}
	}

	/**
	 * @param old      the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
	                           @Nullable final UnitMember selected) {
		if (!Objects.equals(worker, selected)) {
			clear();
			if (selected instanceof IWorker) {
				worker = (IWorker) selected;
				worker.forEach(this::addElement);
				for (final CompletionListener list : cListeners) {
					list.stopWaitingOn(false);
				}
			} else {
				worker = null;
			}
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();

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

	/**
	 * @param category ignored
	 */
	@Override
	public void remove(final String category) {
		// Not implemented.
	}
}
