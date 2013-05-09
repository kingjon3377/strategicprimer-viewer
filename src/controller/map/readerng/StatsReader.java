package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for workers' stats.
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class StatsReader implements INodeHandler<WorkerStats> {
	/**
	 * @return the class this knows how to write.
	 */
	@Override
	public Class<WorkerStats> writes() {
		return WorkerStats.class;
	}
	/**
	 * @return the list of tags this knows how to read.
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("stats");
	}
	/**
	 * Parse stats from XML.
	 * @param element the current tag
	 * @param stream the stream to read more tags from
	 * @param players ignored
	 * @param warner the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed stats object
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public WorkerStats parse(final StartElement element, final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final WorkerStats retval = new WorkerStats(Integer.parseInt(XMLHelper
				.getAttribute(element, "str")), Integer.parseInt(XMLHelper
				.getAttribute(element, "dex")), Integer.parseInt(XMLHelper
				.getAttribute(element, "con")), Integer.parseInt(XMLHelper
				.getAttribute(element, "int")), Integer.parseInt(XMLHelper
				.getAttribute(element, "wis")), Integer.parseInt(XMLHelper
				.getAttribute(element, "cha")));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return retval;
	}
	/**
	 * @param obj a stats object
	 * @return the SPIR representing it
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SPIntermediateRepresentation write(final WorkerStats obj) {
		return new SPIntermediateRepresentation("stats", Pair.of("str",
				Integer.toString(obj.getStrength())), Pair.of("dex",
				Integer.toString(obj.getDexterity())), Pair.of("con",
				Integer.toString(obj.getConstitution())), Pair.of("int",
				Integer.toString(obj.getIntelligence())), Pair.of("wis",
				Integer.toString(obj.getWisdom())), Pair.of("cha",
				Integer.toString(obj.getCharisma())));
	}

}
