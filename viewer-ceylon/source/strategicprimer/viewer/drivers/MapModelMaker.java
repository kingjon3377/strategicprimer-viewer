package strategicprimer.viewer.drivers;

import ceylon.language.Sequence;
import controller.map.drivers.DriverFailedException;
import controller.map.misc.MapReaderAdapter;
import java.nio.file.Paths;
import model.misc.IMultiMapModel;
import util.Warning;

import static ceylon.interop.java.toJavaStringArray_.toJavaStringArray;

/**
 * A wrapper around MapReaderAdapter to work around a Ceylon compiler bug (TODO: report):
 * the typechecker requires "spreading" the Java array returned by namesToFiles(), but
 * the compiler then errors out because there isn't a sequence() method in ObjectArray.
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
 * @deprecated this is a hack
 */
@Deprecated
public class MapModelMaker {
	/**
	 * @param reader the reader to use
	 * @param warner the Warning instance to use
	 * @param array the array of arguments
	 */
	public static IMultiMapModel readMapModelHacked(MapReaderAdapter reader,
													Warning warner,
													Sequence<String> array)
			throws DriverFailedException {
		return reader.readMultiMapModel(warner, Paths.get(array.getFirst()),
				MapReaderAdapter.namesToFiles(false,
						toJavaStringArray(array.getRest())));
	}
}
