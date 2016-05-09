package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import util.NullCleaner;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.imageXML;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
import static java.lang.Boolean.parseBoolean;
import static util.NullCleaner.assertNotNull;

/**
 * A class to hold XML I/O for mobile fixtures, other than units.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class FluidMobileHandler {
	/**
	 * @param element   the element containing an animal
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the animal
	 * @throws SPFormatException if the data is invalid
	 */
	public static final Animal readAnimal(final StartElement element,
						final Iterable<XMLEvent> stream,
						final IMutablePlayerCollection players,
						final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "animal");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final Animal fix =
				new Animal(getAttribute(element, "kind"), hasAttribute(element,
						"traces"),
								  parseBoolean(getAttribute(element, "talking",
										  "false")),
								  getAttribute(element, "status", "wild"),
								  getOrGenerateID(element, warner, idFactory));
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Parse a centaur.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the centaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static final Centaur readCentaur(final StartElement element,
						 final Iterable<XMLEvent> stream,
						 final IMutablePlayerCollection players,
						 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "centaur");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final Centaur fix = new Centaur(getAttribute(element, "kind"),
											   getOrGenerateID(element, warner,
													   idFactory));
		// TODO: Add addImage() method to XMLHelper.
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Parse a dragon.
	 *
	 * TODO: Investigate whether all fixtures that have only an ID and a kind use the
	 * same constructor convention and so can be handled like ID-only fixtures.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the dragon represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static final Dragon readDragon(final StartElement element,
						final Iterable<XMLEvent> stream,
						final IMutablePlayerCollection players,
						final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "dragon");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final Dragon fix = new Dragon(getAttribute(element, "kind"),
											 getOrGenerateID(element, warner,
													 idFactory));
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Parse a fairy.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the fairy represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static final Fairy readFairy(final StartElement element, final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players, final Warning warner,
					   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "fairy");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final Fairy fix = new Fairy(getAttribute(element, "kind"),
										   getOrGenerateID(element, warner, idFactory));
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Parse a giant.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the giant represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static final Giant readGiant(final StartElement element, final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players, final Warning warner,
					   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "giant");
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Giant fix = new Giant(getAttribute(element, "kind"),
										   getOrGenerateID(element, warner, idFactory));
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Write an Animal to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj the object to write. Must be an instance of Animal.
	 * @param indent the current indentation level.
	 * @throws IOException on I/O error
	 */
	public static final void writeAnimal(final Appendable ostream, final Object obj,
										 final int indent) throws IOException {
		if (!(obj instanceof Animal)) {
			throw new IllegalArgumentException("Can only write Animal");
		}
		final Animal fix = (Animal) obj;
		writeTag(ostream, "animal", indent);
		writeAttribute(ostream, "kind", fix.getKind());
		if (fix.isTraces()) {
			writeAttribute(ostream, "traces", "");
		}
		if (fix.isTalking()) {
			writeBooleanAttribute(ostream, "talking", true);
		}
		if (!"wild".equals(fix.getStatus())) {
			writeAttribute(ostream, "status", fix.getStatus());
		}
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
}
