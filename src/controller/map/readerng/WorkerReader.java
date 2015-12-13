package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.NonNull;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for Workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class WorkerReader implements INodeHandler<@NonNull Worker> {
	/**
	 * @return the class this knows how to write.
	 */
	@Override
	public Class<Worker> writtenClass() {
		return Worker.class;
	}

	/**
	 * @return the list of tags this knows how to read.
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("worker"));
	}

	/**
	 * Parse a worker from XML.
	 *
	 * @param element the current tag
	 * @param stream the stream to read more tags from
	 * @param players ignored
	 * @param warner the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed Worker
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Worker parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final Worker retval = new Worker(
				XMLHelper.getAttribute(element, "name"),
				XMLHelper.getAttribute(element, "race", "human"),
				XMLHelper.getOrGenerateID(element, warner, idFactory));
		XMLHelper.addImage(element, retval);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final Object result =
						ReaderAdapter.ADAPTER.parse(NullCleaner
								.assertNotNull(event.asStartElement()), stream,
								players, warner, idFactory);
				if (result instanceof Job) {
					retval.addJob((Job) result);
				} else if (result instanceof WorkerStats) {
					retval.setStats((WorkerStats) result);
				} else {
					final String outerName = NullCleaner.valueOrDefault(element
							.getName().getLocalPart(), "a null tag");
					final String innerName =
							NullCleaner.valueOrDefault(NullCleaner
									.assertNotNull(event.asStartElement())
									.getName().getLocalPart(), "a null tag");
					throw new UnwantedChildException(outerName, innerName,
							event.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}

	/**
	 * @param obj a worker
	 * @return the SPIR representing it.
	 */
	@Override
	public SPIntermediateRepresentation write(final Worker obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"worker");
		retval.addAttribute("name", obj.getName());
		if (!"human".equals(obj.getRace())) {
			retval.addAttribute("race", obj.getRace());
		}
		retval.addIdAttribute(obj.getID());
		final WorkerStats stats = obj.getStats();
		if (stats != null) {
			retval.addChild(STATS_READER.write(stats));
		}
		retval.addImageAttribute(obj);
		for (final IJob job : obj) {
			if (job instanceof Job
					&& (job.getLevel() > 0 || job.iterator().hasNext())) {
				retval.addChild(JOB_READER.write((Job) job));
			}
		}
		return retval;
	}
	/**
	 * A reader to write stats.
	 */
	private static final StatsReader STATS_READER = new StatsReader();
	/**
	 * A reader to write jobs.
	 */
	private static final JobReader JOB_READER = new JobReader();
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerReader";
	}
}
