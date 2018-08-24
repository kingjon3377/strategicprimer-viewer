import ceylon.random {
    Random,
    DefaultRandom
}

"A single [[Random]] for the whole application."
shared Random singletonRandom = DefaultRandom();

// TODO: Uncomment once eclipse/ceylon#6986 fixed
/*"An annotation to make a parameterized test randomly generate numbers."
by("Stéphane Épardaud", "Jonathan Lovelace")
shared annotation RandomGenerationAnnotation randomlyGenerated(
            Integer max /*= runtime.maxIntegerValue*/) // TODO: Investigate why it won't let me provide that as the default
		=> RandomGenerationAnnotation(max);

shared final annotation class RandomGenerationAnnotation(Integer max)
		satisfies OptionalAnnotation<RandomGenerationAnnotation,FunctionOrValueDeclaration>
            & ArgumentProvider {
	shared actual {Anything*} arguments(ArgumentProviderContext context) =>
        singletonRandom.integers(max);
} */