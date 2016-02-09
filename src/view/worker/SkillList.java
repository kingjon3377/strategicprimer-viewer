package view.worker;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import model.listeners.JobSelectionListener;
import model.listeners.LevelGainListener;
import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.workermgmt.SkillListModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A visual list of a job's skills. Also handles listening for selection changes.
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
public final class SkillList extends JList<ISkill> implements SkillSelectionSource,
		                                                              LevelGainListener,
		                                                              JobSelectionListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<SkillSelectionListener> ssListeners = new ArrayList<>();
	/**
	 * The list model.
	 */
	private final SkillListModel lmodel;

	/**
	 * Constructor.
	 */
	public SkillList() {
		lmodel = new SkillListModel();
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
			final ISkill temp = getSelectedValue();
			for (final SkillSelectionListener list : ssListeners) {
				list.selectSkill(temp);
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSkillSelectionListener(
			                                        final SkillSelectionListener list) {
		ssListeners.remove(list);
	}

	/**
	 * We just pass the event on to the list model, which is the object *really*
	 * concerned
	 * about level events.
	 */
	@Override
	public void level() {
		lmodel.level();
	}

	/**
	 * @param job passed on to list model
	 */
	@Override
	public void selectJob(@Nullable final IJob job) {
		lmodel.selectJob(job);
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
