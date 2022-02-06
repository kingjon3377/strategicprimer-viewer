package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import common.idreg.DuplicateIDException;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.fixtures.mobile.IMutableUnit;
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

	private static final List<String> INITIALIZERS = Collections.unmodifiableList(Arrays.asList(
		"CREATE TABLE IF NOT EXISTS workers (" +
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
			");",
		"CREATE TABLE IF NOT EXISTS worker_job_levels (" +
			"    worker INTEGER NOT NULL," +
			"    job VARCHAR(32) NOT NULL," +
			"    level INTEGER NOT NULL CHECK(level >= 0)" +
			");",
		"CREATE TABLE IF NOT EXISTS worker_skill_levels (" +
			"    worker INTEGER NOT NULL," +
			"    associated_job VARCHAR(32) NOT NULL," +
			"    skill VARCHAR(32) NOT NULL," +
			"    level INTEGER NOT NULL check(level >= 0)," +
			"    hours INTEGER NOT NULL check(hours >= 0)" +
			");"));

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
			String portrait = obj.getPortrait();
			WorkerStats stats = obj.getStats();
			if (stats != null) {
				sql.update(WORKER_SQL, context.getId(), obj.getId(), obj.getName(),
					obj.getRace(), obj.getImage(), portrait, stats.getHitPoints(),
					stats.getMaxHitPoints(), stats.getStrength(),
					stats.getDexterity(), stats.getConstitution(),
					stats.getIntelligence(), stats.getWisdom(),
					stats.getCharisma()).execute();
				} else {
					sql.update(WORKER_SQL, context.getId(), obj.getId(), obj.getName(),
						obj.getRace(), obj.getImage(), portrait, null, null, null,
						null, null, null, null, null).execute();
				}
				for (IJob job : obj) {
					sql.update(JOB_SQL, obj.getId(), job.getName(), job.getLevel())
						.execute();
					for (ISkill skill : job) {
						sql.update(SKILL_SQL, obj.getId(),
							job.getName(), skill.getName(), skill.getLevel(),
							skill.getHours()).execute();
					}
				}
				return true;
			});
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readWorkerStats(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			IMutableUnit unit = (IMutableUnit) findById(map, (Integer) dbRow.get("unit"),
				warner);
			int id = (Integer) dbRow.get("id");
			String name = (String) dbRow.get("name");
			String race = (String) dbRow.get("race");
			String image = (String) dbRow.get("image");
			String portrait = (String) dbRow.get("portrait");
			Integer hp = (Integer) dbRow.get("hp");
			Integer maxHp = (Integer) dbRow.get("max_hp");
			Integer str = (Integer) dbRow.get("str");
			Integer dex = (Integer) dbRow.get("dex");
			Integer con = (Integer) dbRow.get("con");
			Integer intel = (Integer) dbRow.get("int");
			Integer wis = (Integer) dbRow.get("wis");
			Integer cha = (Integer) dbRow.get("cha");
			Worker worker = new Worker(name, race, id);
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
			unit.addMember(worker);
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readJobLevel(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			int id = (Integer) dbRow.get("worker");
			Worker worker = workers.get(id);
			String job = (String) dbRow.get("job");
			int level = (Integer) dbRow.get("level");
			worker.addJob(new Job(job, level));
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readSkillLevel(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			int id = (Integer) dbRow.get("worker");
			Worker worker = workers.get(id);
			String skill = (String) dbRow.get("skill");
			int level = (Integer) dbRow.get("level");
			int hours = (Integer) dbRow.get("hours");
			IMutableJob job = (IMutableJob) worker.getJob((String) dbRow.get("associated_job"));
			job.addSkill(new Skill(skill, level, hours));
		};
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception>
			readWorkerNotes(final IMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			int id = (Integer) dbRow.get("fixture");
			Worker worker = workers.get(id);
			int player = (Integer) dbRow.get("player");
			String note = (String) dbRow.get("note");
			if (worker != null) {
				worker.setNote(map.getPlayers().getPlayer(player), note);
			}
		};
	}

	@Override
	public void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		Map<Integer, Worker> workers = new HashMap<Integer, Worker>();
		try {
			handleQueryResults(db, warner, "worker stats", readWorkerStats(map, workers),
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
	}
}
