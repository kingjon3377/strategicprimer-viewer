package controller.map.fluidxml;

import controller.map.cxml.AbstractCompactReader;
import controller.map.cxml.CompactImplementReader;
import controller.map.cxml.CompactMapNGReader;
import controller.map.cxml.CompactMobileReader;
import controller.map.cxml.CompactPlayerReader;
import controller.map.cxml.CompactReader;
import controller.map.cxml.CompactResourcePileReader;
import controller.map.cxml.CompactResourceReader;
import controller.map.cxml.CompactTextReader;
import controller.map.cxml.CompactTownReader;
import controller.map.cxml.CompactUnitReader;
import controller.map.cxml.CompactWorkerReader;
import controller.map.iointerfaces.SPWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.map.IMapNG;
import model.map.River;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;

/**
 * The main writer-to-XML class in the 'fluid XML' implementation.
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
public class SPFluidWriter implements SPWriter, FluidXMLWriter {
	final Map<Class<?>, FluidXMLWriter> writers = new HashMap<>();
	public SPFluidWriter() {
		for (CompactReader writer : Arrays.asList(
				CompactImplementReader.READER, CompactMapNGReader.READER,
				CompactMobileReader.READER, CompactPlayerReader.READER,
				CompactResourcePileReader.READER, CompactResourceReader.READER,
				CompactTextReader.READER, CompactTownReader.READER,
				CompactUnitReader.READER, CompactWorkerReader.READER)) {
			Type type = writer.getClass().getGenericSuperclass();
			while (!(type instanceof ParameterizedType) || ((ParameterizedType) type).getRawType() != AbstractCompactReader.class) {
				if (type instanceof ParameterizedType) {
					type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
				} else {
					type = ((Class<?>) type).getGenericSuperclass();
				}
			}
			writers.put((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0], writer::write);
		}
		writers.put(River.class, (ostream, obj, indent) -> CompactMapNGReader
																   .writeRiver(ostream,
																		   (River) obj,
																		   indent));
		writers.put(RiverFixture.class, (ostream, obj, indent) -> CompactMapNGReader
																		  .writeAllRivers(
																				  ostream,
																				  (RiverFixture) obj,
																				  indent));
		writers.put(AdventureFixture.class, FluidExplorableHandler::writeAdventure);
		writers.put(Portal.class, FluidExplorableHandler::writePortal);
		writers.put(Battlefield.class, FluidExplorableHandler::writeBattlefield);
		writers.put(Cave.class, FluidExplorableHandler::writeCave);
		writers.put(Ground.class, FluidTerrainHandler::writeGround);
		writers.put(Forest.class, FluidTerrainHandler::writeForest);
		writers.put(Hill.class, FluidTerrainHandler::writeHill);
		writers.put(Oasis.class, FluidTerrainHandler::writeOasis);
		writers.put(Sandbar.class, FluidTerrainHandler::writeSandbar);
		writers.put(Mountain.class, FluidTerrainHandler::writeMountain);
	}
	@Override
	public void writeSPObject(final Appendable ostream, final Object obj,
							  final int indent)
			throws IOException, IllegalArgumentException {
		ClassIterable types = new ClassIterable(obj);
		for (final Class<?> cls : types) {
			if (writers.containsKey(cls)) {
				writers.get(cls).writeSPObject(ostream, obj, indent);
				return;
			}
		}
		throw new IllegalArgumentException("Not an object we know how to write");
	}

	@Override
	public void write(final File file, final IMapNG map) throws IOException {
		try (final Writer writer = new FileWriter(file)) {
			writeSPObject(writer, map, 0);
		}
	}

	@Override
	public void write(final Appendable ostream, final IMapNG map) throws IOException {
		writeSPObject(ostream, map, 0);
	}
}
