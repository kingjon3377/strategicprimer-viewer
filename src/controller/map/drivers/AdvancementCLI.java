package controller.map.drivers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import util.Warning;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapHelper;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver for running worker advancement.
 * @author Jonathan Lovelace
 *
 */
public final class AdvancementCLI {
	/**
	 * Do not instantiate. (TODO: Move stuff out of main() and into class methods ...)
	 */
	private AdvancementCLI() {
		// Do nothing yet.
	}
	/**
	 * Driver. See usage statement in first lines of method.
	 *
	 * TODO: Figure out how to, like the ExplorationCLI, apply same operations to all maps at once.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: AdvancementCLI map [seed]");
			System.exit(1);
		}
		final MapHelper helper = new MapHelper();
		final MapReaderAdapter reader = new MapReaderAdapter();
		// ESCA-JAVA0177:
		IMap master;
		try {
			master = reader.readMap(args[0], Warning.INSTANCE);
		} catch (IOException except) {
			System.err.println("I/O error reading maps:");
			System.err.println(except.getLocalizedMessage());
			System.exit(1);
			return; // NOPMD
		} catch (XMLStreamException except) {
			System.err.println("Malformed XML in map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(2);
			return; // NOPMD
		} catch (SPFormatException except) {
			System.err.println("SP format error in map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(3);
			return; // NOPMD
		}
		long seed;
		if (args.length >= 2) {
			seed = Long.parseLong(args[1]);
		} else {
			seed = System.currentTimeMillis();
		}
		final Random random = new Random(seed);
		final List<Player> players = helper.getPlayerChoices(Collections.singletonList(master));
		try {
			final int playerNum = helper.chooseFromList(players,
					"The players:",
					"No players in the map.",
					"Please make a selection: ", true);
			if (playerNum < 0) {
				return; // NOPMD
			}
			final Player player = players.get(playerNum);
			final List<Unit> units = helper.getUnits(master, player);
			for (final Unit unit : units) {
				for (UnitMember member : unit) {
					if (member instanceof Worker) {
						advance((Worker) member, helper, random);
					}
				}
			}
		} catch (IOException except) {
			System.exit(4);
			return; // NOPMD
		}
		try {
			reader.write(args[0], master);
		} catch (IOException except) {
			System.err.println("I/O error writing to a map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(5);
		}
		SystemOut.SYS_OUT
				.print("If running the same advancement on a submap, the following seed should (?) give the same results:");
		SystemOut.SYS_OUT.println(seed);
	}
	/**
	 * Let the player state a worker's training or experience increase.
	 * @param worker the worker
	 * @param helper the helper to use to ask the user to select a job.
	 * @param random the Random instance to use to determine whether to level up.
	 */
	private static void advance(final Worker worker, final MapHelper helper, final Random random) {
		SystemOut.SYS_OUT.print("Running advancement for worker ");
		SystemOut.SYS_OUT.println(worker.getName());
		SystemOut.SYS_OUT.println("Specify one more than the last number to add a new Job.");
		final List<Job> jobs = helper.toList(worker);
		int jobNum = 0;
		while (jobNum >= 0) {
			try {
				jobNum = helper.chooseFromList(jobs,
						"Jobs the worker can work as:", "Worker has no jobs.",
						"Please make a selection: ", false);
				if (jobNum == jobs.size()) {
					final Job job = addJob(helper);
					worker.addJob(job);
					jobs.add(job);
				} else if (jobNum > jobs.size()) {
					break;
				}
			} catch (IOException except) {
				break;
			}
			advance(jobs.get(jobNum), helper, random);
		}
	}
	/**
	 * Let the palyer state a worker's training or experience increase in a single Job.
	 * @param job the job we're looking at
	 * @param helper the helper to use to ask the worker to select a skill and for how many hours to increase
	 * @param random the Random instance to use to determine whether to level up.
	 */
	private static void advance(final Job job, final MapHelper helper, final Random random) {
		final List<Skill> skills = helper.toList(job);
		int skillNum = 0;
		if (skills.isEmpty()) {
			SystemOut.SYS_OUT.println("No skills in list, please add one before continuing.");
			Skill skill;
			try {
				skill = addSkill(helper);
			} catch (IOException except) {
				System.err.println("I/O error ...");
				return; // NOPMD
			}
			job.addSkill(skill);
			skills.add(skill);
		}
		SystemOut.SYS_OUT.println("Specify one more than the last number to add a new Skill.");
		while (skillNum >= 0) {
			try {
				skillNum = helper.chooseFromList(skills, "Skills in the Job:",
						"No skills in the Job.", "Please make a selection: ", false);
				if (skillNum == skills.size()) {
					final Skill skill = addSkill(helper);
					job.addSkill(skill);
					skills.add(skill);
				} else if (skillNum > skills.size()) {
					break;
				}
				advance(skills.get(skillNum), helper, random);
			} catch (IOException except) {
				break;
			}
		}
	}
	/**
	 * Let the player advance a worker's training in a particular skill.
	 * @param skill the skill in question
	 * @param helper the helper to use to get the player's input
	 * @param random the Random instance to use to determine whether to level up.
	 * @throws IOException on I/O error reading the number of hours
	 */
	private static void advance(final Skill skill, final MapHelper helper, final Random random) throws IOException {
		final int hours = helper.inputNumber("Advance how many hours? ");
		final int threshold = random.nextInt(100);
		skill.addHours(hours, threshold);
		if (skill.getHours() == 0) {
			SystemOut.SYS_OUT.print("Worker gained a level in skill ");
			SystemOut.SYS_OUT.println(skill.getName());
		}
	}
	/**
	 * Let the player create a new Job.
	 * @param helper the helper to use to get the player's input
	 * @return the new Job.
	 * @throws IOException on I/O error
	 */
	private static Job addJob(final MapHelper helper) throws IOException {
		final String name = helper.inputString("Name of the job: ");
		final int levels = helper.inputNumber("Levels in the job: ");
		return new Job(name, levels);
	}
	/**
	 * Let the player create a new Skill. We assume this is at level 0 with 0
	 * hours, since both can easily be corrected immediately.
	 * @param helper the helper to use to get the player's input
	 * @return the new Skill
	 * @throws IOException on I/O error
	 */
	private static Skill addSkill(final MapHelper helper) throws IOException {
		final String name = helper.inputString("Name of the skill: ");
		return new Skill(name, 0, 0);
	}
}
