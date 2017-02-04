package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.Player;
import model.map.PlayerImpl;
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
public final class YAPlayerReader extends YAAbstractReader<Player> {
	/**
	 * Constructor.
	 * @param warning the Warning instance to use.
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAPlayerReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}
	/**
	 * Read a Player from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 * @return the object read from XML
	 */
	@Override
	public Player read(final StartElement element,
					   final QName parent,
					   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "player");
		requireNonEmptyParameter(element, "number", true);
		requireNonEmptyParameter(element, "code_name", true);
		spinUntilEnd(element.getName(), stream);
		return new PlayerImpl(getIntegerParameter(element, "number"),
								 getParameter(element, "code_name"));
	}

	/**
	 * We only support the "player" tag.
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
		writeProperty(ostream, "number", obj.getPlayerId());
		writeProperty(ostream, "code_name", obj.getName());
		closeLeafTag(ostream);
	}

	/**
	 * We can only write Players.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Player;
	}
}
