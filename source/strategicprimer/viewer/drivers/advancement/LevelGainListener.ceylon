import java.util {
    EventListener
}

"An interface for objects that want to be notified when a worker gains a level in the
 currently selected skill."
shared interface LevelGainListener satisfies EventListener {
    "Handle a gained level."
    shared formal void level(String workerName, String jobName, String skillName,
        Integer gains, Integer currentLevel);
}
