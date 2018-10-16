import ceylon.random {
    Random,
    DefaultRandom
}

"A single [[Random]] instance for the whole application."
shared Random singletonRandom = DefaultRandom();
