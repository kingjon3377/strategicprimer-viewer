package controller.map.fluidxml;

import controller.map.iointerfaces.SPWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Ground;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.UnitMember;
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
import model.map.fixtures.mobile.IUnit;
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
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Village;
import util.NullCleaner;

import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.indent;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
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
	private final Map<Class<?>, FluidXMLWriter> writers = new HashMap<>();
	public SPFluidWriter() {
		writers.put(River.class, FluidTerrainHandler::writeRivers);
		writers.put(RiverFixture.class, FluidTerrainHandler::writeRivers);
		writers.put(AdventureFixture.class, FluidExplorableHandler::writeAdventure);
		writers.put(Portal.class, FluidExplorableHandler::writePortal);
		writers.put(Battlefield.class, FluidExplorableHandler::writeBattlefield);
		writers.put(Cave.class, FluidExplorableHandler::writeCave);
		writers.put(Ground.class, FluidTerrainHandler::writeGround);
		writers.put(Forest.class, FluidTerrainHandler::writeForest);
		createSimpleFixtureWriter(Hill.class, "hill");
		createSimpleFixtureWriter(Oasis.class, "oasis");
		createSimpleFixtureWriter(Sandbar.class, "sandbar");
		writers.put(Mountain.class, FluidTerrainHandler::writeMountain);
		writers.put(Animal.class, FluidUnitMemberHandler::writeAnimal);
		createSimpleFixtureWriter(Centaur.class, "centaur");
		createSimpleFixtureWriter(Djinn.class, "djinn");
		createSimpleFixtureWriter(Dragon.class, "dragon");
		createSimpleFixtureWriter(Fairy.class, "fairy");
		createSimpleFixtureWriter(Giant.class, "giant");
		createSimpleFixtureWriter(Griffin.class, "griffin");
		createSimpleFixtureWriter(Minotaur.class, "minotaur");
		createSimpleFixtureWriter(Ogre.class, "ogre");
		createSimpleFixtureWriter(Phoenix.class, "phoenix");
		createSimpleFixtureWriter(Simurgh.class, "simurgh");
		createSimpleFixtureWriter(Sphinx.class, "sphinx");
		createSimpleFixtureWriter(Troll.class, "troll");
		writers.put(TextFixture.class, FluidExplorableHandler::writeTextFixture);
		createSimpleFixtureWriter(Implement.class, "implement");
		writers.put(ResourcePile.class, FluidResourceHandler::writeResource);
		writers.put(CacheFixture.class, FluidResourceHandler::writeCache);
		writers.put(Meadow.class, FluidResourceHandler::writeMeadow);
		writers.put(Grove.class, FluidResourceHandler::writeGrove);
		writers.put(Mine.class, FluidResourceHandler::writeMine);
		writers.put(MineralVein.class, FluidResourceHandler::writeMineral);
		createSimpleFixtureWriter(Shrub.class, "shrub");
		writers.put(StoneDeposit.class, FluidResourceHandler::writeStone);
		writers.put(IWorker.class, FluidUnitMemberHandler::writeWorker);
		writers.put(IJob.class, FluidUnitMemberHandler::writeJob);
		writers.put(ISkill.class, FluidUnitMemberHandler::writeSkill);
		writers.put(WorkerStats.class, FluidUnitMemberHandler::writeStats);
		writers.put(IUnit.class, this::writeUnit);
		writers.put(Fortress.class, this::writeFortress);
		writers.put(Village.class, FluidTownHandler::writeVillage);
		writers.put(AbstractTown.class, FluidTownHandler::writeTown);
		writers.put(IMapNG.class, this::writeMap);
		writers.put(Player.class, SPFluidWriter::writePlayer);
	}
	@Override
	public void writeSPObject(final Appendable ostream, final Object obj,
							  final int indent)
			throws IOException, IllegalArgumentException {
		final Iterable<Class<?>> types = new ClassIterable(obj);
		for (final Class<?> cls : types) {
			if (writers.containsKey(cls)) {
				NullCleaner.assertNotNull(writers.get(cls)).writeSPObject(ostream, obj, indent);
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
	private void createSimpleFixtureWriter(final Class<?> cls, final String tag) {
		writers.put(cls, (ostream, obj, indent) -> {
			if (!cls.isInstance(obj)) {
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
				writeImage(ostream, (HasImage) obj);
			}
			ostream.append(" />\n");
		});
	}
	/**
	 * Write a unit to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an IUnit
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	private void writeUnit(final Appendable ostream, final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof IUnit)) {
			throw new IllegalArgumentException("Can only write IUnit");
		}
		final IUnit unit = (IUnit) obj;
		writeTag(ostream, "unit", indent);
		writeIntegerAttribute(ostream, "owner", unit.getOwner().getPlayerId());
		writeNonEmptyAttribute(ostream, "kind", unit.getKind());
		writeNonEmptyAttribute(ostream, "name", unit.getName());
		writeIntegerAttribute(ostream, "id", unit.getID());
		writeImage(ostream, unit);
		if (unit instanceof HasPortrait) {
			writeNonEmptyAttribute(ostream, "portrait",
					((HasPortrait) unit).getPortrait());
		}
		final String orders = unit.getOrders().trim();
		if (unit.iterator().hasNext() || !orders.isEmpty()) {
			ostream.append('>').append(orders).append('\n');
			for (final UnitMember member : unit) {
				writeSPObject(ostream, member, indent + 1);
			}
			indent(ostream, indent);
			ostream.append("</unit>\n");
		} else {
			ostream.append(" />\n");
		}
	}
	/**
	 * Write a fortress to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be a Fortress
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	private void writeFortress(final Appendable ostream, final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof Fortress)) {
			throw new IllegalArgumentException("Can only write Fortress");
		}
		final Fortress fort = (Fortress) obj;
		writeTag(ostream, "fortress", indent);
		writeIntegerAttribute(ostream, "owner", fort.getOwner().getPlayerId());
		writeNonEmptyAttribute(ostream, "name", fort.getName());
		writeIntegerAttribute(ostream, "id", fort.getID());
		writeImage(ostream, fort);
		writeNonEmptyAttribute(ostream, "portrait", fort.getPortrait());
		ostream.append('>');
		if (fort.iterator().hasNext()) {
			ostream.append('\n');
			for (final FortressMember unit : (Iterable<FortressMember>) obj) {
				writeSPObject(ostream, unit, indent + 1);
			}
			indent(ostream, indent);
		}
		ostream.append("</fortress>\n");
	}
	/**
	 * Write a map to XML.
	 * @param ostream the stream to write to
	 * @param obj the map to write. Must be an IMapNG.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	private void writeMap(final Appendable ostream, final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof IMapNG)) {
			throw new IllegalArgumentException("Can only write IMapNG");
		}
		final IMapNG map = (IMapNG) obj;
		writeTag(ostream, "view", indent);
		writeIntegerAttribute(ostream, "current_player",
				map.getCurrentPlayer().getPlayerId());
		writeIntegerAttribute(ostream, "current_turn", map.getCurrentTurn());
		ostream.append(">\n");
		writeTag(ostream, "map", indent + 1);
		final MapDimensions dim = map.dimensions();
		writeIntegerAttribute(ostream, "version", dim.version);
		writeIntegerAttribute(ostream, "rows", dim.rows);
		writeIntegerAttribute(ostream, "columns", dim.cols);
		ostream.append(">\n");
		for (final Player player : map.players()) {
			writeSPObject(ostream, player, indent + 2);
		}
		for (int i = 0; i < dim.rows; i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				final TileType terrain = map.getBaseTerrain(point);
				if ((TileType.NotVisible != terrain)
							|| map.isMountainous(point)
							|| (map.getGround(point) != null)
							|| (map.getForest(point) != null)
							|| map.streamOtherFixtures(point).anyMatch(x->true)) {
					if (rowEmpty) {
						rowEmpty = false;
						writeTag(ostream, "row", indent + 2);
						writeIntegerAttribute(ostream, "index", i);
						ostream.append(">\n");
					}
					writeTag(ostream, "tile", indent + 3);
					writeIntegerAttribute(ostream, "row", i);
					writeIntegerAttribute(ostream, "column", j);
					if (TileType.NotVisible != terrain) {
						writeAttribute(ostream, "kind", terrain.toXML());
					}
					ostream.append(">");
					boolean needEOL = true;
					if (map.isMountainous(point)) {
						eolIfNeeded(true, ostream);
						needEOL = false;
						writeTag(ostream, "mountain", indent + 4);
						ostream.append(" />\n");
					}
					for (final River river : map.getRivers(point)) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						writeSPObject(ostream, river, indent + 4);
					}
					final Ground ground = map.getGround(point);
					if (ground != null) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						writeSPObject(ostream, ground, indent + 4);
					}
					final Forest forest = map.getForest(point);
					if (forest != null) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						writeSPObject(ostream, forest, indent + 4);
					}
					for (final TileFixture fixture : map.getOtherFixtures(point)) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						writeSPObject(ostream, fixture, indent + 4);
					}
					if (!needEOL) {
						indent(ostream, indent + 3);
					}
					ostream.append("</tile>\n");
				}
			}
			if (!rowEmpty) {
				indent(ostream, indent + 2);
				ostream.append("</row>\n");
			}
		}
		indent(ostream, indent + 1);
		ostream.append("</map>\n");
		indent(ostream, indent);
		ostream.append("</view>\n");
	}
	/**
	 * Write a newline if needed.
	 *
	 * @param writer  the writer to write to
	 * @param needEOL whether we need a newline.
	 * @throws IOException on I/O error
	 */
	private static void eolIfNeeded(final boolean needEOL,
									final Appendable writer) throws IOException {
		if (needEOL) {
			writer.append('\n');
		}
	}
	/**
	 * Write a player to a stream. This is here because it's not a good fit for any of
	 * the other classes that collect methods.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be a Player.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	private static void writePlayer(final Appendable ostream, final Object obj,
										  final int indent) throws IOException {
		if (!(obj instanceof Player)) {
			throw new IllegalArgumentException("Can only write Player");
		}
		final Player player = (Player) obj;
		writeTag(ostream, "player", indent);
		writeIntegerAttribute(ostream, "number", player.getPlayerId());
		writeAttribute(ostream, "code_name", player.getName());
		ostream.append(" />\n");
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SPFluidWriter";
	}
}
