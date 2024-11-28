package drivers.advancement;

import drivers.common.cli.ICLIHelper;
import org.jetbrains.annotations.Nullable;

import legacy.map.HasName;

import legacy.map.fixtures.UnitMember;

import drivers.worker_mgmt.UnitMemberListener;

import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.IJob;
import drivers.common.LevelGainListener;

import java.util.Objects;

/**
 * A listener to print a line whenever a worker gains a level.
 */
/* package */ final class LevelListener implements LevelGainListener, UnitMemberListener, SkillSelectionListener {
	private final ICLIHelper cli;

	/**
	 * Constructor.
	 */
	public LevelListener(final ICLIHelper cli) {
		this.cli = cli;
	}

	/**
	 * The current worker.
	 */
	private @Nullable UnitMember worker = null;

	/**
	 * The current skill.
	 */
	private @Nullable ISkill skill = null;

	@Override
	public void selectSkill(final @Nullable ISkill selectedSkill) {
		skill = selectedSkill;
	}

	/**
	 * We don't actually print the Job name, so we don't track it.
	 *
	 * TODO: Probably should
	 */
	@Override
	public void selectJob(final @Nullable IJob selectedJob) {
	}

	@Override
	public void memberSelected(final @Nullable UnitMember old, final @Nullable UnitMember selected) {
		worker = selected;
	}

	/**
	 * Wrapper around {@link HasName#getName} that also handles non-{@link
	 * HasName} objects using their {@link Object#toString toString} method.
	 */
	private static String getName(final Object named) {
		if (named instanceof final HasName n) {
			return n.getName();
		} else {
			return named.toString();
		}
	}

	/**
	 * Notify the user of a gained level.
	 *
	 * TODO: This is less thread-safe than it was in Ceylon; should
	 * probably make local copies of fields at the start instead of going
	 * to the shared copies throughout the method.
	 */
	@Override
	public void level(final String workerName, final String jobName, final String skillName, final long gains,
					  final int currentLevel) {
		final String actualWorkerName;
		final String actualSkillName;
		if (!workerName.isEmpty() && !"unknown".equals(workerName)) {
			actualWorkerName = workerName;
		} else if (Objects.isNull(worker)) {
			return;
		} else {
			actualWorkerName = getName(worker);
		}
		if (!skillName.isEmpty() && !"unknown".equals(skillName)) {
			actualSkillName = skillName;
		} else if (Objects.isNull(skill)) {
			return;
		} else {
			actualSkillName = getName(skill);
		}
		final String count = (gains == 1) ? "a level" : gains + " levels";
		cli.print(actualWorkerName, " gained ", count, " in ", actualSkillName, ". ");
	}
}
