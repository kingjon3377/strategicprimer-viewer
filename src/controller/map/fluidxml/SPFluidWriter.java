package controller.map.fluidxml;

import controller.map.cxml.AbstractCompactReader;
import controller.map.cxml.CompactMapNGReader;
import controller.map.cxml.CompactPlayerReader;
import controller.map.cxml.CompactReader;
import controller.map.cxml.CompactTownReader;
import controller.map.cxml.CompactUnitReader;
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
import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.River;
import model.map.fixtures.Ground;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;

import static controller.map.fluidxml.XMLHelper.imageXML;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;

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
				CompactMapNGReader.READER,
				CompactPlayerReader.READER,
				CompactTownReader.READER,
				CompactUnitReader.READER)) {
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
		addSimpleFixtureWriter(Hill.class, "hill");
		addSimpleFixtureWriter(Oasis.class, "oasis");
		addSimpleFixtureWriter(Sandbar.class, "sandbar");
		writers.put(Mountain.class, FluidTerrainHandler::writeMountain);
		writers.put(Animal.class, FluidMobileHandler::writeAnimal);
		addSimpleFixtureWriter(Centaur.class, "centaur");
		addSimpleFixtureWriter(Djinn.class, "djinn");
		addSimpleFixtureWriter(Dragon.class, "dragon");
		addSimpleFixtureWriter(Fairy.class, "fairy");
		addSimpleFixtureWriter(Giant.class, "giant");
		addSimpleFixtureWriter(Griffin.class, "griffin");
		addSimpleFixtureWriter(Minotaur.class, "minotaur");
		addSimpleFixtureWriter(Ogre.class, "ogre");
		addSimpleFixtureWriter(Phoenix.class, "phoenix");
		addSimpleFixtureWriter(Simurgh.class, "simurgh");
		addSimpleFixtureWriter(Sphinx.class, "sphinx");
		addSimpleFixtureWriter(Troll.class, "troll");
		writers.put(TextFixture.class, FluidExplorableHandler::writeTextFixture);
		addSimpleFixtureWriter(Implement.class, "implement");
		writers.put(ResourcePile.class, FluidResourceHandler::writeResource);
		writers.put(CacheFixture.class, FluidResourceHandler::writeCache);
		writers.put(Meadow.class, FluidResourceHandler::writeMeadow);
		writers.put(Grove.class, FluidResourceHandler::writeGrove);
		writers.put(Mine.class, FluidResourceHandler::writeMine);
		writers.put(MineralVein.class, FluidResourceHandler::writeMineral);
		addSimpleFixtureWriter(Shrub.class, "shrub");
		writers.put(StoneDeposit.class, FluidResourceHandler::writeStone);
		writers.put(IWorker.class, FluidWorkerHandler::writeWorker);
		writers.put(IJob.class, FluidWorkerHandler::writeJob);
		writers.put(ISkill.class, FluidWorkerHandler::writeSkill);
		writers.put(WorkerStats.class, FluidWorkerHandler::writeStats);
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
	/**
	 * Create a writer for the simplest cases (only an ID number and maybe an image, or
	 * an ID number and a kind), and add this writer to our collection.
	 * @param cls the class of objects to use this writer for
	 * @param tag the tag to be used for this class
	 */
	private void addSimpleFixtureWriter(final Class<?> cls, final String tag) {
		writers.put(cls, (ostream, obj, indent) -> {
			if (!(cls.isInstance(obj))) {
				throw new IllegalArgumentException("Can only write " +
														   cls.getSimpleName());
			} else if (!(obj instanceof IFixture)) {
				throw new IllegalStateException("Can only 'simply' write fixtures");
			}
			writeTag(ostream, tag, indent);
			if (obj instanceof HasKind) {
				writeAttribute(ostream, "kind", ((HasKind) obj).getKind());
			}
			writeIntegerAttribute(ostream, "id", ((IFixture) obj).getID());
			if (obj instanceof HasImage) {
				ostream.append(imageXML((HasImage) obj));
			}
			ostream.append(" />\n");
		});
	}
}
