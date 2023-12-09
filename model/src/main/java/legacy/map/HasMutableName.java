package legacy.map;

import common.map.HasName;

/**
 * An interface for things that have a name that can change.
 */
public interface HasMutableName extends HasName {
    /**
     * Set the name of whatever this is.
     */
    void setName(String name);
}
