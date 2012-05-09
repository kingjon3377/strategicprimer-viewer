package controller.map.readerng;

import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * An interface for *stateless* per-class XML readers. 
 * @author Jonathan Lovelace
 * @param <T> The type of object the reader knows how to read
 *
 */
public interface INodeReader<T> {
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
}
