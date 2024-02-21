package legacy.map.fixtures;

import legacy.map.IFixture;

import java.util.stream.Stream;

/**
 * A (marker) interface for fixtures that contain other fixtures in a way we
 * want to support iterating over, since Java doesn't have reified generics, but
 * we need to be able to distinguish them from other types that are {@link
 * Iterable} but iterate over something other than {@link IFixture fixtures}.
 */
public interface FixtureIterable<Type extends IFixture> extends Iterable<Type> {
    Stream<Type> stream();
}
