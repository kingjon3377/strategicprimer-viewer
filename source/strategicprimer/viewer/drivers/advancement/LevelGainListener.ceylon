import java.util {
    EventListener
}

import lovelace.util.common {
    todo
}
"An interface for objects that want to be notified when a worker gains a level in the
 currently selected skill."
shared interface LevelGainListener satisfies EventListener {
    "Handle a gained level."
    todo("If possible, specify what skill, what worker, etc. here, so callers don't have
          to deduce that information")
    shared formal void level();
}
