import ceylon.dbc {
	Sql,
	SqlNull
}

import java.sql {
	Types
}

import strategicprimer.model.map {
	HasPortrait
}
import strategicprimer.model.map.fixtures.mobile {
	IWorker,
	IUnit
}
object dbWorkerWriter satisfies DatabaseWriter<IWorker, IUnit> {
	shared actual void write(Sql db, IWorker obj, IUnit context) {
		db.Statement("""CREATE TABLE IF NOT EXISTS workers (
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
		                )""").execute();
		db.Statement("""CREATE TABLE IF NOT EXISTS worker_job_levels (
			                worker INTEGER NOT NULL,
			                job VARCHAR(32) NOT NULL,
			                level INTEGER NOT NULL CHECK(level >= 0)
		                )""").execute();
		db.Statement("""CREATE TABLE IF NOT EXISTS worker_skill_levels (
			                worker INTEGER NOT NULL,
			                associated_job VARCHAR(32) NOT NULL,
			                skill VARCHAR(32) NOT NULL,
			                level INTEGER NOT NULL check(level >= 0),
			                hours INTEGER NOT NULL check(hours >= 0)
		                )""").execute();
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
}