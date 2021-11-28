package impl.dbio;

import buckelieg.jdbc.fn.DB;

interface DatabaseWriter<Item, Context> {
	/**
	 * Set up the tables that this writer uses on the given connection.
	 * Should be a no-op if called with the same Sql again.
	 */
	void initialize(DB db);

	/**
	 * Write an object to the database.
	 */
	void write(DB db, Item obj, Context context);

	/**
	 * Whether we can write the given object. Should generally be equivalent to 
	 * <code>obj instanceof Item &amp;&amp; context instanceof
	 * Context</code>, but we can't write that here in Java as type
	 * parameters aren't reified.
	 */
	boolean canWrite(Object obj, Object context);

	/**
	 * Write the given object, when the caller knows the object is the
	 * right type but the typechecker doesn't. This will probably crash the
	 * program if the types don't in fact match.
	 */
	default void writeRaw(DB db, Object obj, Object context) {
		write(db, (Item) obj, (Context) context);
	}
}
