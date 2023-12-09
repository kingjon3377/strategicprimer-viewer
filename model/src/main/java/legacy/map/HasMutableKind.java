package legacy.map;

/**
 * An interface for fixtures that have a "kind" property that is mutable.
 */
public interface HasMutableKind extends HasKind {
    /**
     * Set the kind of whatever this is.
     */
    void setKind(String kind);
}
