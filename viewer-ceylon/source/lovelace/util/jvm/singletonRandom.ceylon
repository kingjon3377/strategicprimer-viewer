import ceylon.random {
    Random,
    DefaultRandom
}

import lovelace.util.common {
    todo
}
"A single [[Random]] for the whole application."
shared Random singletonRandom = DefaultRandom();