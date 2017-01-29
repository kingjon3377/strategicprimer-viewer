package model.map.fixtures.mobile;

import java.util.Formatter;
import java.util.stream.Stream;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class for immortals that don't have any state other than their ID, so we only need
 * one class for all of them.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class SimpleImmortal implements Immortal, HasMutableImage, HasKind {
	/**
	 * The kinds of immortal this class covers.
	 */
	public enum SimpleImmortalKind {
		/**
		 * Sphinx.
		 */
		Sphinx("sphinx", "Sphinxes", 35),
		/**
		 * Djinn.
		 */
		Djinn("djinn", "Djinni", 30),
		/**
		 * Griffin.
		 */
		Griffin("griffin", "Griffins", 28),
		/**
		 * Minotaurs.
		 */
		Minotaur("minotaur", "Minotaurs", 30),
		/**
		 * Ogre.
		 */
		Ogre("ogre", "Ogres", 28),
		/**
		 * Phoenix.
		 */
		Phoenix("phoenix", "Phoenixes", 35),
		/**
		 * Simurgh.
		 */
		Simurgh("simurgh", "Simurghs", 35),
		/**
		 * Troll.
		 */
		Troll("troll", "Trolls", 28);
		/**
		 * The word to use for both default image and XML tag.
		 */
		private final String tag;
		/**
		 * The plural of this kind.
		 */
		private final String plural;
		/**
		 * The DC to use for this kind.
		 */
		private final int dc;
		/**
		 * @param tagParam the word to use for both default image and XML tag.
		 * @param pluralString the plural of this kind
		 * @param dcNum the DC to use for this kind
		 */
		private SimpleImmortalKind(final String tagParam, final String pluralString,
								   final int dcNum) {
			tag = tagParam;
			plural = pluralString;
			dc = dcNum;
		}
		/**
		 * @return the word to use for both default image and XML tag.
		 */
		public String getTag() {
			return tag;
		}
		/**
		 * @return the plural of this kind
		 */
		public String plural() {
			return plural;
		}
		/**
		 * The DC to use for this kind.
		 */
		public int getDC() {
			return dc;
		}
		/**
		 * @param desc a string representing a kind of immortal
		 * @return what kind that represents
		 */
		public static SimpleImmortalKind parse(final String desc) {
			return Stream.of(values()).filter(kind -> desc.equals(kind.tag)).findAny()
						   .orElseThrow(
								   () -> new IllegalArgumentException("No immortal kinds matching " +
																			  desc));
		}
	}
	/**
	 * What kind of immortal this is.
	 */
	private final SimpleImmortalKind kind;
	/**
	 * The ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * @param iKind what kind of immortal this is
	 * @param idNum its ID number
	 */
	public SimpleImmortal(final SimpleImmortalKind iKind, final int idNum) {
		kind = iKind;
		id = idNum;
	}
	/**
	 * Clone the object.
	 * @param zero ignored, as a simple immortal has no sensitive information
	 * @return a copy of this object
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public SimpleImmortal copy(final boolean zero) {
		final SimpleImmortal retval = new SimpleImmortal(kind, id);
		retval.image = image;
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the immortal
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return kind.getTag();
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the immortal
	 */
	@Override
	public String getDefaultImage() {
		return kind.getTag() + ".png";
	}

	/**
	 * An object is equal iff it is a SimpleImmortal of the same kind with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof SimpleImmortal)
										 && (((TileFixture) obj).getID() == id) &&
										 ((SimpleImmortal) obj).kind == kind);
	}

	/**
	 * Use the ID for hashing.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * The ID number.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * If we ignore ID, all SimpleImmortals of the same kind are equal.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID.
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof SimpleImmortal && ((SimpleImmortal) fix).kind == kind;
	}

	/**
	 * A fixture is a subset iff it is equal.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof SimpleImmortal && ((SimpleImmortal) obj).kind == kind) {
				return true;
			} else {
				ostream.format("%s\tFor ID #%d, different kinds of members%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%sCalled with different IDs, #%d and #%d%n", context,
					Integer.valueOf(id), Integer.valueOf(obj.getID()));
			return false;
		}
	}

	/**
	 * The per-instance icon filename.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Sphinx is Sphinxes.
	 * @return a string describing all sphinxes as a class
	 */
	@Override
	public String plural() {
		return kind.plural();
	}

	/**
	 * A short description.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (kind == SimpleImmortalKind.Ogre) {
			return "an ogre";
		} else {
			return "a " + kind.getTag();
		}
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return kind.getDC();
	}
	/**
	 * @return what kind of immortal this is
	 */
	@Override
	public String getKind() {
		return kind.getTag();
	}
	/**
	 * @return what kind of immortal this is
	 */
	public SimpleImmortalKind kind() {
		return kind;
	}
}
