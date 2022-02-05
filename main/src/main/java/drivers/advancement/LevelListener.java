package drivers.advancement;

import org.jetbrains.annotations.Nullable;

import common.map.HasName;

import common.map.fixtures.UnitMember;

import drivers.worker_mgmt.UnitMemberListener;

import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.IJob;

/**
 * A listener to print a line whenever a worker gains a level.
 */
/* package */ class LevelListener implements LevelGainListener, UnitMemberListener, SkillSelectionListener {
	/**
	 * The current worker.
	 */
	@Nullable
	private UnitMember worker = null;

	/**
	 * The current skill.
	 */
	@Nullable
	private ISkill skill = null;

	@Override
	public void selectSkill(@Nullable final ISkill selectedSkill) {
		skill = selectedSkill;
	}

	/**
	 * We don't actually print the Job name, so we don't track it.
	 *
	 * TODO: Probably should
	 */
	@Override
	public void selectJob(@Nullable final IJob selectedJob) {}

	@Override
	public void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected) {
		worker = selected;
	}

	/**
	 * Wrapper around {@link HasName#getName} that also handles non-{@link
	 * HasName} objects using their {@link Object#toString toString} method.
	 */
	private static String getName(final Object named) {
		if (named instanceof HasName) {
			return ((HasName) named).getName();
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
	public void level(final String workerName, final String jobName, final String skillName, final int gains, final int currentLevel) {
		String actualWorkerName;
		String actualSkillName;
		if (!workerName.isEmpty() && !"unknown".equals(workerName)) {
			actualWorkerName = workerName;
		} else if (worker != null) { // TODO: invert?
			actualWorkerName = getName(worker);
		} else {
			return;
		}
		if (!skillName.isEmpty() && !"unknown".equals(skillName)) {
			actualSkillName = skillName;
		} else if (skill != null) { // TODO: invert?
			actualSkillName = getName(skill);
		} else {
			return;
		}
		String count = (gains == 1) ? "a level" : gains + " levels";
		System.out.printf("%s gained %s in %s", actualWorkerName, count, actualSkillName); // TODO: Take ICLIHelper instead of using stdout
	}
}
