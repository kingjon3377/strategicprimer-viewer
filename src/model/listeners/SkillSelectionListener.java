package model.listeners;

import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when the user selects a Skill from a
 * list or tree.
 *
 * @author Jonathan Lovelace
 *
 */
public interface SkillSelectionListener {
	/**
	 * @param skill the newly selected Skill. May be null if no selection.
	 */
	void selectSkill(@Nullable final Skill skill);
}
