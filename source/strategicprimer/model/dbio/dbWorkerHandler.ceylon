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
			   max_hp INTEGER CHECK((hp IS NULL AND max_hp IS NULL) OR (hp NOT NULL AND max_hp NOT NULL)),
			   str INTEGER CHECK((hp IS NULL AND str IS NULL) OR (hp NOT NULL AND str NOT NULL)),
			   dex INTEGER CHECK((hp IS NULL AND dex IS NULL) OR (hp NOT NULL AND dex NOT NULL)),
			   con INTEGER CHECK((hp IS NULL AND con IS NULL) OR (hp NOT NULL AND con NOT NULL)),
			   int INTEGER CHECK((hp IS NULL AND int IS NULL) OR (hp NOT NULL AND int NOT NULL)),
			   wis INTEGER CHECK((hp IS NULL AND wis IS NULL) OR (hp NOT NULL AND wis NOT NULL)),
			   cha INTEGER CHECK((hp IS NULL AND cha IS NULL) OR (hp NOT NULL AND cha NOT NULL))
		   )""",
		"""CREATE TABLE IF NOT EXISTS worker_job_levels (
			   worker INTEGER NOT NULL,
			   job VARCHAR(32) NOT NULL,
			   level INTEGER NOT NULL CHECK(level >= 0)
		   )""",
		"""CREATE TABLE IF NOT EXISTS worker_skill_levels (
			   worker INTEGER NOT NULL,
			   associated_job VARCHAR(32) NOT NULL,
			   skill VARCHAR(32) NOT NULL,
			   level INTEGER NOT NULL check(level >= 0),
			   hours INTEGER NOT NULL check(hours >= 0)
		   )"""
	];
	shared actual void write(Sql db, IWorker obj, IUnit context) {
		value worker = db.Insert(
			"""INSERT INTO workers (unit, id, name, race, image, portrait, hp, max_hp, str, dex, con, int, wis, cha)
			   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""");
		value jobRow = db.Insert("""INSERT INTO worker_job_levels (worker, job, level) VALUES(?, ?, ?)""");
		value skillRow = db.Insert("""INSERT INTO worker_skill_levels (worker, associated_job, skill, level, hours)
		                              VALUES(?, ?, ?, ?, ?)""");
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
		MutableMap<Integer, Worker> workers = HashMap<Integer, Worker>();
		for (row in db.Select("""SELECT * FROM workers""").Results()) {
			assert (is Integer unitId = row["unit"], is IUnit unit = super.findById(map, unitId, warner),
				is Integer id = row["id"], is String name = row["name"], is String race = row["race"],
				is String? image = row["image"], is String? portrait = row["portrait"],
				is Integer? hp = row["hp"], is Integer? maxHp = row["max_hp"], is Integer? str = row["str"],
				is Integer? dex = row["dex"], is Integer? con = row["con"], is Integer? int = row["int"],
				is Integer? wis = row["wis"], is Integer? cha = row["cha"]);
			Worker worker = Worker(name, race, id);
			if (exists hp) {
				assert (exists maxHp, exists str, exists dex, exists con, exists int, exists wis, exists cha);
				worker.stats = WorkerStats(hp, maxHp, str, dex, con, int, wis, cha);
			}
			if (exists image) {
				worker.image = image;
			}
			if (exists portrait) {
				worker.portrait = portrait;
			}
			if (exists existing = workers[id]) {
				warner.handle(DuplicateIDException(id));
			}
			workers[id] = worker;
		}
		for (row in db.Select("""SELECT * FROM worker_job_levels""").Results()) {
			assert (is Integer id = row["worker"], exists worker = workers[id], is String job = row["job"],
				is Integer level = row["level"]);
			worker.addJob(Job(job, level));
		}
		for (row in db.Select("""SELECT * FROM worke_skill_levels""").Results()) {
			assert (is Integer id = row["worker"], exists worker = workers[id], is String job = row["associated_job"],
				is String skill = row["skill"], is Integer level = row["level"], is Integer hours = row["hours"]);
			worker.getJob(job).addSkill(Skill(skill, level, hours));
		}
	}
}