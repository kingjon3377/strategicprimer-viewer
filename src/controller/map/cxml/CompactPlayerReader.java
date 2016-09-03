package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import util.LineEnd;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactPlayerReader extends AbstractCompactReader<Player> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<Player> READER = new CompactPlayerReader();
	/**
	 * Singleton.
	 */
	private CompactPlayerReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 *@param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Player read(final StartElement element,
					   final QName parent, final IMutablePlayerCollection players,
					   final Warning warner, final IDRegistrar idFactory,
					   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "player");
		requireNonEmptyParameter(element, "number", true, warner);
		requireNonEmptyParameter(element, "code_name", true, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return new Player(getIntegerParameter(element, "number"),
								getParameter(element, "code_name"));
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "player".equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Player obj, final int indent)
			throws IOException {
		writeTag(ostream, "player", indent);
		ostream.append(" number=\"");
		ostream.append(Integer.toString(obj.getPlayerId()));
		ostream.append("\" code_name=\"");
		ostream.append(obj.getName());
		ostream.append("\" />");
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Player;
	}
}
