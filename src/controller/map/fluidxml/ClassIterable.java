package controller.map.fluidxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
public class ClassIterable implements Iterable<Class<?>> {
	private final List<Class<?>> classes = new ArrayList<>();
	public ClassIterable(final Object obj) {
		final Class<?> base = obj.getClass();
		classes.add(base);
		addInterfaces(classes, base);
		for (Class<?> cls = base.getSuperclass(); cls != null; cls = cls.getSuperclass()) {
			classes.add(cls);
			addInterfaces(classes, cls);
		}
	}
	public Iterator<Class<?>> iterator() {
		return classes.iterator();
	}
	private static void addInterfaces(final Collection<Class<?>> list, final Class<?> cls) {
		for (Class<?> interf : cls.getInterfaces()) {
			list.add(interf);
			addInterfaces(list, interf);
		}
	}
}
