import ceylon.dbc {
	Sql
}
interface DatabaseWriter<out Item, in Value=Item> given Item satisfies Object {
	"Write an object to the database."
	shared formal void write(Sql db, Value obj);
	"Whether we can write the given object."
	shared default Boolean canWrite(Object obj) => obj is Item;
	"Write the given object, when the caller knows the object is the right type but the
	 typechecker doesn't. This will probably crash the program if the types don't in fact
	 match."
	shared default void writeRaw(Sql db, Object obj) {
		assert (is Value obj);
		write(db, obj);
	}
}