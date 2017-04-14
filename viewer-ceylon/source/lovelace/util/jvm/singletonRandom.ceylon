import ceylon.random {
    Random,
    DefaultRandom
}

import lovelace.util.common {
    todo
}
"A single [[Random]] for the whole application."
todo("Replace with wrapper around the `random` in `ceylon.math`")
shared Random singletonRandom = DefaultRandom();