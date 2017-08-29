import ceylon.random {
    Random,
    DefaultRandom
}

"A single [[Random]] for the whole application."
shared Random singletonRandom = DefaultRandom();