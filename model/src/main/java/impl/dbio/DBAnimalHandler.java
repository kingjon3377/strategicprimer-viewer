package impl.dbio;

import common.map.IFixture;
import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.AnimalOrTracks;
import common.xmlio.Warning;

import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static io.jenetics.facilejdbc.Param.value;

final class DBAnimalHandler extends AbstractDatabaseWriter<AnimalOrTracks, /*Point|IUnit|IWorker*/Object>
		implements MapContentsReader {
	public DBAnimalHandler() {
		super(AnimalOrTracks.class, Object.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Animal || obj instanceof AnimalTracks) &&
			(context instanceof Point || context instanceof IUnit);
	}


	private static Optional<Integer> born(final Animal animal) { // TODO: OptionalInt
		final Map<String, Integer> model = MaturityModel.getMaturityAges();
		if (model.containsKey(animal.getKind())) {
			final int maturityAge = model.get(animal.getKind());
			if (maturityAge <= (DBMapWriter.currentTurn - animal.getBorn())) {
				return Optional.empty();
			}
		}
		return Optional.of(animal.getBorn());
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
		if (!((context instanceof Point) || (context instanceof IUnit) || (context instanceof IWorker))) {
			throw new IllegalArgumentException("context must be a point, a unit, or a worker");
		}
		if (obj instanceof AnimalTracks) {
			if (context instanceof IUnit || context instanceof IWorker) {
				throw new IllegalArgumentException("Animal tracks can't occur inside a unit or worker");
			}
			INSERT_TRACKS.on(value("row", ((Point) context).getRow()), value("column", ((Point) context).getColumn()),
						value("kind", obj.getKind()), value("image", ((AnimalTracks) obj).getImage()))
					.execute(db.connection());
		} else if (obj instanceof Animal) {
			final List<Param> params = new ArrayList<>();
			if (context instanceof Point) {
				params.add(value("row", ((Point) context).getRow()));
				params.add(value("column", ((Point) context).getColumn()));
			} else {
				params.add(value("parent", ((IFixture) context).getId()));
			}
			params.add(value("kind", obj.getKind()));
			params.add(value("talking", ((Animal) obj).isTalking()));
			params.add(value("status", ((Animal) obj).getStatus()));
			Optional<Integer> born = born((Animal) obj);
			if (born.isPresent()) {
				params.add(value("born", born.get()));
			}
			params.add(value("count", ((Animal) obj).getPopulation()));
			params.add(value("id", obj.getId()));
			params.add(value("image", ((Animal) obj).getImage()));
			INSERT_ANIMAL.on(params).execute(db.connection());
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readAnimal(final IMutableMapNG map, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final String kind = (String) dbRow.get("kind");
			final boolean talking = getBooleanValue(dbRow, "talking");
			final String status  = (String) dbRow.get("status");
			final Integer born = (Integer) dbRow.get("born");
			final int count = (Integer) dbRow.get("count");
			final int id = (Integer) dbRow.get("id");
			final String image = (String) dbRow.get("image");
			final AnimalImpl animal = new AnimalImpl(kind, talking, status,
				id, (born == null) ? -1 : born, count);
			if (image != null) {
				animal.setImage(image);
			}
			final Integer row = (Integer) dbRow.get("row");
			final Integer column = (Integer) dbRow.get("column");
			final Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), animal);
			} else {
				multimapPut(containees, parentId, animal);
			}
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
			readTracks(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final String kind = (String) dbRow.get("kind");
			final String image = (String) dbRow.get("image");
			final AnimalTracks track = new AnimalTracks(kind);
			if (image != null) {
				track.setImage(image);
			}
			map.addFixture(new Point(row, column), track);
		};
	}

	private static final Query SELECT_ANIMALS = Query.of("SELECT * FROM animals");
	private static final Query SELECT_TRACKS = Query.of("SELECT * FROM tracks");
	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "animal populations", readAnimal(map, containees),
			SELECT_ANIMALS);
		handleQueryResults(db, warner, "animal tracks", readTracks(map),
			SELECT_TRACKS);
	}
}
