package model.viewer;

import java.util.function.Predicate;
import model.map.TileFixture;

/**
 * A wrapper around a Predicate-of-TileFixture, used to determine z-order of fixtures.
 *
 * TODO: Provide a convenience factory method that takes a Class and a method reference
 * on that class to simplify the "obj instanceof X && ((X) obj).method()" pattern
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class FixtureMatcher {
	/**
	 * The wrapped predicate.
	 */
	private final Predicate<TileFixture> predicate;
	/**
	 * Whether matching fixtures should be displayed.
	 */
	private boolean displayed = true;
	/**
	 * @param matcher the predicate to use to match fixtures.
	 * @param desc a description of the fixtures that it matches
	 */
	public FixtureMatcher(final Predicate<TileFixture> matcher, final String desc) {
		predicate = matcher;
		description = desc;
	}
	/**
	 * @param fixture a fixture
	 * @return whether we match it.
	 */
	public boolean matches(final TileFixture fixture) {
		return predicate.test(fixture);
	}
	/**
	 * @return whether matching fixtures should be displayed
	 */
	public boolean isDisplayed() {
		return displayed;
	}
	/**
	 * @param display whether matching fixtures should be displayed
	 */
	public void setDisplayed(final boolean display) {
		displayed = display;
	}
	/**
	 * A description of items matched by this matcher.
	 */
	private final String description;
	/**
	 * @return a description of items matched by this matcher
	 */
	public String getDescription() {
		return description;
	}
}
