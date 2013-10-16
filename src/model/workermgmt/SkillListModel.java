package model.workermgmt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A list model for a list of the skills associated with a Job.
 *
 * @author Jonathan Lovelace
 */
public class SkillListModel extends DefaultListModel<Skill> implements
		CompletionSource, AddRemoveListener, CompletionListener {
	/**
	 * A non-null "null" Job. Adjusted here to prevent modification.
	 */
	private static final Job NULL_JOB = new Job("null", -1) {
		@Override
		public boolean addSkill(final Skill skill) {
			return false;
		}
	};
	/**
	 * The current Job.
	 */
	private Job job = NULL_JOB;

	/**
	 * Constructor.
	 *
	 * @param sources property-change sources to listen to.
	 */
	public SkillListModel(final CompletionSource... sources) {
		if (sources.length == 0) {
			throw new IllegalStateException("No sources given");
		}
		for (final CompletionSource source : sources) {
			source.addCompletionListener(this);
		}
	}

	/**
	 * @param result the object we were waiting on
	 */
	@Override
	public void stopWaitingOn(final Object result) {
		if ("null_job".equals(result)) {
			handleNewJob(null);
		} else if (result instanceof Job) {
			handleNewJob((Job) result);
		} else if ("level".equals(result)) {
			fireContentsChanged(this, 0, getSize());
		}
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
				list.stopWaitingOn(skill);
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
	private void handleNewJob(@Nullable final Job newValue) {
		if (!job.equals(newValue)) {
			clear();
			job = newValue == null ? NULL_JOB : newValue;
			for (Skill skill : job) {
				addElement(skill);
			}
			final Object retval = isEmpty() ? Integer.valueOf(-1) : Integer
					.valueOf(0);
			assert retval != null;
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(retval);
			}
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<CompletionListener> cListeners = new ArrayList<>();

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
