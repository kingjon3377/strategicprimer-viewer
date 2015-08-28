package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import util.NullCleaner;
import util.Pair;
import util.Warning;

/**
 * A reader to produce Players.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class PlayerReader implements INodeHandler<Player> {
	/**
	 * Parse a player from the XML.
	 *
	 * @param element the start element to read from
	 * @param stream the stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the player produced
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Player parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Player player =
				new Player(XMLHelper.parseInt(getAttribute(element, "number"),
						NullCleaner.assertNotNull(element.getLocation())),
						getAttribute(element, "code_name"));
		return player;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("player"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Player> writes() {
		return Player.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends Player> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("player",
				Pair.of("number", NullCleaner.assertNotNull(Integer
						.toString(obj.getPlayerId()))), Pair.of("code_name",
						obj.getName()));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PlayerReader";
	}
}
