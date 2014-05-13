package model.workermgmt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.JobSelectionListener;
import model.listeners.LevelGainListener;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A list model for a list of the skills associated with a Job.
 *
 * @author Jonathan Lovelace
 */
public final class SkillListModel extends DefaultListModel<ISkill> implements
		CompletionSource, AddRemoveListener, JobSelectionListener,
		LevelGainListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * A non-null "null" Job. Adjusted here to prevent modification.
	 * TODO: Make an anonymous object of type IJob, not Job.
	 */
	private static final Job NULL_JOB = new Job("null", -1) {
		@Override
		public boolean addSkill(final ISkill skill) {
			return false;
		}
	};
	/**
	 * The current Job.
	 */
	private IJob job = NULL_JOB;

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
	 * @param category what kind of thing is being added; if not a Skill we
	 *        ignore it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		if ("skill".equals(category) && !NULL_JOB.equals(job)) {
			final Skill skill = new Skill(addendum, 0, 0);
			job.addSkill(skill);
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
		if (!job.equals(newValue)) {
			clear();
			job = NullCleaner.valueOrDefault(newValue, NULL_JOB);
			for (final ISkill skill : job) {
				addElement(skill);
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
