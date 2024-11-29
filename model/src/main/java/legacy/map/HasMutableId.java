package legacy.map;

/**
 * An interface for the few fixtures whose IDs are variable (in both cases because they originally didn't have IDs, and
 * when sans-ID fixtures are read from a map we need a way to fix them *after* reading all the other fixtures in the map
 * to be able to know what IDs are in use).
 *
 * @author Jonathan Lovelace
 */
public interface HasMutableId extends IFixture {
	void setId(int id);
}
