package model.map.fixtures.mobile;

import model.map.XMLWritableImpl;
import model.map.fixtures.UnitMember;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture:
 * these should only be part of a unit, not as a top-level tag. (And TODO: some
 * of the other MobileFixtures should be similarly converted.)
 *
 * TODO: Add Jobs, skills, etc.
 * @author Jonathan Lovelace
 *
 */
public class Worker extends XMLWritableImpl implements UnitMember {
	/**
	 * Constructor.
	 * @param wName the worker's name
	 * @param file the file the worker was loaded from
	 * @param idNum the ID number of the worker
	 */
	public Worker(final String wName, final String file, final int idNum) {
		super(file);
		name = wName;
		id = idNum;
	}
	/**
	 * The ID number of the worker.
	 */
	private final int id; // NOPMD
	/**
	 * @return the ID number of the worker.
	 */
	public int getID() {
		return id;
	}
	/**
	 * The worker's name.
	 */
	private final String name;
	/**
	 * @return the worker's name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Worker && ((Worker) obj).name.equals(name)
				&& ((Worker) obj).id == id);
	}
	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}
}
