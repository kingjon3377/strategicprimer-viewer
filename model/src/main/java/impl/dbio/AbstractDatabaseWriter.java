package impl.dbio;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

import lovelace.util.LovelaceLogger;

public abstract class AbstractDatabaseWriter<Item, Context> implements DatabaseWriter<Item, Context> {
	private static final Pattern LINEBREAK = Pattern.compile("\\R");

	protected AbstractDatabaseWriter(final Class<Item> itemClass, final Class<Context> contextClass) {
		this.itemClass = itemClass;
		this.contextClass = contextClass;
	}

	private final Class<Item> itemClass;
	private final Class<Context> contextClass;

	@SuppressWarnings("DesignForExtension")
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
	private final Collection<Transactional> connections = new HashSet<>();

	@Override
	public final void initialize(final Transactional sql) throws SQLException {
		if (!connections.contains(sql)) {
			sql.transaction().accept(db -> {
				for (final Query initializer : getInitializers()) {
					initializer.execute(db);
					LovelaceLogger.debug("Executed initializer beginning %s",
							LINEBREAK.split(initializer.rawSql())[0]);
				}
			});
			connections.add(sql);
		}
	}
}
