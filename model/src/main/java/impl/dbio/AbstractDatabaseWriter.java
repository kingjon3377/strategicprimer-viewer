package impl.dbio;

import buckelieg.jdbc.fn.DB;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

abstract class AbstractDatabaseWriter<Item, Context> implements DatabaseWriter<Item, Context> {
	protected AbstractDatabaseWriter(Class<Item> itemClass, Class<Context> contextClass) {
		this.itemClass = itemClass;
		this.contextClass = contextClass;
	}

	protected final Logger log = Logger.getLogger(getClass().getName());

	private final Class<Item> itemClass;
	private final Class<Context> contextClass;

	@Override
	public boolean canWrite(Object obj, Object context) {
		return itemClass.isInstance(obj) && contextClass.isInstance(context);
	}

	/**
	 * SQL to run to initialize the needed tables.
	 */
	public abstract Iterable<String> getInitializers();

	/**
	 * Database connections that we've been initialized for.
	 * TODO: Is this really best practice in the jdbc-fn library?
	 */
	private final Set<DB> connections = new HashSet<>();

	@Override
	public void initialize(DB sql) {
		if (!connections.contains(sql)) {
			sql.transaction(db -> {
				for (String initializer : getInitializers()) {
					db.script(initializer).execute();
					log.fine("Executed initializer beginning " +
						initializer.split("\\R")[0]);
				}
				connections.add(sql);
				return true;
			});
		}
	}
}
