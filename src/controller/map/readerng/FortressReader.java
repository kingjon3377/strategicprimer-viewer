package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import model.map.fixtures.Unit;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
/**
 * A reader for fortresses.
 * @author Jonathan Lovelace
 */
public class FortressReader implements INodeHandler<Fortress> {
	/**
	 * Parse a fortress. 
	 * @param element the element to start with
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttribute(element,
						"owner", "-1"))), getAttribute(element,
						"name", ""),
				getOrGenerateID(element, warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				fort.addUnit(new UnitReader().parse(event.asStartElement(), //NOPMD
						stream, players, warner, idFactory));
			} else if (event.isEndElement() && "fortress".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(
						"fortress", event
								.asStartElement().getName().getLocalPart(),
						event.getLocation().getLineNumber());
			}
		}
		return fort;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("fortress");
	}
	
	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Fortress> writes() {
		return Fortress.class;
	}
	/**
	 * Write an instance of the type to a Writer.
	 * 
	 * @param <S> the actual type of the object to write
	 * @param obj
	 *            the object to write
	 * @param writer
	 *            the Writer we're currently writing to
	 * @param inclusion
	 *            whether to create 'include' tags and separate files for
	 *            elements whose 'file' is different from that of their parents
	 * @throws IOException
	 *             on I/O error while writing
	 */
	@Override
	public <S extends Fortress> void write(final S obj, final Writer writer,
			final boolean inclusion) throws IOException {
		writer.write("<fortress owner=\"");
		writer.write(obj.getOwner().getId());
		if (!obj.getName().isEmpty()) {
			writer.write("\" name=\"");
			writer.write(obj.getName());
		}
		writer.write("\" id=\"");
		writer.write(Long.toString(obj.getID()));
		writer.write("\">");
		if (!obj.getUnits().isEmpty()) {
			writer.write('\n');
			final ReaderAdapter adapter = new ReaderAdapter();
			for (Unit unit : obj.getUnits()) {
				writer.write("\t\t\t\t");
				if (!inclusion || unit.getFile().equals(obj.getFile())) {
					adapter.write(unit, writer, inclusion);
				} else {
					writer.write("<include file=\"");
					writer.write(adapter.writeForInclusion(unit));
					writer.write("\" />\n");
				}
			}
			writer.write("\t\t\t");
		}
		writer.write("</fortress>");
	}

}
