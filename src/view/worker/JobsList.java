package view.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.JobSelectionListener;
import model.listeners.JobSelectionSource;
import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Job;
import model.workermgmt.JobsListModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A visual list of a worker's Jobs. We also handle listening for selection
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public final class JobsList extends JList<Job> implements
		ListSelectionListener, JobSelectionSource, AddRemoveListener,
		UnitMemberListener, CompletionListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<JobSelectionListener> jsListeners = new ArrayList<>();
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
		lmodel.addCompletionListener(this);
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		@Nullable
		final Job temp = getSelectedValue();
		for (final JobSelectionListener list : jsListeners) {
			list.selectJob(temp);
		}
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
	 * @param old passed to list model
	 * @param selected passed to list model
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		lmodel.memberSelected(old, selected);
	}
	/**
	 * @param end whether to slip to the end
	 */
	@Override
	public void stopWaitingOn(final boolean end) {
		if (end) {
			setSelectedIndex(lmodel.size() - 1);
		} else {
			setSelectedIndex(0);
		}
	}
}
