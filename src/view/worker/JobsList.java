package view.worker;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import model.listeners.AddRemoveListener;
import model.listeners.JobSelectionListener;
import model.listeners.JobSelectionSource;
import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
import model.workermgmt.JobsListModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A visual list of a worker's Jobs. We also handle listening for selection changes.
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
public final class JobsList extends JList<IJob> implements
		JobSelectionSource, AddRemoveListener,
				UnitMemberListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<JobSelectionListener> jsListeners = new ArrayList<>();
	/**
	 * The list model.
	 */
	private final JobsListModel lmodel;

	/**
	 * Constructor.
	 */
	public JobsList() {
		lmodel = new JobsListModel();
		setModel(lmodel);
		lmodel.addCompletionListener(end -> {
			if (end) {
				setSelectedIndex(lmodel.size() - 1);
			} else {
				setSelectedIndex(0);
			}
		});
		addListSelectionListener(evt -> {
			@Nullable
			final IJob temp = getSelectedValue();
			for (final JobSelectionListener list : jsListeners) {
				list.selectJob(temp);
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addJobSelectionListener(final JobSelectionListener list) {
		jsListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeJobSelectionListener(final JobSelectionListener list) {
		jsListeners.remove(list);
	}

	/**
	 * @param category passed to list model
	 * @param addendum passed to list model
	 */
	@Override
	public void add(final String category, final String addendum) {
		lmodel.add(category, addendum);
	}

	/**
	 * @param category passed to list model
	 */
	@Override
	public void remove(final String category) {
		lmodel.remove(category);
	}

	/**
	 * @param old      passed to list model
	 * @param selected passed to list model
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
	                           @Nullable final UnitMember selected) {
		lmodel.memberSelected(old, selected);
	}
}
