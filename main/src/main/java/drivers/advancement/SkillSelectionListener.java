package drivers.advancement;

import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.IJob;

import org.jspecify.annotations.Nullable;

/**
 * An interface for objects that want to know when the user selects a Skill from a list or tree.
 */
/* package */ interface SkillSelectionListener {
	/**
	 * Handle a new skill being selected.
	 */
	void selectSkill(@Nullable ISkill selectedSkill);

	/**
	 * Tell the listener that the skill that may be selected soon is in the given Job.
	 */
	void selectJob(@Nullable IJob selectedJob);
}
