package view.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.listeners.CompletionListener;
import model.listeners.JobSelectionSource;
import model.listeners.LevelGainSource;
import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.SkillListModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A visual list of a job's skills. Also handles listening for selection
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public class SkillList extends JList<Skill> implements ListSelectionListener,
		SkillSelectionSource {
	/**
	 * Constructor.
	 *
	 * @param jsSources what our model should listen to for the currently selected Job
	 * @param lgSources what our model should listen to for skill leveling notifications
	 */
	public SkillList(final JobSelectionSource[] jsSources,
			final LevelGainSource[] lgSources) {
		final SkillListModel lmodel = new SkillListModel();
		for (final JobSelectionSource source : jsSources) {
			source.addJobSelectionListener(lmodel);
		}
		for (final LevelGainSource source : lgSources) {
			source.addLevelGainListener(lmodel);
		}
		setModel(lmodel);
		lmodel.addCompletionListener(new CompletionListener() {
			@Override
			public void stopWaitingOn(final boolean end) {
				if (end) {
					setSelectedIndex(lmodel.size() - 1);
				} else {
					setSelectedIndex(0);
				}
			}
		});
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		@Nullable
		final Skill temp = getSelectedValue();
		for (final SkillSelectionListener list : ssListeners) {
			list.selectSkill(temp);
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<SkillSelectionListener> ssListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.remove(list);
	}
}
