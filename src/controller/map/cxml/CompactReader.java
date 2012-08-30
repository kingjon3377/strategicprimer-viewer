package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.XMLWritable;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * An interface for XML readers that can read multiple related types.
 * @param <T> the common supertype of all types this can return
 * @author Jonathan Lovelace
 *
 */
public interface CompactReader<T extends XMLWritable> {
	/**
	 * @param <U> the type actually returned
	 * @param element the element being parsed
	 * @param stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use as needed
	 * @param idFactory the ID factory to use as needed
	 * @return the object parsed from XML
	 * @throws SPFormatException on SP format errors
	 */
	<U extends T> U read(StartElement element, IteratorWrapper<XMLEvent> stream,
			PlayerCollection players, Warning warner, IDFactory idFactory) throws SPFormatException;

}
