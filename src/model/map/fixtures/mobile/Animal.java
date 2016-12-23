package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An animal or group of animals.
 *
 * TODO: Add more features (population, to start with).
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
public class Animal implements MobileFixture, HasMutableImage, HasKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * Whether this is really the animal, or only traces.
	 */
	private final boolean traces;
	/**
	 * Whether this is a talking animal.
	 */
	private final boolean talking;
	/**
	 * The domestication status of the animal.
	 *
	 * TODO: Should this be an enumerated type?
	 */
	private String status;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * Kind of animal.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param animal  what kind of animal
	 * @param tracks  whether this is really the animal, or only tracks
	 * @param talks   whether this is a talking animal.
	 * @param dStatus domestication status
	 * @param idNum   the ID number.
	 */
	public Animal(final String animal, final boolean tracks,
				  final boolean talks, final String dStatus, final int idNum) {
		kind = animal;
		traces = tracks;
		talking = talks;
		status = dStatus;
		id = idNum;
	}

	/**
	 * The ID number of the animal.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Whether this instance is just tracks or traces instead of an actual animal.
	 * @return true if this is only traces or tracks, false if this is really the animal
	 */
	public boolean isTraces() {
		return traces;
	}

	/**
	 * Whether the animal is a talking animal.
	 * @return whether the animal is a talking animal
	 */
	public boolean isTalking() {
		return talking;
	}

	/**
	 * What kind of animal this is.
	 * @return what kind of animal this is
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The domestication status of the animal.
	 * @return the domestication status of the animal
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * Set the domestication status of the animal.
	 * @param nStatus the new status
	 */
	public final void setStatus(final String nStatus) {
		status = nStatus;
	}

	/**
	 * A simple String representation of the animal.
	 * @return a String representation of the animal
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(18 + kind.length());
		if (traces) {
			builder.append("traces of ");
		}
		if (talking) {
			builder.append("talking ");
		}
		builder.append(kind);
		return builder.toString();
	}

	/**
	 * TODO: Should depend on the kind of animal.
	 *
	 * @return the name of an image to represent the animal
	 */
	@Override
	public String getDefaultImage() {
		if (traces) {
			return "tracks.png";
		} else {
			return "animal.png";
		}
	}

	/**
	 * Test equality.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Animal)
										 && kind.equals(((Animal) obj).kind)
										 && (((Animal) obj).traces == traces)
										 && (((Animal) obj).talking == talking)
										 && status.equals(((Animal) obj).status)
										 && (((Animal) obj).id == id || traces));
	}

	/**
	 * Test whether another fixture is an Animal that is a strict subset of this one.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member is a subset of this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Animal) {
				return areObjectsEqual(ostream, kind, ((Animal) obj).kind,
						"%s\tDifferent kinds of animal for ID #%d%n", context,
						Integer.valueOf(id)) && isConditionTrue(ostream,
						talking || !((Animal) obj).talking,
						"%s\tIn animal ID #%d:\tSubmap's is talking and master's " +
								"isn't%n",
						context, Integer.valueOf(id)) &&
							   isConditionTrue(ostream, !traces || ((Animal) obj).traces,
									   "%s\tIn animal ID #%d:\tSubmap has animal and " +
											   "master only tracks%n",
									   context, Integer.valueOf(id)) &&
							   areObjectsEqual(ostream, status, ((Animal) obj).status,
									   "%s\tAnimal domestication status differs at ID #%d%n",
									   context, Integer.valueOf(id));
			} else {
				ostream.format("%s\tFor ID #%d, different kinds of members%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%s\tCalled with different IDs, #%d and #%d%n", context,
					Integer.valueOf(id), Integer.valueOf(obj.getID()));
			return false;
		}
	}

	/**
	 * Use ID number for hash value.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * Test equality ignoring ID number.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Animal) && ((Animal) fix).kind.equals(kind)
					   && (((Animal) fix).traces == traces)
					   && ((Animal) fix).status.equals(status)
					   && (((Animal) fix).talking == talking);
	}

	/**
	 * The icon to use for this instance.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the icon to use for this instance.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of "animal" is "animals".
	 * @return a string describing all animals as a class
	 */
	@Override
	public String plural() {
		return "Animals";
	}

	/**
	 * The "short description" is the same as {@link #toString}.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}

	/**
	 * Clone the animal.
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Animal copy(final boolean zero) {
		// TODO: Should we "zero" out any information?
		final Animal retval = new Animal(kind, traces, talking, status, id);
		retval.image = image;
		return retval;
	}
}
