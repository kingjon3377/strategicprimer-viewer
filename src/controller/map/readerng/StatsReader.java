package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for workers' stats.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class StatsReader implements INodeHandler<@NonNull WorkerStats> {
	/**
	 * @return the class this knows how to write.
	 */
	@Override
	public Class<WorkerStats> writtenClass() {
		return WorkerStats.class;
	}

	/**
	 * @return the list of tags this knows how to read.
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("stats"));
	}

	/**
	 * Parse stats from XML.
	 *
	 * @param element   the current tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed stats object
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public WorkerStats parse(final StartElement element,
							 final Iterable<XMLEvent> stream,
							 final IMutablePlayerCollection players,
							 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final Location loc = NullCleaner.assertNotNull(element.getLocation());
		final WorkerStats retval = new WorkerStats(XMLHelper.parseInt(XMLHelper
																			  .getAttribute(
																					  element,
																					  "hp"),
				loc), XMLHelper.parseInt(XMLHelper
												 .getAttribute(element, "max"), loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "str"),
																  loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "dex"),
																  loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "con"),
																  loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "int"),
																  loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "wis"),
																  loc),
														  XMLHelper.parseInt(XMLHelper
																					 .getAttribute(
																							 element,
																							 "cha"),
																  loc));
		XMLHelper.spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * @param obj a stats object
	 * @return the SPIR representing it
	 */
	@Override
	public SPIntermediateRepresentation write(final WorkerStats obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("stats");
		retval.addIntegerAttribute("hp", obj.getHitPoints());
		retval.addIntegerAttribute("max", obj.getMaxHitPoints());
		retval.addIntegerAttribute("str", obj.getStrength());
		retval.addIntegerAttribute("dex", obj.getDexterity());
		retval.addIntegerAttribute("con", obj.getConstitution());
		retval.addIntegerAttribute("int", obj.getIntelligence());
		retval.addIntegerAttribute("wis", obj.getWisdom());
		retval.addIntegerAttribute("cha", obj.getCharisma());
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "StatsReader";
	}
}
