package controller.map.report.tabular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PatientMap;

/**
 * An interface for tabular-report generators. It's expected that implementers will take
 * the current player and the location of his HQ as constructor parameters.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of thing an implementer can report on
 * @author Jonathan Lovelace
 */
public interface ITableGenerator<@NonNull T> {
	/**
	 * A pattern to match quotes in input.
	 */
	Pattern QUOTE_PATTERN = Pattern.compile("\"");

	/**
	 * Produce a tabular report on a particular category of fixtures in the map. All
	 * fixtures covered in this table should be removed from the set before returning.
	 * @param ostream the stream to write the table to
	 * @param type the type of object being looked for
	 * @param fixtures the set of fixtures
	 * @throws IOException on I/O error writing to the stream
	 */
	@SuppressWarnings("QuestionableName")
	default void produce(final Appendable ostream, final Class<T> type,
						 final PatientMap<Integer, Pair<Point, IFixture>> fixtures)
			throws IOException {
		final List<Pair<Integer, Pair<Point, T>>> values =
				new ArrayList<>(fixtures.entrySet().stream()
										.filter(entry -> applies(entry.getValue()
																		 .second()) &&
																 type.isInstance(
																		 entry.getValue()
																				 .second()))
										.map(entry -> Pair.of(entry.getKey(),
												Pair.of(entry.getValue().first(),
														type.cast(entry.getValue()
																		  .second()))))
										.collect(Collectors.toList()));
		Collections
				.sort(values, (one, two) -> comparePairs(one.second(), two.second()));
		if (!headerRow().isEmpty()) {
			ostream.append(headerRow());
			ostream.append(getRowDelimiter());
		}
		for (final Pair<Integer, Pair<Point, T>> pair : values) {
			if (produce(ostream, fixtures, pair.second().second(), pair.second().first())) {
				fixtures.remove(pair.first());
			}
		}
		fixtures.coalesce();
	}
	/**
	 * Produce a single line of a tabular report.
	 * @param ostream the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item the item to base the line on
	 * @param loc the location of this item
	 * @return whether to remove this item from the Map
	 * @throws IOException on I/O error writing to the stream
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	boolean produce(Appendable ostream, PatientMap<Integer, Pair<Point, IFixture>> fixtures,
				 T item, Point loc) throws IOException;
	/**
	 * Note that this returns the *square* of the distance; for *comparison* that
	 * suffices, and is far faster than taking umpteen square roots. For *display*
	 * remember to take the square root.
	 *
	 * @param first one point
	 * @param second a second point
	 * @return the square of the distance between them
	 */
	default int distance(final Point first, final Point second) {
		return ((first.col - second.col) * (first.col - second.col)) +
					   ((first.row - second.row) * (first.row - second.row));
	}

	/**
	 * @param first one point
	 * @param second another point
	 * @return the distance between them, formatted for print
	 */
	default String distanceString(final Point first, final Point second) {
		return String.format("%.0f", Double.valueOf(Math.sqrt(distance(first, second))));
	}
	/**
	 * This should not include the newline.
	 * @return the CSV header row to print at the top of the report
	 */
	String headerRow();
	/**
	 * @param one a Pair of one fixture and its location (in the other order)
	 * @param two a Pair of another fixture and its location (in the other order)
	 * @return the result of a comparison between the pairs
	 */
	@SuppressWarnings("QuestionableName")
	int comparePairs(Pair<Point, T> one, Pair<Point, T> two);
	/**
	 * @param currentPlayer the player for whom the report is being produced
	 * @param owner the owner of the current fixture
	 * @return a string describing the owner of the current fixture, either "you",
	 * "independent", or the owner's name.
	 */
	default String getOwnerString(final Player currentPlayer, final Player owner) {
		if (currentPlayer.equals(owner)) {
			return "You";
		} else if (owner.isIndependent()) {
			return "Independent";
		} else {
			return owner.getName();
		}
	}
	/**
	 * @return the character delimiting fields in output files.
	 */
	default char getFieldDelimiter() {
		return ',';
	}
	/**
	 * Write the field delimiter to a stream.
	 * @param ostream the stream to write to
	 * @throws IOException on I/O error writing to the stream
	 */
	default void writeFieldDelimiter(final Appendable ostream) throws IOException {
		ostream.append(getFieldDelimiter());
	}
	/**
	 * @return the character delimiting rows in the output
	 */
	default char getRowDelimiter() {
		return '\n';
	}
	/**
	 * Write a field to a stream, quoting it if necessary.
	 * @param ostream the stream to write to
	 * @param field the field-value to write
	 * @throws IOException on I/O error while writing
	 */
	default void writeField(final Appendable ostream, final String field)
			throws IOException {
		final String quotesQuoted = QUOTE_PATTERN.matcher(field).replaceAll("\"\"");
		if (quotesQuoted.contains(Character.toString('"')) ||
					quotesQuoted.contains(Character.toString(getFieldDelimiter())) ||
					quotesQuoted.contains(Character.toString(getRowDelimiter())) ||
					quotesQuoted.contains(Character.toString(' '))) {
			ostream.append('"');
			ostream.append(quotesQuoted);
			ostream.append('"');
		} else {
			ostream.append(quotesQuoted);
		}
	}
	/**
	 * @param obj an object


	 */
	default boolean applies(final IFixture obj) {
		return true;
	}
}
