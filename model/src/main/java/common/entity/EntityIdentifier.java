package common.entity;

import org.jspecify.annotations.NonNull;

/**
 * An interface for ways of identifying entities in the game-world. This is an interface to possibly allow us to import
 * maps from the 2009-2022 campaign, and to allow us to extend the format over time without breaking changes.
 *
 * TODO: What should the interface contain?
 */
public interface EntityIdentifier {
	@NonNull
	String getIdentifierString();
}
