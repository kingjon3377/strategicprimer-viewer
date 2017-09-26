"An interface for objects that can indicate a gained level."
shared interface LevelGainSource {
    "Notify the given listener of future gained levels."
    shared formal void addLevelGainListener(LevelGainListener listener);
    "Stop notifying the given listener of gained levels."
    shared formal void removeLevelGainListener(LevelGainListener listener);
}