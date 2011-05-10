package controller.character;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import model.character.AdminStatsImpl;
import model.character.CharStats;
import model.character.JobLevels;
import model.character.PlayerStatsImpl;
import model.character.SPCharacter;

/**
 * Write a character to file.
 * 
 * TODO: write tests for this
 * 
 * @author Jonathan Lovelace.
 * 
 */
public class CharacterWriter {
	/**
	 * Constructor taking a filename.
	 * 
	 * @param filename
	 *            the file to write to
	 * @throws IOException
	 *             on I/O error opening file.
	 */
	public CharacterWriter(final String filename) throws IOException {
		this(new FileWriter(filename)); // $codepro.audit.disable closeWhereCreated
	}

	/**
	 * Constructor taking a writer.
	 * 
	 * @param out
	 *            a stream to write to
	 */
	public CharacterWriter(final Writer out) {
		ostream = new PrintWriter(new BufferedWriter(out)); // $codepro.audit.disable closeWhereCreated
	}

	/**
	 * The stream we'll write to.
	 */
	private final PrintWriter ostream;

	/**
	 * Write a character.
	 * 
	 * @param character
	 *            the character to write.
	 */
	public void write(final SPCharacter character) {
		write(character, false);
	}

	/**
	 * Write a character, maybe for player consumption.
	 * 
	 * @param character
	 *            the character to write.
	 * @param forPlayer
	 *            true if we shouldn't write exact scores.
	 */
	public void write(final SPCharacter character, final boolean forPlayer) {
		writeHeader();
		writeName(character);
		writeStats(character, forPlayer);
		writeJobs(character);
		ostream.close(); // $codepro.audit.disable closeInFinally
	}

	/**
	 * Writes the file header.
	 */
	private void writeHeader() {
		ostream.println("# SP character file format v. 0");
		ostream.println();
	}

	/**
	 * Writes the character's name.
	 * 
	 * @param character
	 *            the character to write.
	 */
	private void writeName(final SPCharacter character) {
		ostream.print("CHARNAME:");
		ostream.println(character.getName());
		ostream.println();
	}

	/**
	 * Writes the character's stats.
	 * 
	 * @param character
	 *            the character to write
	 * @param forPlayer
	 *            true if we shouldn't write exact stats.
	 */
	private void writeStats(final SPCharacter character, final boolean forPlayer) {
		if (forPlayer || character.getStats() instanceof PlayerStatsImpl) {
			writeApproximateStats(character);
		} else {
			writeExactStats(character);
		}
		ostream.println();
	}

	/**
	 * Write the character's stats, approximately.
	 * 
	 * @param character
	 *            the character to write
	 */
	private void writeApproximateStats(final SPCharacter character) {
		for (final CharStats.Stat stat : CharStats.Stat.values()) {
			ostream.print(stat);
			ostream.print(':');
			ostream.println(character.getStats().getStat(stat));
		}
	}

	/**
	 * Write the character's stats, exactly.
	 * 
	 * @param character
	 *            the character to write
	 */
	private void writeExactStats(final SPCharacter character) {
		for (final CharStats.Stat stat : CharStats.Stat.values()) {
			ostream.print(stat);
			ostream.print(':');
			ostream.println(((AdminStatsImpl) character.getStats())
					.getStatValue(stat));
		}
	}

	/**
	 * Write the character's Jobs
	 * 
	 * @param character
	 *            the character to write
	 */
	private void writeJobs(final SPCharacter character) {
		for (final JobLevels job : character.getJobs()) {
			ostream.print("Job:");
			ostream.print(job.getJob());
			ostream.print(':');
			ostream.print(job.getLevels());
		}
	}
}
