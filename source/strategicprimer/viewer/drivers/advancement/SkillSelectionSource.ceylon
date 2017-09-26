import lovelace.util.common {
    todo
}

"An interface for objects that handle the user's selection of Skills."
todo("is there a more generic interface that could be used instead?")
interface SkillSelectionSource {
    "Notify the given listener of newly selected skills."
    shared formal void addSkillSelectionListener(SkillSelectionListener listener);
    "Stop notifying the given listener."
    shared formal void removeSkillSelectionListener(SkillSelectionListener listener);
}
