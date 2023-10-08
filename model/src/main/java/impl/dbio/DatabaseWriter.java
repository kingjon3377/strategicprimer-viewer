package impl.dbio;

import io.jenetics.facilejdbc.Transactional;

import java.sql.SQLException;

// TODO: Can we get rid of the Context parameter? Is it ever not a subtype of IFixture?
interface DatabaseWriter<Item, Context> {
	/**
	 * Set up the tables that this writer uses on the given connection.
	 * Should be a no-op if called with the same Sql again.
	 */
	void initialize(Transactional db) throws SQLException;

	/**
	 * Write an object to the database.
	 */
	void write(Transactional db, Item obj, Context context) throws SQLException;

	/**
	 * Whether we can write the given object. Should generally be equivalent to
	 * {@code obj instanceof Item && context instanceof
	 * Context}, but we can't write that here in Java as type
	 * parameters aren't reified.
	 */
	boolean canWrite(Object obj, Object context);

	/**
	 * Write the given object, when the caller knows the object is the
	 * right type but the typechecker doesn't. This will probably crash the
	 * program if the types don't in fact match.
	 */
	default void writeRaw(final Transactional db, final Object obj, final Object context) throws SQLException {
		write(db, (Item) obj, (Context) context);
	}
}
