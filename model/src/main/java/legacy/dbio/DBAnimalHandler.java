package legacy.dbio;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.MaturityModel;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.AnimalOrTracks;
import common.xmlio.Warning;

import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.OptionalInt;

import static io.jenetics.facilejdbc.Param.value;

public final class DBAnimalHandler extends AbstractDatabaseWriter<AnimalOrTracks, /*Point|IUnit|IWorker*/Object>
		implements MapContentsReader {
	public DBAnimalHandler() {
		super(AnimalOrTracks.class, Object.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Animal || obj instanceof AnimalTracks) &&
				(context instanceof Point || context instanceof IUnit);
	}


	private static OptionalInt born(final Animal animal) {
		final Map<String, Integer> model = MaturityModel.getMaturityAges();
		if (model.containsKey(animal.getKind())) {
			final int maturityAge = model.get(animal.getKind());
			if (maturityAge <= (DBMapWriter.currentTurn - animal.getBorn())) {
				return OptionalInt.empty();
			}
		}
		return OptionalInt.of(animal.getBorn());
	}

	private static final List<Query> INITIALIZERS = List.of(Query.of("CREATE TABLE IF NOT EXISTS animals (" +
					"    row INTEGER," +
					"    column INTEGER" +
					"    CHECK ((animals.row IS NOT NULL AND column IS NOT NULL) OR" +
					"        (animals.row IS NULL AND column IS NULL))," +
					"    parent INTEGER" +
					"    CHECK ((row IS NOT NULL AND parent IS NULL) OR" +
					"        (row IS NULL AND parent IS NOT NULL))," +
					"    kind VARCHAR(32) NOT NULL," +
					"    talking BOOLEAN NOT NULL," +
					"    status VARCHAR(32) NOT NULL," +
					"    born INTEGER," +
					"    count INTEGER NOT NULL," +
					"    id INTEGER NOT NULL," +
					"    image VARCHAR(255)" +
					");"),
			// We assume that animal tracks can't occur inside a unit or fortress.
			Query.of("CREATE TABLE IF NOT EXISTS tracks (" +
					"    row INTEGER NOT NULL," +
					"    column INTEGER NOT NULL," +
					"    kind VARCHAR(32) NOT NULL," +
					"    image VARCHAR(255)" +
					");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_TRACKS = Query.of("INSERT INTO tracks (row, column, kind, image) " +
			"VALUES(:row, :column, :kind, :image);");

	private static final Query INSERT_ANIMAL = Query.of("INSERT INTO animals (row, column, parent, kind, " +
			"talking, status, born, count, id, image) " +
			"VALUES(:row, :column, :parent, :kind, :talking, :status, :born, :count, :id, :image);");

	@Override
	public void write(final Transactional db, final AnimalOrTracks obj, final Object context) throws SQLException {
		// TODO: Extract a helper taking the context & any number of Class args to assert it is instanceof one of them
		if (!((context instanceof Point) || (context instanceof IUnit) || (context instanceof IWorker))) {
			throw new IllegalArgumentException("context must be a point, a unit, or a worker");
		}
		switch (obj) {
			case AnimalTracks animalTracks -> {
				if (context instanceof IUnit || context instanceof IWorker) { // TODO: Invert test to use type-inferring instanceof
					throw new IllegalArgumentException("Animal tracks can't occur inside a unit or worker");
				}
				INSERT_TRACKS.on(value("row", ((Point) context).row()), value("column", ((Point) context).column()),
								value("kind", obj.getKind()), value("image", animalTracks.getImage()))
						.execute(db.connection());
			}
			case final Animal a -> {
				final List<Param> params = new ArrayList<>();
				if (context instanceof final Point p) {
					params.add(value("row", p.row()));
					params.add(value("column", p.column()));
				} else {
					params.add(value("parent", ((IFixture) context).getId()));
				}
				params.add(value("kind", obj.getKind()));
				params.add(value("talking", a.isTalking()));
				params.add(value("status", a.getStatus()));
				final OptionalInt born = born(a);
				if (born.isPresent()) {
					params.add(value("born", born.getAsInt()));
				}
				params.add(value("count", a.getPopulation()));
				params.add(value("id", obj.getId()));
				params.add(value("image", a.getImage()));
				INSERT_ANIMAL.on(params).execute(db.connection());
			}
			default -> {
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
	readAnimal(final IMutableLegacyMap map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final String kind = (String) dbRow.get("kind");
			final boolean talking = getBooleanValue(dbRow, "talking");
			final String status = (String) dbRow.get("status");
			final Integer born = (Integer) dbRow.get("born");
			final int count = (Integer) dbRow.get("count");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final AnimalImpl animal = new AnimalImpl(kind, talking, status,
					id, Objects.requireNonNullElse(born, -1), count);
			if (!Objects.isNull(image)) {
				animal.setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (!Objects.isNull(row) && !Objects.isNull(column)) {
				map.addFixture(new Point(row, column), animal);
			} else {
				multimapPut(containees, parentId, animal);
			}
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
	readTracks(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final String kind = (String) dbRow.get("kind");
			final String image = (String) dbRow.get("image");
			final AnimalTracks track = new AnimalTracks(kind);
			if (!Objects.isNull(image)) {
				track.setImage(image);
			}
			map.addFixture(new Point(row, column), track);
		};
	}

	private static final Query SELECT_ANIMALS = Query.of("SELECT * FROM animals");
	private static final Query SELECT_TRACKS = Query.of("SELECT * FROM tracks");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "animal populations", readAnimal(map, containees),
				SELECT_ANIMALS);
		handleQueryResults(db, warner, "animal tracks", readTracks(map),
				SELECT_TRACKS);
	}
}
