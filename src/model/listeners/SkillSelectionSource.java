package model.listeners;
/**
 * An interface for objects that handle the user's selection of Skills.
 * @author Jonathan Lovelace
 *
 */
public interface SkillSelectionSource {
	/**
	 * @param list a listener to add
	 */
	void addSkillSelectionListener(SkillSelectionListener list);
	/**
	 * @param list a listener to remove
	 */
	void removeSkillSelectionListener(SkillSelectionListener list);
}
