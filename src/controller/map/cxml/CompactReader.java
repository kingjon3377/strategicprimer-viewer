package controller.map.cxml;

import java.io.IOException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * An interface for XML readers that can read multiple related types.
 *
 * @param <T> the common supertype of all types this can return
 * @author Jonathan Lovelace
 *
 */
public interface CompactReader<T> {
	/**
	 * @param element the element being parsed
	 * @param stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use as needed
	 * @param idFactory the ID factory to use as needed
	 * @return the object parsed from XML
	 * @throws SPFormatException on SP format errors
	 */
	T read(StartElement element, IteratorWrapper<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDFactory idFactory)
			throws SPFormatException;

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent the current indentation level.
	 * @throws IOException on I/O problems.
	 */
	void write(Appendable ostream, T obj, int indent) throws IOException;

	/**
	 * @param tag a tag. May be null, to simplify callers.
	 * @return whether we support it. Should return false if null.
	 */
	boolean isSupportedTag(@Nullable final String tag);



}
