package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import controller.map.SPFormatException;

/**
 * An interface for *stateless* per-class XML readers. 
 * @author Jonathan Lovelace
 * @param <T> The type of object the reader knows how to read
 *
 */
public interface INodeReader<T> {
	/**
	 * @return the data type that the reader will produce.
	 */
	Class<T> represents();
	/**
	 * Parse an instance of the type from XML.
	 * @param element the eleent to start parsing with
	 * @param stream to get more elements from
	 * @param players the collection of players
	 * @return the produced type
	 * @throws SPFormatException on map format problems
	 */
	T parse(StartElement element, Iterable<XMLEvent> stream,
			PlayerCollection players) throws SPFormatException;
}
