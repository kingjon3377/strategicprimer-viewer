package controller.map.cxml;

import java.io.IOException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import model.map.fixtures.explorable.AdventureFixture;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;
/**
 * A reader for adventure hooks.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
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
 *
 */
public class CompactAdventureReader extends
		AbstractCompactReader<AdventureFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactAdventureReader READER = new CompactAdventureReader();
	/**
	 * Read an adventure from XML.
	 * @param element The XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed adventure
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public AdventureFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "adventure");
		Player player = players.getIndependent();
		if (hasParameter(element, "owner")) {
			player =
					players.getPlayer(parseInt(getParameter(element, "owner"),
							element.getLocation().getLineNumber()));
		}
		final AdventureFixture retval =
				new AdventureFixture(player,
						getParameter(element, "brief", ""), getParameter(
								element, "full", ""), getOrGenerateID(element,
								warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return retval;
	}
	/**
	 * Write an adventure to XML.
	 * @param ostream the stream to write to
	 * @param obj the adventure to write
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final AdventureFixture obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		ostream.append("<adventure id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append("\" ");
		if (!obj.getOwner().isIndependent()) {
			ostream.append("owner=\"");
			ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
			ostream.append("\" ");
		}
		if (!obj.getBriefDescription().isEmpty()) {
			ostream.append("brief=\"");
			ostream.append(obj.getBriefDescription());
			ostream.append("\" ");
		}
		if (!obj.getFullDescription().isEmpty()) {
			ostream.append("full=\"");
			ostream.append(obj.getFullDescription());
			ostream.append("\" ");
		}
		ostream.append(imageXML(obj));
		ostream.append(" />\n");
	}
	/**
	 * @param tag a tag
	 * @return whether it is one we support
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "adventure".equalsIgnoreCase(tag);
	}

}
