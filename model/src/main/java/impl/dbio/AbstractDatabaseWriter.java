package impl.dbio;

import buckelieg.jdbc.fn.DB;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

abstract class AbstractDatabaseWriter<Item, Context> implements DatabaseWriter<Item, Context> {
	protected AbstractDatabaseWriter(final Class<Item> itemClass, final Class<Context> contextClass) {
		this.itemClass = itemClass;
		this.contextClass = contextClass;
	}

	protected final Logger log = Logger.getLogger(getClass().getName());

	private final Class<Item> itemClass;
	private final Class<Context> contextClass;

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return itemClass.isInstance(obj) && contextClass.isInstance(context);
	}

	/**
	 * SQL to run to initialize the needed tables.
	 */
	public abstract List<String> getInitializers();

	/**
	 * Database connections that we've been initialized for.
	 * TODO: Is this really best practice in the jdbc-fn library?
	 */
	private final Set<DB> connections = new HashSet<>();

	@Override
	public void initialize(final DB sql) {
		if (!connections.contains(sql)) {
			sql.transaction(db -> {
				for (final String initializer : getInitializers()) {
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
