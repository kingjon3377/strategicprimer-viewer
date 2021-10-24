package common.map.fixtures.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A test that {@link ProxyAnimal#reduced} works properly.
 */
public class TestProxyAnimal {
	/**
	 * Test that the basic {@link Animal#reduced} works the way we expect.
	 */
	@ParameterizedTest
	@MethodSource
	public void testProxyAnimalReduction(int id, int newId) {
		// TODO: should take population of original and population to test as parameters?
		Animal base = new AnimalImpl("test", false, "status", id, -1, 12);
		ProxyAnimal proxy = new ProxyAnimal(base, base.copy(false), base.copy(false));
		assertEquals(3, base.reduced(3, newId).getPopulation(),
			"Test that reduced() works the way we expect in the non-proxy case.");
		Animal reduced = proxy.reduced(3, newId);
		assertTrue(reduced instanceof ProxyAnimal);
		for (Animal proxied : ((ProxyAnimal) reduced).getProxied()) {
			assertEquals(newId, proxied.getId());
			assertEquals(3, proxied.getPopulation());
		}
	}

	/**
	 * @by J. Dimeo https://stackoverflow.com/a/49032736
	 * @by Jonathan Lovelace (adapting to Arguments of ints)
	 */
	private static class PairCollater implements Function<Integer, Stream<Arguments>> {
		Integer prev;

		@Override
		public Stream<Arguments> apply(Integer curr) {
			if (prev == null) {
				prev = curr;
				return Stream.empty();
			}
			try {
				return Stream.of(Arguments.of(prev, curr));
			} finally {
				prev = null;
			}
		}
	}

	static Stream<Arguments> testProxyAnimalReduction() {
		ThreadLocalRandom tlr = ThreadLocalRandom.current();
		return tlr.ints(0, Integer.MAX_VALUE - 2).boxed().sequential()
			.flatMap(new PairCollater()).limit(3);
	}
}
