import java.util {
    JRandom=Random
}

import lovelace.util.common {
    todo
}
"A single [[Random|JRandom]] for the whole application."
todo("Replace with wrapper around the `random` in `ceylon.math`")
shared JRandom singletonRandom = JRandom(system.milliseconds);