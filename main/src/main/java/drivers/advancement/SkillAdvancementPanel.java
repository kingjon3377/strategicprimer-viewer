package drivers.advancement;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;

import static lovelace.util.ShowErrorDialog.showErrorDialog;
import lovelace.util.Platform;
import static lovelace.util.BoxPanel.centeredHorizontalBox;
import lovelace.util.BorderedPanel;
import lovelace.util.FlowPanel;

import lovelace.util.SingletonRandom;

import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.IJob;

import drivers.worker_mgmt.UnitMemberListener;

import common.map.fixtures.UnitMember;

import common.map.fixtures.mobile.IWorker;

import drivers.common.IAdvancementModel;

/**
 * A panel to let a user add hours of experience to a Skill.
 */
/* package */ final class SkillAdvancementPanel extends BorderedPanel
		implements SkillSelectionListener, LevelGainSource, UnitMemberListener {
	private static JPanel secondPanelFactory(JButton... buttons) {
		Platform.makeButtonsSegmented(buttons);
		if (Platform.SYSTEM_IS_MAC) {
			return centeredHorizontalBox(buttons);
		} else {
			return new FlowPanel(buttons);
		}
	}

	// TODO: can we make these final? They had to be 'late' in Ceylon
	private JTextField hours; // FIXME: Why not use a spinner?
	private IAdvancementModel model;

	@Nullable
	private ISkill skill = null;

	@Nullable
	private IJob job = null;

	@Nullable
	private IWorker worker = null;

	private final List<LevelGainListener> listeners = new ArrayList<>();

	private void okListener(ActionEvent event) {
		// TODO: In Ceylon we assigned all of these to local variables, as the typechecker
		// enforced thread-safety of reading mutable nullable variables; we should really
		// do the same here to avoid TOCTOU races
		if (worker != null && job != null && skill != null) {
			int level = skill.getLevel();
			int number;
			try {
				number = Integer.parseInt(hours.getText());
			} catch (NumberFormatException except) {
				showErrorDialog(hours, "Strategic Primer Worker Advancement",
					"Hours to add must be a number");
				return;
			}
			// TODO: Make frequency of leveling checks (i.e. size of hour-chunks to
			// add at a time) configurable. This is correct (per documentation before
			// I added support for workers to the map format) for ordinary experience,
			// but workers learning or working under a more experienced mentor can get
			// multiple "hours" per hour, and they should only check for a level with
			// each *actual* hour.
			for (int hour = 0; hour < number; hour++) {
				model.addHoursToSkill(worker, job.getName(), skill.getName(), 1,
					SingletonRandom.SINGLETON_RANDOM.nextInt(100));
			}
			int newLevel = skill.getLevel();
			if (newLevel != level) {
				for (LevelGainListener listener : listeners) {
					// TODO: What if it's a proxy for all workers in a unit?
					listener.level(worker.getName(), job.getName(),
						skill.getName(), newLevel - level, newLevel);
				}
			}
		} // FIXME: Better diagnostics on which condition of 'if' failed in an 'else' clause
		// Clear if OK and no skill selected, on Cancel, and after successfully adding skill
		hours.setText("");
	}

	private void cancelListener(ActionEvent event) {
		hours.setText("");
	}

	// FIXME: Remove if never called
	public static SkillAdvancementPanel delegate(IAdvancementModel model, JTextField hours,
			JButton okButton, JButton cancelButton) {
		return new SkillAdvancementPanel(model, hours, okButton, cancelButton);
	}

	// FIXME: Inline into public constructor if static factory is never called
	private SkillAdvancementPanel(IAdvancementModel model, JTextField hours, JButton okButton,
			JButton cancelButton) {
		super(null, new FlowPanel(new JLabel("Add "), hours, new JLabel(" hours to skill?")),
			secondPanelFactory(okButton, cancelButton));
		this.model = model;
		okButton.addActionListener(this::okListener);
		cancelButton.addActionListener(this::cancelListener);
		hours.setActionCommand("OK");
		hours.addActionListener(this::okListener);
		this.hours = hours;
		setMinimumSize(new Dimension(200, 40));
		setPreferredSize(new Dimension(220, 60));
		setMaximumSize(new Dimension(240, 60));
	}

	public SkillAdvancementPanel(IAdvancementModel model) {
		this(model, new JTextField(3), new JButton("OK"), new JButton("Cancel"));
	}

	@Override
	public void selectSkill(@Nullable ISkill selectedSkill) {
		skill = selectedSkill;
		if (selectedSkill != null) {
			hours.requestFocusInWindow();
		}
	}

	@Override
	public void selectJob(@Nullable IJob selectedJob) {
		job = selectedJob;
	}

	@Override
	public void addLevelGainListener(LevelGainListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeLevelGainListener(LevelGainListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void memberSelected(@Nullable UnitMember previousSelection,
			@Nullable UnitMember selected) {
		if (selected instanceof IWorker) {
			worker = (IWorker) selected;
		} else {
			worker = null;
		}
	}
}
