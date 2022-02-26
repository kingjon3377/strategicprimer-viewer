package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IFixture;
import common.map.fixtures.Implement;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import common.idreg.DuplicateIDException;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.mobile.worker.ISkill;
import common.xmlio.Warning;

final class DBWorkerHandler extends AbstractDatabaseWriter<IWorker, IUnit> implements MapContentsReader {
	public DBWorkerHandler() {
		super(IWorker.class, IUnit.class);
	}

	// TODO: Add a getInstance() method, taking the class to write, to MapContentsReader (or add a cache elsewhere) so
	//  we don't have to have multiple instances

	private final DBAnimalHandler animalHandler = new DBAnimalHandler();

	private final DBImplementHandler equipmentHandler = new DBImplementHandler();

	private static final List<String> INITIALIZERS = List.of("CREATE TABLE IF NOT EXISTS workers (" +
			                                                         "   unit INTEGER NOT NULL," +
			                                                         "   id INTEGER NOT NULL," +
			                                                         "   name VARCHAR(64) NOT NULL," +
			                                                         "   race VARCHAR(32) NOT NULL," +
			                                                         "   image VARCHAR(255)," +
			                                                         "   portrait VARCHAR(255)," +
			                                                         "   hp INTEGER," +
			                                                         "   max_hp INTEGER CHECK((hp IS NULL AND max_hp IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND max_hp IS NOT NULL))," +
			                                                         "   str INTEGER CHECK((hp IS NULL AND str IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND str IS NOT NULL))," +
			                                                         "   dex INTEGER CHECK((hp IS NULL AND dex IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND dex IS NOT NULL))," +
			                                                         "   con INTEGER CHECK((hp IS NULL AND con IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND con IS NOT NULL))," +
			                                                         "   int INTEGER CHECK((hp IS NULL AND int IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND int IS NOT NULL))," +
			                                                         "   wis INTEGER CHECK((hp IS NULL AND wis IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND wis IS NOT NULL))," +
			                                                         "   cha INTEGER CHECK((hp IS NULL AND cha IS NULL) OR" +
			                                                         "       (hp IS NOT NULL AND cha IS NOT NULL))" +
			                                                         ");", "CREATE TABLE IF NOT EXISTS worker_job_levels (" +
					                                                               "    worker INTEGER NOT NULL," +
					                                                               "    job VARCHAR(32) NOT NULL," +
					                                                               "    level INTEGER NOT NULL CHECK(level >= 0)" +
					                                                               ");", "CREATE TABLE IF NOT EXISTS worker_skill_levels (" +
							                                                                     "    worker INTEGER NOT NULL," +
							                                                                     "    associated_job VARCHAR(32) NOT NULL," +
							                                                                     "    skill VARCHAR(32) NOT NULL," +
							                                                                     "    level INTEGER NOT NULL check(level >= 0)," +
							                                                                     "    hours INTEGER NOT NULL check(hours >= 0)" +
							                                                                     ");");
	// FIXME: Also pull in Animal initializers

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String WORKER_SQL =
		"INSERT INTO workers (unit, id, name, race, image, portrait, hp, max_hp, " +
			"    str, dex, con, int, wis, cha) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final String JOB_SQL =
		"INSERT INTO worker_job_levels (worker, job, level) VALUES(?, ?, ?);";

	private static final String SKILL_SQL =
		"INSERT INTO worker_skill_levels (worker, associated_job, skill, level, hours) " +
			"VALUES(?, ?, ?, ?, ?);";

	@Override
	public void write(final DB db, final IWorker obj, final IUnit context) {
		db.transaction(sql -> {
			final String portrait = obj.getPortrait();
			final WorkerStats stats = obj.getStats();
			if (stats == null) {
				sql.update(WORKER_SQL, context.getId(), obj.getId(), obj.getName(),
						obj.getRace(), obj.getImage(), portrait, null, null, null,
						null, null, null, null, null).execute();
			} else {
				sql.update(WORKER_SQL, context.getId(), obj.getId(), obj.getName(),
						obj.getRace(), obj.getImage(), portrait, stats.getHitPoints(),
						stats.getMaxHitPoints(), stats.getStrength(),
						stats.getDexterity(), stats.getConstitution(),
						stats.getIntelligence(), stats.getWisdom(),
						stats.getCharisma()).execute();
			}
				for (final IJob job : obj) {
					sql.update(JOB_SQL, obj.getId(), job.getName(), job.getLevel())
						.execute();
					for (final ISkill skill : job) {
						sql.update(SKILL_SQL, obj.getId(),
							job.getName(), skill.getName(), skill.getLevel(),
							skill.getHours()).execute();
					}
				}
				if (obj.getMount() != null) {
					animalHandler.write(sql, obj.getMount(), obj);
				}
				for (final Implement item : obj.getEquipment()) {
					equipmentHandler.write(sql, item, obj);
				}
				return true;
			});
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readWorkerStats(final IMutableMapNG map,
			final Map<Integer, Worker> workers, final Map<Integer, List<Object>> containees) {
		return (dbRow, warner) -> {
			final int unitId = (Integer) dbRow.get("unit");
			final int id = (Integer) dbRow.get("id");
			final String name = (String) dbRow.get("name");
			final String race = (String) dbRow.get("race");
			final String image = (String) dbRow.get("image");
			final String portrait = (String) dbRow.get("portrait");
			final Integer hp = (Integer) dbRow.get("hp");
			final Integer maxHp = (Integer) dbRow.get("max_hp");
			final Integer str = (Integer) dbRow.get("str");
			final Integer dex = (Integer) dbRow.get("dex");
			final Integer con = (Integer) dbRow.get("con");
			final Integer intel = (Integer) dbRow.get("int");
			final Integer wis = (Integer) dbRow.get("wis");
			final Integer cha = (Integer) dbRow.get("cha");
			final Worker worker = new Worker(name, race, id);
			if (hp != null) {
				worker.setStats(new WorkerStats(hp, maxHp, str, dex, con, intel, wis, cha));
			}
			if (image != null) {
				worker.setImage(image);
			}
			if (portrait != null) {
				worker.setPortrait(portrait);
			}
			if (workers.containsKey(id)) {
				warner.handle(new DuplicateIDException(id));
			}
			workers.put(id, worker);
			multimapPut(containees, unitId, worker);
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception>
			readJobLevel(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("worker");
			final Worker worker = workers.get(id);
			final String job = (String) dbRow.get("job");
			final int level = (Integer) dbRow.get("level");
			worker.addJob(new Job(job, level));
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception>
			readSkillLevel(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("worker");
			final Worker worker = workers.get(id);
			final String skill = (String) dbRow.get("skill");
			final int level = (Integer) dbRow.get("level");
			final int hours = (Integer) dbRow.get("hours");
			final IMutableJob job = (IMutableJob) worker.getJob((String) dbRow.get("associated_job"));
			job.addSkill(new Skill(skill, level, hours));
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, Exception>
			readWorkerNotes(final IMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("fixture");
			final Worker worker = workers.get(id);
			final int player = (Integer) dbRow.get("player");
			final String note = (String) dbRow.get("note");
			if (worker != null) {
				worker.setNote(map.getPlayers().getPlayer(player), note);
			}
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
			final Map<Integer, List<Object>> containees, final Warning warner) {
		final Map<Integer, Worker> workers = new HashMap<>();
		try {
			handleQueryResults(db, warner, "worker stats", readWorkerStats(map, workers, containees),
				"SELECT * FROM workers");
			handleQueryResults(db, warner, "Job levels", readJobLevel(map, workers),
				"SELECT * FROM worker_job_levels");
			handleQueryResults(db, warner, "Skill levels", readSkillLevel(map, workers),
				"SELECT * FROM worker_skill_levels");
			handleQueryResults(db, warner, "Worker notes", readWorkerNotes(map, workers),
				"SELECT * FROM notes");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
		containers.putAll(workers);
	}
}
