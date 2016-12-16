package controller.map.fluidxml;

import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.SPWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.Village;
import util.LineEnd;
import util.NullCleaner;

import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
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
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated FluidXML is deprecated in favor of YAXML
 */
@Deprecated
public class SPFluidWriter implements SPWriter, FluidXMLWriter {
	/**
	 * An extracted compiled Pattern for a close-tag without a space.
	 */
	private static final Pattern SNUG_END_TAG =
			Pattern.compile("([^ ])/>");
	/**
	 * A map from classes to the writers that write them to XML.
	 */
	private final Map<Class<?>, FluidXMLWriter> writers = new HashMap<>();

	/**
	 * Set up the writers.
	 */
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

	/**
	 * Write a player to XML. This is here because it's not a good fit for any of
	 * the other classes that collect methods.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private static void writePlayer(final XMLStreamWriter ostream, final Object obj,
									final int indent) throws XMLStreamException {
		if (!(obj instanceof Player)) {
			throw new IllegalArgumentException("Can only write Player");
		}
		final Player player = (Player) obj;
		writeTag(ostream, "player", indent, true);
		writeIntegerAttribute(ostream, "number", player.getPlayerId());
		writeAttribute(ostream, "code_name", player.getName());
	}

	/**
	 * Create DOM subtree representing the given object.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException on error in the writer
	 */
	@Override
	public void writeSPObject(final XMLStreamWriter ostream, final Object obj,
							  final int indent)
			throws XMLStreamException, IllegalArgumentException {
		final Iterable<Class<?>> types = new ClassIterable(obj);
		for (final Class<?> cls : types) {
			if (writers.containsKey(cls)) {
				NullCleaner.assertNotNull(writers.get(cls))
						.writeSPObject(ostream, obj, indent);
				return;
			}
		}
		throw new IllegalArgumentException("Not an object we know how to write");
	}

	@Override
	public void write(final Path file, final IMapNG map)
			throws IOException {
		try (final Writer writer = Files.newBufferedWriter(file)) {
			writeSPObject(writer, map);
		}
	}

	@Override
	public void write(final Appendable ostream, final IMapNG map)
			throws IOException {
		writeSPObject(ostream, map);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the object to write
	 * @throws IOException        on I/O error
	 */
	@Override
	public void writeSPObject(final Appendable ostream, final Object obj)
			throws IOException {
		final XMLOutputFactory xof = XMLOutputFactory.newInstance();
		final StringWriter writer = new StringWriter();
		try {
			final XMLStreamWriter xsw = xof.createXMLStreamWriter(writer);
			xsw.setDefaultNamespace(ISPReader.NAMESPACE);
			writeSPObject(xsw, obj, 0);
			xsw.writeCharacters(LineEnd.LINE_SEP);
			xsw.writeEndDocument();
			xsw.flush();
			xsw.close();
		} catch (final XMLStreamException except) {
			throw new IOException("Failure in creating XML", except);
		}
		ostream.append(SNUG_END_TAG.matcher(writer.toString()).replaceAll("$1 />"));
	}

	/**
	 * Create a writer for the simplest cases (only an ID number and maybe an image, or
	 * an ID number and a kind), and add this writer to our collection.
	 *
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
			writeTag(ostream, tag, indent, true);
			if (obj instanceof HasKind) {
				writeAttribute(ostream, "kind", ((HasKind) obj).getKind());
			}
			writeIntegerAttribute(ostream, "id", ((IFixture) obj).getID());
			if (obj instanceof HasImage) {
				writeImage(ostream, (HasImage) obj);
			}
		});
	}
	/**
	 * Write a unit's orders or results to XML.
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param turn which turn this is for
	 * @param tag the tag to use, either "orders" or "results"
	 * @param text the text of the orders or results
	 * @throws XMLStreamException       on error in the writer
	 */
	private static void writeUnitOrders(final XMLStreamWriter ostream, final int indent,
										final int turn, final String tag,
										final String text) throws XMLStreamException {
		if (text.isEmpty()) {
			return;
		}
		writeTag(ostream, tag, indent, false);
		if (turn >= 0) {
			writeIntegerAttribute(ostream, "turn", turn);
		}
		ostream.writeCharacters(text);
		ostream.writeEndElement();
	}
	/**
	 * Write a unit to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeUnit(final XMLStreamWriter ostream, final Object obj,
						   final int indent) throws XMLStreamException {
		if (!(obj instanceof IUnit)) {
			throw new IllegalArgumentException("Can only write IUnit");
		}
		final IUnit unit = (IUnit) obj;
		final boolean hasContents =
				unit.iterator().hasNext() || !unit.getAllOrders().isEmpty() ||
						!unit.getAllResults().isEmpty();
		writeTag(ostream, "unit", indent, !hasContents);
		writeIntegerAttribute(ostream, "owner", unit.getOwner().getPlayerId());
		writeNonEmptyAttribute(ostream, "kind", unit.getKind());
		writeNonEmptyAttribute(ostream, "name", unit.getName());
		writeIntegerAttribute(ostream, "id", unit.getID());
		writeImage(ostream, unit);
		if (unit instanceof HasPortrait) {
			writeNonEmptyAttribute(ostream, "portrait",
					((HasPortrait) unit).getPortrait());
		}
		for (final Map.Entry<Integer, String> entry : unit.getAllOrders().entrySet()) {
			writeUnitOrders(ostream, indent + 1, entry.getKey().intValue(), "orders",
					entry.getValue().trim());
		}
		for (final Map.Entry<Integer, String> entry : unit.getAllResults().entrySet()) {
			writeUnitOrders(ostream, indent + 1, entry.getKey().intValue(), "results",
					entry.getValue().trim());
		}
		for (final UnitMember member : unit) {
			writeSPObject(ostream, member, indent + 1);
		}
		if (hasContents) {
			XMLHelper.indent(ostream, indent);
			ostream.writeEndElement();
		}
	}

	/**
	 * Write a fortress to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeFortress(final XMLStreamWriter ostream, final Object obj,
							   final int indent) throws XMLStreamException {
		if (!(obj instanceof Fortress)) {
			throw new IllegalArgumentException("Can only write Fortress");
		}
		final Fortress fort = (Fortress) obj;
		writeTag(ostream, "fortress", indent, false);
		writeIntegerAttribute(ostream, "owner", fort.getOwner().getPlayerId());
		writeNonEmptyAttribute(ostream, "name", fort.getName());
		if (TownSize.Small != fort.size()) {
			writeAttribute(ostream, "size", fort.size().toString());
		}
		writeIntegerAttribute(ostream, "id", fort.getID());
		writeImage(ostream, fort);
		writeNonEmptyAttribute(ostream, "portrait", fort.getPortrait());
		boolean any = false;
		//noinspection unchecked: checked as first operation of method
		for (final FortressMember unit : (Iterable<FortressMember>) obj) {
			any = true;
			writeSPObject(ostream, unit, indent + 1);
		}
		if (any) {
			XMLHelper.indent(ostream, indent);
		}
		ostream.writeEndElement();
	}

	/**
	 * Write a map to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeMap(final XMLStreamWriter ostream, final Object obj,
						  final int indent) throws XMLStreamException {
		if (!(obj instanceof IMapNG)) {
			throw new IllegalArgumentException("Can only write IMapNG");
		}
		final IMapNG map = (IMapNG) obj;
		writeTag(ostream, "view", indent, false);
		writeIntegerAttribute(ostream, "current_player",
				map.getCurrentPlayer().getPlayerId());
		writeIntegerAttribute(ostream, "current_turn", map.getCurrentTurn());
		writeTag(ostream, "map", indent + 1, false);
		final MapDimensions dim = map.dimensions();
		writeIntegerAttribute(ostream, "version", dim.version);
		writeIntegerAttribute(ostream, "rows", dim.rows);
		writeIntegerAttribute(ostream, "columns", dim.cols);
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
							|| map.streamOtherFixtures(point).anyMatch(x -> true)) {
					if (rowEmpty) {
						writeTag(ostream, "row", indent + 2, false);
						rowEmpty = false;
						writeIntegerAttribute(ostream, "index", i);
					}
					writeTag(ostream, "tile", indent + 3, false);
					writeIntegerAttribute(ostream, "row", i);
					writeIntegerAttribute(ostream, "column", j);
					if (TileType.NotVisible != terrain) {
						writeAttribute(ostream, "kind", terrain.toXML());
					}
					boolean anyContents = false;
					if (map.isMountainous(point)) {
						anyContents = true;
						writeTag(ostream, "mountain", indent + 4, true);
					}
					for (final River river : map.getRivers(point)) {
						anyContents = true;
						writeSPObject(ostream, river, indent + 4);
					}
					final Ground ground = map.getGround(point);
					if (ground != null) {
						anyContents = true;
						writeSPObject(ostream, ground, indent + 4);
					}
					final Forest forest = map.getForest(point);
					if (forest != null) {
						anyContents = true;
						writeSPObject(ostream, forest, indent + 4);
					}
					for (final TileFixture fixture : map.getOtherFixtures(point)) {
						anyContents = true;
						writeSPObject(ostream, fixture, indent + 4);
					}
					if (anyContents) {
						XMLHelper.indent(ostream, indent + 3);
					}
					ostream.writeEndElement();
				}
			}
			if (!rowEmpty) {
				XMLHelper.indent(ostream, indent + 2);
				ostream.writeEndElement();
			}
		}
		XMLHelper.indent(ostream, indent + 1);
		ostream.writeEndElement();
		XMLHelper.indent(ostream, indent);
		ostream.writeEndElement();
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SPFluidWriter";
	}
}
