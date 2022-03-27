package impl.dbio;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.SQLException;
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
	public abstract List<Query> getInitializers();

	/**
	 * Database connections that we've been initialized for.
	 * TODO: Is this really best practice in the jdbc-fn library?
	 */
	private final Set<Transactional> connections = new HashSet<>();

	@Override
	public void initialize(final Transactional sql) throws SQLException {
		if (!connections.contains(sql)) {
			sql.transaction().accept(db -> {
				for (final Query initializer : getInitializers()) {
					initializer.execute(db);
					log.fine("Executed initializer beginning " +
						initializer.rawSql().split("\\R")[0]);
				}
			});
			connections.add(sql);
		}
	}
}
