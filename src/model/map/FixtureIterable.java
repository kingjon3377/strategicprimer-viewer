package model.map;

/**
 * Interface to give us knowledge at run-time that an iterable is an iterable of
 * some sort of fixture.
 *
 * @author Jonathan Lovelace
 *
 * @param <T> the type of the iterable.
 */
public interface FixtureIterable<T extends IFixture> extends Iterable<T> {
	// Nothing new.
}
