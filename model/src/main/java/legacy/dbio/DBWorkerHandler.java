package legacy.dbio;

import legacy.map.IFixture;
import legacy.map.fixtures.Implement;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import common.idreg.DuplicateIDException;
import legacy.map.IMapNG;
import legacy.map.IMutableMapNG;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.worker.WorkerStats;
import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.Skill;
import legacy.map.fixtures.mobile.worker.ISkill;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBWorkerHandler extends AbstractDatabaseWriter<IWorker, IUnit> implements MapContentsReader {
	public DBWorkerHandler() {
		super(IWorker.class, IUnit.class);
	}

	// TODO: Add a getInstance() method, taking the class to write, to MapContentsReader (or add a cache elsewhere) so
	//  we don't have to have multiple instances

	private final DBAnimalHandler animalHandler = new DBAnimalHandler();

	private final DBImplementHandler equipmentHandler = new DBImplementHandler();

	private static final List<Query> INITIALIZERS = List.of(
		Query.of("CREATE TABLE IF NOT EXISTS workers (" +
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
			");"),
		Query.of("CREATE TABLE IF NOT EXISTS worker_job_levels (" +
			"    worker INTEGER NOT NULL," +
			"    job VARCHAR(32) NOT NULL," +
			"    level INTEGER NOT NULL CHECK(level >= 0)" +
			");"),
		Query.of("CREATE TABLE IF NOT EXISTS worker_skill_levels (" +
			"    worker INTEGER NOT NULL," +
			"    associated_job VARCHAR(32) NOT NULL," +
			"    skill VARCHAR(32) NOT NULL," +
			"    level INTEGER NOT NULL check(level >= 0)," +
			"    hours INTEGER NOT NULL check(hours >= 0)" +
			");"));
	// FIXME: Also pull in Animal initializers

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query WORKER_SQL =
		Query.of("INSERT INTO workers (unit, id, name, race, image, portrait, hp, max_hp, " +
			"    str, dex, con, int, wis, cha) " +
			"VALUES(:unit, :id, :name, :race, :image, :portrait, :hp, :max_hp, :str, :dex, :con, :int, :wis, :cha);");

	private static final Query JOB_SQL =
		Query.of("INSERT INTO worker_job_levels (worker, job, level) VALUES(:worker, :job, :level);");

	private static final Query SKILL_SQL =
		Query.of("INSERT INTO worker_skill_levels (worker, associated_job, skill, level, hours) " +
			"VALUES(:worker, :job, :skill, :level, :hours);");

	// FIXME: Need to write notes, unless that's handled centrally
	@Override
	public void write(final Transactional db, final IWorker obj, final IUnit context) throws SQLException {
		db.transaction().accept(sql -> {
			final String portrait = obj.getPortrait();
			final WorkerStats stats = obj.getStats();
			if (stats == null) {
				WORKER_SQL.on(value("unit", context.getId()), value("id", obj.getId()),
					value("name", obj.getName()), value("race", obj.getRace()),
					value("image", obj.getImage()), value("portrait", portrait)).execute(sql);
			} else {
				WORKER_SQL.on(value("unit", context.getId()), value("id", obj.getId()),
					value("name", obj.getName()), value("race", obj.getRace()),
					value("image", obj.getImage()), value("portrait", portrait),
					value("hp", stats.getHitPoints()), value("max_hp", stats.getMaxHitPoints()),
					value("str", stats.getStrength()), value("dex", stats.getDexterity()),
					value("con", stats.getConstitution()), value("int", stats.getIntelligence()),
					value("wis", stats.getWisdom()), value("cha", stats.getCharisma())).execute(sql);
			}
			for (final IJob job : obj) {
				JOB_SQL.on(value("worker", obj.getId()), value("job", job.getName()),
					value("level", job.getLevel())).execute(sql);
				for (final ISkill skill : job) {
					SKILL_SQL.on(value("worker", obj.getId()), value("job", job.getName()),
						value("skill", skill.getName()), value("level", skill.getLevel()),
						value("hours", skill.getHours())).execute(sql);
				}
			}
			if (obj.getMount() != null) {
				animalHandler.initialize(db);
				animalHandler.write(db, obj.getMount(), obj);
			}
			for (final Implement item : obj.getEquipment()) {
				equipmentHandler.initialize(db);
				equipmentHandler.write(db, item, obj);
			}
		});
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readWorkerStats(final IMutableMapNG map,
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

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
	readJobLevel(final IMutableMapNG map, final Map<Integer, Worker> workers) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("worker");
			final Worker worker = workers.get(id);
			final String job = (String) dbRow.get("job");
			final int level = (Integer) dbRow.get("level");
			worker.addJob(new Job(job, level));
		};
	}

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
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

	private static TryBiConsumer<Map<String, Object>, Warning, SQLException>
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

	private static final Query WORKER_SELECT = Query.of("SELECT * FROM workers");
	private static final Query JOB_SELECT = Query.of("SELECT * FROM worker_job_levels");
	private static final Query SKILL_SELECT = Query.of("SELECT * FROM worker_skill_levels");
	private static final Query NOTE_SELECT = Query.of("SELECT * FROM notes");

	@Override
	public void readMapContents(final Connection db, final IMutableMapNG map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		final Map<Integer, Worker> workers = new HashMap<>();
		handleQueryResults(db, warner, "worker stats", readWorkerStats(map, workers, containees), WORKER_SELECT);
		handleQueryResults(db, warner, "Job levels", readJobLevel(map, workers), JOB_SELECT);
		handleQueryResults(db, warner, "Skill levels", readSkillLevel(map, workers), SKILL_SELECT);
		handleQueryResults(db, warner, "Worker notes", readWorkerNotes(map, workers), NOTE_SELECT);
		containers.putAll(workers);
	}
}
