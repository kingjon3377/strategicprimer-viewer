package controller.map.fluidxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A utility class to make iterating over the types of an object (its class, its
 * interfaces, and its superclasses) easy.
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
public class ClassStream implements Supplier<Stream<Class<?>>> {
	/**
	 * The list of class objects.
	 */
	private final Collection<Class<?>> classes = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param obj the object whose superclasses we want to iterate over.
	 */
	public ClassStream(final Object obj) {
		final Class<?> base = obj.getClass();
		classes.add(base);
		addInterfaces(classes, base);
		for (Class<?> cls = base.getSuperclass(); cls != null;
				cls = cls.getSuperclass()) {
			classes.add(cls);
			addInterfaces(classes, cls);
		}
	}

	/**
	 * Add the interfaces satisfied by the given class, recursively, to the given list.
	 *
	 * @param list the list of classes
	 * @param cls  the class to get the interfaces of
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addInterfaces(final Collection<Class<?>> list,
									  final Class<?> cls) {
		for (final Class<?> iface : cls.getInterfaces()) {
			list.add(iface);
			addInterfaces(list, iface);
		}
	}

	/**
	 * An iterator over the object's superclasses.
	 * @return an iterator over the object's superclasses.
	 */
	@Override
	public Stream<Class<?>> get() {
		return classes.stream();
	}

	/**
	 * We basically just delegate to the implementing list.
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ClassStream: " + classes;
	}
}
