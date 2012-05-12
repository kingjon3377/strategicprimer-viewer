package controller.map.readerng;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * An interface for *stateless* per-class XML readers/writers. 
 * @author Jonathan Lovelace
 * @param <T> The type of object the reader knows how to read
 *
 */
public interface INodeHandler<T> {
	/**
	 * @return the class this can write to a writer.
	 */
	Class<T> writes();
	/**
	 * @return a list of the tags the reader can handle.
	 */
	List<String> understands();
	/**
	 * Parse an instance of the type from XML.
	 * @param element the eleent to start parsing with
	 * @param stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the produced type
	 * @throws SPFormatException on map format problems
	 */
	T parse(StartElement element, Iterable<XMLEvent> stream,
			PlayerCollection players, Warning warner, IDFactory idFactory) throws SPFormatException;
	
	/**
	 * Write an instance of the type to a Writer.
	 * 
	 * @param <S> the type of the object---it can be a subclass, to make the adapter work.
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
	<S extends T> void write(S obj, Writer writer, boolean inclusion) throws IOException;
}
