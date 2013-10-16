package view.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
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
		CompletionSource {
	/**
	 * Constructor.
	 *
	 * @param listener something that should listen to us
	 * @param sources what our model should listen to
	 */
	public SkillList(final CompletionListener listener,
			final CompletionSource... sources) {
		final SkillListModel lmodel = new SkillListModel(sources);
		setModel(lmodel);
		lmodel.addCompletionListener(new CompletionListener() {
			/**
			 * @param result what we were waiting on, or a signal value
			 */
			@Override
			public void stopWaitingOn(final Object result) {
				if (Integer.valueOf(0).equals(result)) {
					setSelectedIndex(0);
				} else if (result instanceof Skill) {
					setSelectedValue(result, true);
				}
			}
		});
		addCompletionListener(listener);
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		@Nullable
		final Object temp = getSelectedValue();
		final Object result = temp == null ? "null_skill" : temp;
		for (final CompletionListener list : cListeners) {
			list.stopWaitingOn(result);
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
	public final void addCompletionListener(final CompletionListener list) {
		cListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeCompletionListener(final CompletionListener list) {
		cListeners.remove(list);
	}
}
