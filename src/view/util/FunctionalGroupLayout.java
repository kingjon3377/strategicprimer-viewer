package view.util;

import java.awt.Component;
import java.awt.Container;
import javax.swing.GroupLayout;

/**
 * An extension to GroupLayout to provide helper methods to make initializing a
 * GroupLayout less verbose and use a more functional style.
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
public class FunctionalGroupLayout extends GroupLayout {
	/**
	 * @param host the Container this is the layout manager for
	 * @throws IllegalArgumentException if host is {@code null}
	 */
	public FunctionalGroupLayout(final Container host) {
		super(host);
	}
	/**
	 * @param components components to add in a parallel group. Can be Components or
	 *                      Groups; will throw IllegalArgumentException on anything else
	 * @return that group
	 */
	public ParallelGroup createParallelGroupOf(final Object... components) {
		final ParallelGroup retval = createParallelGroup();
		for (final Object component : components) {
			if (component instanceof Component) {
				retval.addComponent((Component) component);
			} else if (component instanceof Group) {
				retval.addGroup((Group) component);
			} else {
				throw new IllegalArgumentException(
						"Can only handle Components and Groups");
			}
		}
		return retval;
	}
	/**
	 * @param components components to add in a sequential group. Can be Components or
	 *                      Groups; will throw IllegalArgumentException on anything else.
	 * @return that group
	 */
	public SequentialGroup createSequentialGroupOf(final Object... components) {
		final SequentialGroup retval = createSequentialGroup();
		for (final Object component : components) {
			if (component instanceof Component) {
				retval.addComponent((Component) component);
			} else if (component instanceof Group) {
				retval.addGroup((Group) component);
			} else {
				throw new IllegalArgumentException(
						"Can only handle Components and Groups");
			}
		}
		return retval;
	}
}
