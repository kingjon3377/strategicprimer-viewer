package controller.map.fluidxml;

import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.SPWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import util.NullCleaner;

import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;

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
	 * Create DOM subtree representing the given object.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj The object being written.
	 */
	@Override
	public void writeSPObject(final Document document, final Node parent, Object obj)
			throws IllegalArgumentException {
		final Iterable<Class<?>> types = new ClassIterable(obj);
		for (final Class<?> cls : types) {
			if (writers.containsKey(cls)) {
				NullCleaner.assertNotNull(writers.get(cls)).writeSPObject(document, parent, obj);
				return;
			}
		}
		throw new IllegalArgumentException("Not an object we know how to write");
	}

	@Override
	public void write(final Path file, final IMapNG map) throws IOException {
		try (final Writer writer = Files.newBufferedWriter(file)) {
			writeSPObject(writer, map);
		}
	}

	@Override
	public void write(final Appendable ostream, final IMapNG map) throws IOException {
		writeSPObject(ostream, map);
	}
	/**
	 * Write an object to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the object to write
	 * @throws IOException on I/O error
	 */
	public void writeSPObject(final Appendable ostream, final Object obj)
			throws IOException {
		final DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document document = builder.newDocument();
			writeSPObject(document, document, obj);
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			final Writer writer;
			if (ostream instanceof Writer) {
				writer = (Writer) ostream;
			} else if (ostream instanceof OutputStream) {
				writer = new OutputStreamWriter((OutputStream) ostream);
			} else {
				writer = new Writer() {
					@Override
					public void write(final char[] cbuf, final int off, final int len)
							throws IOException {
						ostream.append(CharBuffer.wrap(cbuf, off, len));
					}
					@Override
					public void flush() throws IOException {
						if (ostream instanceof Flushable) {
							((Flushable) ostream).flush();
						}
					}

					@Override
					public void close() throws IOException {
						if (ostream instanceof Closeable) {
							((Closeable) ostream).close();
						}
					}
				};
			}
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
	/**
	 * Create a writer for the simplest cases (only an ID number and maybe an image, or
	 * an ID number and a kind), and add this writer to our collection.
	 * @param cls the class of objects to use this writer for
	 * @param tag the tag to be used for this class
	 */
	private void createSimpleFixtureWriter(final Class<?> cls, final String tag) {
		writers.put(cls, (document, parent, obj) -> {
			if (!cls.isInstance(obj)) {
				throw new IllegalArgumentException("Can only write " +
														   cls.getSimpleName());
			} else if (!(obj instanceof IFixture)) {
				throw new IllegalStateException("Can only 'simply' write fixtures");
			}
			final Element element = document.createElementNS(ISPReader.NAMESPACE, tag);
			if (obj instanceof HasKind) {
				writeAttribute(element, "kind", ((HasKind) obj).getKind());
			}
			writeIntegerAttribute(element, "id", ((IFixture) obj).getID());
			if (obj instanceof HasImage) {
				writeImage(element, (HasImage) obj);
			}
			parent.appendChild(element);
		});
	}
	/**
	 * Write a unit to XML.
	 *
	 * @param obj     The object to write. Must be an IUnit
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeUnit(final Document document, final Node parent,
						   Object obj) {
		if (!(obj instanceof IUnit)) {
			throw new IllegalArgumentException("Can only write IUnit");
		}
		final IUnit unit = (IUnit) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "unit");
		writeIntegerAttribute(element, "owner", unit.getOwner().getPlayerId());
		writeNonEmptyAttribute(element, "kind", unit.getKind());
		writeNonEmptyAttribute(element, "name", unit.getName());
		writeIntegerAttribute(element, "id", unit.getID());
		writeImage(element, unit);
		if (unit instanceof HasPortrait) {
			writeNonEmptyAttribute(element, "portrait",
					((HasPortrait) unit).getPortrait());
		}
		final String orders = unit.getOrders().trim();
		element.appendChild(document.createTextNode(orders));
		for (final UnitMember member : unit) {
			writeSPObject(document, element, member);
		}
		parent.appendChild(element);
	}
	/**
	 * Write a fortress to XML.
	 *
	 * @param obj     The object to write. Must be a Fortress
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeFortress(final Document document, final Node parent,
							   Object obj) {
		if (!(obj instanceof Fortress)) {
			throw new IllegalArgumentException("Can only write Fortress");
		}
		final Fortress fort = (Fortress) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "fortress");
		writeIntegerAttribute(element, "owner", fort.getOwner().getPlayerId());
		writeNonEmptyAttribute(element, "name", fort.getName());
		writeIntegerAttribute(element, "id", fort.getID());
		writeImage(element, fort);
		writeNonEmptyAttribute(element, "portrait", fort.getPortrait());
		//noinspection unchecked: checked as first operation of method
		for (final FortressMember unit : (Iterable<FortressMember>) obj) {
			writeSPObject(document, element, unit);
		}
		parent.appendChild(element);
	}
	/**
	 * Write a map to XML.
	 * @param obj the map to write. Must be an IMapNG.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private void writeMap(final Document document, final Node parent,
						  Object obj) {
		if (!(obj instanceof IMapNG)) {
			throw new IllegalArgumentException("Can only write IMapNG");
		}
		final IMapNG map = (IMapNG) obj;
		final Element viewElement = document.createElementNS(ISPReader.NAMESPACE, "view");
		writeIntegerAttribute(viewElement, "current_player",
				map.getCurrentPlayer().getPlayerId());
		writeIntegerAttribute(viewElement, "current_turn", map.getCurrentTurn());
		final Element mapElement = document.createElementNS(ISPReader.NAMESPACE, "map");
		final MapDimensions dim = map.dimensions();
		writeIntegerAttribute(mapElement, "version", dim.version);
		writeIntegerAttribute(mapElement, "rows", dim.rows);
		writeIntegerAttribute(mapElement, "columns", dim.cols);
		for (final Player player : map.players()) {
			writeSPObject(document, mapElement, player);
		}
		for (int i = 0; i < dim.rows; i++) {
			boolean rowEmpty = true;
			final Element rowElement =
					document.createElementNS(ISPReader.NAMESPACE, "row");
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
						writeIntegerAttribute(rowElement, "index", i);
					}
					final Element element =
							document.createElementNS(ISPReader.NAMESPACE, "tile");
					writeIntegerAttribute(element, "row", i);
					writeIntegerAttribute(element, "column", j);
					if (TileType.NotVisible != terrain) {
						writeAttribute(element, "kind", terrain.toXML());
					}
					if (map.isMountainous(point)) {
						element.appendChild(document.createElementNS(ISPReader.NAMESPACE,
								"mountain"));
					}
					for (final River river : map.getRivers(point)) {
						writeSPObject(document, element, river);
					}
					final Ground ground = map.getGround(point);
					if (ground != null) {
						writeSPObject(document, element, ground);
					}
					final Forest forest = map.getForest(point);
					if (forest != null) {
						writeSPObject(document, element, forest);
					}
					for (final TileFixture fixture : map.getOtherFixtures(point)) {
						writeSPObject(document, element, fixture);
					}
					rowElement.appendChild(element);
				}
			}
			if (!rowEmpty) {
				mapElement.appendChild(rowElement);
			}
		}
		viewElement.appendChild(mapElement);
		parent.appendChild(viewElement);
	}

	/**
	 * Write a player to XML. This is here because it's not a good fit for any of
	 * the other classes that collect methods.
	 *
	 * @param obj     The object to write. Must be a Player.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	private static void writePlayer(final Document document, final Node parent,
									Object obj) {
		if (!(obj instanceof Player)) {
			throw new IllegalArgumentException("Can only write Player");
		}
		final Player player = (Player) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "player");
		writeIntegerAttribute(element, "number", player.getPlayerId());
		writeAttribute(element, "code_name", player.getName());
		parent.appendChild(element);
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SPFluidWriter";
	}
}
