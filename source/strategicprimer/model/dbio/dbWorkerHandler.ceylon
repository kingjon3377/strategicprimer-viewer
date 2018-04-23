import ceylon.collection {
	MutableMap,
	HashMap
}
import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.idreg {
	DuplicateIDException
}
import strategicprimer.model.map {
	HasPortrait,
	IMutableMapNG
}
import strategicprimer.model.map.fixtures.mobile {
	IWorker,
	IUnit,
	Worker
}
import strategicprimer.model.map.fixtures.mobile.worker {
	WorkerStats,
	Job,
	Skill
}
import strategicprimer.model.xmlio {
	Warning
}
object dbWorkerHandler extends AbstractDatabaseWriter<IWorker, IUnit>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS workers (
			   unit INTEGER NOT NULL,
			   id INTEGER NOT NULL,
			   name VARCHAR(64) NOT NULL,
			   race VARCHAR(32) NOT NULL,
			   image VARCHAR(255),
			   portrait VARCHAR(255),
			   hp INTEGER,
			   max_hp INTEGER CHECK((hp IS NULL AND max_hp IS NULL) OR (hp IS NOT NULL AND max_hp IS NOT NULL)),
			   str INTEGER CHECK((hp IS NULL AND str IS NULL) OR (hp IS NOT NULL AND str IS NOT NULL)),
			   dex INTEGER CHECK((hp IS NULL AND dex IS NULL) OR (hp IS NOT NULL AND dex IS NOT NULL)),
			   con INTEGER CHECK((hp IS NULL AND con IS NULL) OR (hp IS NOT NULL AND con IS NOT NULL)),
			   int INTEGER CHECK((hp IS NULL AND int IS NULL) OR (hp IS NOT NULL AND int IS NOT NULL)),
			   wis INTEGER CHECK((hp IS NULL AND wis IS NULL) OR (hp IS NOT NULL AND wis IS NOT NULL)),
			   cha INTEGER CHECK((hp IS NULL AND cha IS NULL) OR (hp IS NOT NULL AND cha IS NOT NULL))
		   );""",
		"""CREATE TABLE IF NOT EXISTS worker_job_levels (
			   worker INTEGER NOT NULL,
			   job VARCHAR(32) NOT NULL,
			   level INTEGER NOT NULL CHECK(level >= 0)
		   );""",
		"""CREATE TABLE IF NOT EXISTS worker_skill_levels (
			   worker INTEGER NOT NULL,
			   associated_job VARCHAR(32) NOT NULL,
			   skill VARCHAR(32) NOT NULL,
			   level INTEGER NOT NULL check(level >= 0),
			   hours INTEGER NOT NULL check(hours >= 0)
		   );"""
	];
	shared actual void write(Sql db, IWorker obj, IUnit context) {
		value worker = db.Insert(
			"""INSERT INTO workers (unit, id, name, race, image, portrait, hp, max_hp, str, dex, con, int, wis, cha)
			   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""");
		value jobRow = db.Insert("""INSERT INTO worker_job_levels (worker, job, level) VALUES(?, ?, ?);""");
		value skillRow = db.Insert("""INSERT INTO worker_skill_levels (worker, associated_job, skill, level, hours)
		                              VALUES(?, ?, ?, ?, ?);""");
		db.transaction(() {
			String|SqlNull portrait;
			if (is HasPortrait obj) {
				portrait = obj.portrait;
			} else {
				portrait = SqlNull(Types.varchar);
			}
			if (exists stats = obj.stats) {
				worker.execute(context.id, obj.id, obj.name, obj.race, obj.image, portrait, stats.hitPoints,
					stats.maxHitPoints, stats.strength, stats.dexterity, stats.constitution, stats.intelligence,
					stats.wisdom, stats.charisma);
			} else {
				worker.execute(context.id, obj.id, obj.name, obj.race, obj.image, portrait,
					*Singleton(SqlNull(Types.integer)).cycled.take(8));
			}
			for (job in obj) {
				jobRow.execute(obj.id, job.name, job.level);
				for (skill in job) {
					skillRow.execute(obj.id, job.name, skill.name, skill.level, skill.hours);
				}
			}
			return true;
		});
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {}
	shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
		log.trace("About to read worker stats");
		variable Integer count = 0;
		MutableMap<Integer, Worker> workers = HashMap<Integer, Worker>();
		for (row in db.Select("""SELECT * FROM workers""").Results()) {
			assert (is Integer unitId = row["unit"], is IUnit unit = super.findById(map, unitId, warner),
				is Integer id = row["id"], is String name = row["name"], is String race = row["race"],
				is String|SqlNull image = row["image"], is String|SqlNull portrait = row["portrait"],
				is Integer|SqlNull hp = row["hp"], is Integer|SqlNull maxHp = row["max_hp"],
				is Integer|SqlNull str = row["str"], is Integer|SqlNull dex = row["dex"],
				is Integer|SqlNull con = row["con"], is Integer|SqlNull int = row["int"],
				is Integer|SqlNull wis = row["wis"], is Integer|SqlNull cha = row["cha"]);
			Worker worker = Worker(name, race, id);
			if (is Integer hp) {
				assert (is Integer maxHp, is Integer str, is Integer dex, is Integer con, is Integer int,
					is Integer wis, is Integer cha);
				worker.stats = WorkerStats(hp, maxHp, str, dex, con, int, wis, cha);
			}
			if (is String image) {
				worker.image = image;
			}
			if (is String portrait) {
				worker.portrait = portrait;
			}
			if (exists existing = workers[id]) {
				warner.handle(DuplicateIDException(id));
			}
			workers[id] = worker;
			unit.addMember(worker);
			count++;
			if ((count % 50) == 0) {
				log.trace("Read ``count`` workers' stats");
			}
		}
		log.trace("Finished reading workers' stats, about to start Job levels");
		count = 0;
		for (row in db.Select("""SELECT * FROM worker_job_levels""").Results()) {
			assert (is Integer id = row["worker"], exists worker = workers[id], is String job = row["job"],
				is Integer level = row["level"]);
			worker.addJob(Job(job, level));
			count++;
			if ((count % 50) == 0) {
				log.trace("Read ``count`` Job levels");
			}
		}
		log.trace("Finished reading Job levels, about to start Skill levels");
		count = 0;
		for (row in db.Select("""SELECT * FROM worker_skill_levels""").Results()) {
			assert (is Integer id = row["worker"], exists worker = workers[id], is String job = row["associated_job"],
				is String skill = row["skill"], is Integer level = row["level"], is Integer hours = row["hours"]);
			worker.getJob(job).addSkill(Skill(skill, level, hours));
			count++;
			if ((count % 50) == 0) {
				log.trace("Read ``count`` Skill levels");
			}
		}
		log.trace("Finished reading Skill levels, and everything having to do with workers");
	}
}