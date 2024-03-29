package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * An entity can be located within, or "at the same location as", another entity. Care will need to be taken to ensure
 * there are no location loops.
 *
 * @param parent The entity that is the parent of the one this location object is embedded in.
 */
public record InParentLocation(@NotNull EntityIdentifier parent) implements Location {
}
