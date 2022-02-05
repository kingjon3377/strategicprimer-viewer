package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.sql.Types;

import common.map.Point;
import common.map.IMutableMapNG;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.AnimalOrTracks;
import common.xmlio.Warning;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;

final class DBAnimalHandler extends AbstractDatabaseWriter<AnimalOrTracks, /*Point|IUnit*/Object>
		implements MapContentsReader {
	public DBAnimalHandler() {
		super(AnimalOrTracks.class, Object.class);
	}

	@Override
	public boolean canWrite(final Object obj, final Object context) {
		return (obj instanceof Animal || obj instanceof AnimalTracks) &&
			(context instanceof Point || context instanceof IUnit);
	}


	private static Optional<Integer> born(final Animal animal) {
		Map<String, Integer> model = MaturityModel.getMaturityAges();
		if (model.containsKey(animal.getKind())) {
			int maturityAge = model.get(animal.getKind());
			if (maturityAge <= (DBMapWriter.currentTurn - animal.getBorn())) {
				return Optional.empty();
			}
		}
		return Optional.of(animal.getBorn());
	}

	private static final Iterable<String> INITIALIZERS = Collections.unmodifiableList(
		Arrays.asList("CREATE TABLE IF NOT EXISTS animals (" +
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
				");",
			// We assume that animal tracks can't occur inside a unit or fortress.
				"CREATE TABLE IF NOT EXISTS tracks (" +
				"    row INTEGER NOT NULL," +
				"    column INTEGER NOT NULL," +
				"    kind VARCHAR(32) NOT NULL," +
				"    image VARCHAR(255)" +
				");"));

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_TRACKS = "INSERT INTO tracks (row, column, kind, image) " +
		"VALUES(?, ?, ?, ?);";

	private static final String INSERT_ANIMAL = "INSERT INTO animals (row, column, parent, kind, " +
		"talking, status, born, count, id, image) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final AnimalOrTracks obj, final Object context) {
		if (!((context instanceof Point) || (context instanceof IUnit))) {
			throw new IllegalArgumentException("context must be a point or a unit");
		}
		if (obj instanceof AnimalTracks) {
			if (context instanceof IUnit) {
				throw new IllegalArgumentException("Animal tracks can't occur inside a unit");
			}
			db.update(INSERT_TRACKS, ((Point) context).getRow(), ((Point) context).getColumn(),
				((AnimalTracks) obj).getKind(), ((AnimalTracks) obj).getImage()).execute();
		} else if (obj instanceof Animal) {
			if (context instanceof Point) {
				db.update(INSERT_ANIMAL, ((Point) context).getRow(),
					((Point) context).getColumn(), null, ((Animal) obj).getKind(),
					((Animal) obj).isTalking(), ((Animal) obj).getStatus(),
					born((Animal) obj).orElse(null), ((Animal) obj).getPopulation(),
					obj.getId(), ((Animal) obj).getImage()).execute();
			} else {
				db.update(INSERT_ANIMAL, null, null, ((IUnit) context).getId(),
					((Animal) obj).getKind(), ((Animal) obj).isTalking(),
					((Animal) obj).getStatus(), born((Animal) obj).orElse(null),
					((Animal) obj).getPopulation(), obj.getId(),
					((Animal) obj).getImage()).execute();
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readAnimal(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			String kind = (String) dbRow.get("kind");
			Boolean talking = /* DBMapReader.databaseBoolean(dbRow.get("talking")) */ // FIXME
				(Boolean) dbRow.get("talking"); // This will compile but probably won't work
			String status  = (String) dbRow.get("status");
			Integer born = (Integer) dbRow.get("born");
			int count = (Integer) dbRow.get("count");
			int id = (Integer) dbRow.get("id");
			String image = (String) dbRow.get("image");
			AnimalImpl animal = new AnimalImpl(kind, talking, status,
				(born == null) ? -1 : born, count);
			if (image != null) {
				animal.setImage(image);
			}
			Integer row = (Integer) dbRow.get("row");
			Integer column = (Integer) dbRow.get("column");
			Integer parentId = (Integer) dbRow.get("parent");
			if (row != null && column != null) {
				map.addFixture(new Point(row, column), animal);
			} else {
				((IMutableUnit) findById(map, parentId, warner)).addMember(animal);
			}
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception>
			readTracks(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			String kind = (String) dbRow.get("kind");
			String image = (String) dbRow.get("image");
			AnimalTracks track = new AnimalTracks(kind);
			if (image != null) {
				track.setImage(image);
			}
			map.addFixture(new Point(row, column), track);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "animal populations", readAnimal(map),
				"SELECT * FROM animals WHERE row IS NOT NULL");
			handleQueryResults(db, warner, "animal tracks", readTracks(map),
				"SELECT * FROM tracks");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "animals in units", readAnimal(map),
				"SELECT * FROM animals WHERE parent IS NOT NULL");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
