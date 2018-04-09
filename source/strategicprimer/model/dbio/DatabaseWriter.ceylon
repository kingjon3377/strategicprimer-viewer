import ceylon.dbc {
	Sql
}
interface DatabaseWriter<in Item, in Context>
		given Item satisfies Object given Context satisfies Object {
	"Write an object to the database."
	shared formal void write(Sql db, Item obj, Context context);
	"Whether we can write the given object."
	shared default Boolean canWrite(Object obj, Object context) => obj is Item && context is Context;
	"Write the given object, when the caller knows the object is the right type but the
	 typechecker doesn't. This will probably crash the program if the types don't in fact
	 match."
	shared default void writeRaw(Sql db, Object obj, Object context) {
		assert (is Item obj, is Context context);
		write(db, obj, context);
	}
}