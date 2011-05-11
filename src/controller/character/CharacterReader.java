package controller.character;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.character.AdminStatsImpl;
import model.character.CharStats;
import model.character.JobLevels;
import model.character.JobType;
import model.character.PlayerStatsImpl;
import model.character.SPCharacter;
import view.util.IsAdmin;

/**
 * A class to read characters from file.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class CharacterReader {
	/**
	 * Constructor.
	 * 
	 * @param inStream
	 *            the stream to read from
	 */
	public CharacterReader(final BufferedReader inStream) {
		istream = inStream;
	}

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the file to read from
	 * @throws FileNotFoundException
	 *             when file not found.
	 */
	public CharacterReader(final String filename) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(filename))); // $codepro.audit.disable closeWhereCreated
	}

	/**
	 * The stream we read from.
	 */
	private BufferedReader istream;

	/**
	 * Only call this method once per Reader.
	 * 
	 * @return the character from the stream or file.
	 * @throws IOException
	 *             on I/O error while reading
	 */
	public SPCharacter getCharacter() throws IOException {
		String line = istream.readLine();
		while (line != null) {
			handleLine(line);
			line = istream.readLine();
		}
		return createCharacter();
	}

	/**
	 * 
	 * @return a new character based on what we've read from the stream.
	 */
	private SPCharacter createCharacter() {
		return new SPCharacter(name, stats);
	}

	/**
	 * Handle a line of input.
	 * 
	 * @param line
	 *            the input to handle
	 */
	private void handleLine(final String line) {
		if (line.length() > 0 && line.charAt(0) != '#') {
			if (line.charAt(0) == ' ' || line.charAt(0) == '\t') {
				handleLine(line.trim());
			} else {
				if (line.contains(":")) {
					handleLine(line.split(":")[0],
							line.substring(line.indexOf(':')));
				}
			}
		}
	}

	/**
	 * The character's name.
	 */
	private String name;
	/**
	 * The character's stats.
	 */
	private CharStats stats;

	/**
	 * Handle a line split into a variable and its value(s).
	 * 
	 * @param var
	 *            the variable
	 * @param rest
	 *            the rest of the line
	 */
	private void handleLine(final String var, final String rest) {
		if (rest.charAt(0) == ':') {
			handleLine(var, rest.substring(1));
		} else if ("charname".equalsIgnoreCase(var)) {
			name = rest;
		} else if ("job".equalsIgnoreCase(var)) {
			handleJob(rest.split(":")[0], rest.substring(rest.indexOf(':')));
		} else if (isStat(var.trim())) {
			if (stats == null) {
				createStats(isNumeric(rest.trim()) && IsAdmin.IS_ADMIN);
			}
			handleStat(var.trim(), rest.trim());
		}
	}

	/**
	 * Is the string a stat?
	 * 
	 * @param str
	 *            a string
	 * @return whether it is a stat we know about,
	 */
	private static boolean isStat(final String str) {
		try {
			CharStats.Stat.valueOf(str);
			return true; // NOPMD
		} catch (final IllegalArgumentException except) {
			return false;
		}
	}

	/**
	 * Is the string numeric?
	 * 
	 * @param str
	 *            a string
	 * @return whether it is numeric.
	 */
	private static boolean isNumeric(final String str) {
		try {
			Integer.parseInt(str);
			return true; // NOPMD
		} catch (final NumberFormatException except) {
			return false;
		}
	}

	/**
	 * Create the stats variable.
	 * 
	 * @param admin
	 *            whether to use the admin implementation.
	 */
	private void createStats(final boolean admin) {
		stats = (admin ? new AdminStatsImpl() : new PlayerStatsImpl());
	}

	/**
	 * Add a stat to stats.
	 * 
	 * Preconditions (not checked here; the first two are checked earlier):
	 * stats is set up properly, stat is actually a stat, value is either an
	 * attribute or numeric.
	 * 
	 * @param stat
	 *            the stat
	 * @param value
	 *            its value, either an Attribute or numeric.
	 * 
	 */
	private void handleStat(final String stat, final String value) {
		try {
			if (stats instanceof AdminStatsImpl) {
				((AdminStatsImpl) stats).setStat(CharStats.Stat.valueOf(stat),
						Integer.parseInt(value));
			} else {
				stats.setStat(CharStats.Stat.valueOf(stat),
						AdminStatsImpl.convertStat(Integer.parseInt(value)));
			}
		} catch (final NumberFormatException except) {
			stats.setStat(CharStats.Stat.valueOf(stat),
					CharStats.Attribute.valueOf(value));
		}
	}

	/**
	 * Handle a Job. FIXME: validate input before calling this.
	 * 
	 * @param job
	 *            the job
	 * @param levels
	 *            how many levels
	 */
	private void handleJob(final String job, final String levels) {
		if (isJob(job) && isNumeric(levels)) {
			jobs.add(new JobLevels(JobType.valueOf(job), Integer
					.parseInt(levels)));
		}
	}

	/**
	 * The jobs the character has levels in.
	 */
	private final List<JobLevels> jobs = new ArrayList<JobLevels>();

	/**
	 * Is the string a Job? FIXME: Should load Job definitions from file rather
	 * than enumerating them.
	 * 
	 * @param str
	 *            a string
	 * @return whether it is a Job we know about.
	 */
	private static boolean isJob(final String str) {
		try {
			JobType.valueOf(str);
			return true; // NOPMD
		} catch (final IllegalArgumentException except) {
			return false;
		}
	}
}
