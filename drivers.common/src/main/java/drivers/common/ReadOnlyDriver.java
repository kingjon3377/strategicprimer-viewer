package drivers.common;

/**
 * An interface for drivers which operate on a map model of some kind but never
 * want to have its contents written back to disk (automatically).
 */
public interface ReadOnlyDriver extends ModelDriver {
}
