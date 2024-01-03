package legacy.map.fixtures.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import legacy.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;
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
    public void testProxyAnimalReduction(final int id, final int newId) {
        // TODO: should take population of original and population to test as parameters?
        final Animal base = new AnimalImpl("test", false, "status", id, -1, 12);
        final ProxyAnimal proxy = new ProxyAnimal(base, base.copy(IFixture.CopyBehavior.KEEP),
                base.copy(IFixture.CopyBehavior.KEEP));
        assertEquals(3, base.reduced(3, newId).getPopulation(),
                "Test that reduced() works the way we expect in the non-proxy case.");
        final Animal reduced = proxy.reduced(3, newId);
        assertInstanceOf(ProxyAnimal.class, reduced, "Proxy reduction produces another proxy");
        for (final Animal proxied : ((ProxyAnimal) reduced).getProxied()) {
            assertEquals(newId, proxied.getId(), "Each animal proxied by it has the correct ID");
            assertEquals(3, proxied.getPopulation(),
                    "Each animal proxied by it has the correct population");
        }
    }

    /**
     * @author J. Dimeo https://stackoverflow.com/a/49032736
     * @author Jonathan Lovelace (adapting to Arguments of ints)
     */
    private static class PairCollater implements Function<Integer, Stream<Arguments>> {
        @Nullable
        Integer prev;

        @Override
        public Stream<Arguments> apply(final Integer curr) {
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
        final ThreadLocalRandom tlr = ThreadLocalRandom.current();
        return tlr.ints(0, Integer.MAX_VALUE - 2).boxed().sequential()
                .flatMap(new PairCollater()).limit(3);
    }
}
