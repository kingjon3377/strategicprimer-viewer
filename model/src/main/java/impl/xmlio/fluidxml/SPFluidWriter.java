package impl.xmlio.fluidxml;

import org.javatuples.Pair;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import common.map.HasKind;
import common.map.HasImage;
import common.map.Player;
import common.map.MapDimensions;
import common.map.IFixture;
import common.map.Point;
import common.map.River;
import common.map.TileType;
import common.map.IMapNG;
import common.map.TileFixture;
import common.map.Direction;

import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.TextFixture;
import common.map.fixtures.Ground;
import common.map.fixtures.UnitMember;
import common.map.fixtures.FortressMember;

import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Cave;

import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.SimpleImmortal;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.ImmortalAnimal;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.ISkill;

import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;

import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;

import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.CommunityStats;

import impl.xmlio.SPWriter;

import lovelace.util.TypeStream;
import lovelace.util.MalformedXMLException;

import static impl.xmlio.fluidxml.FluidBase.*;

import java.util.Map;
import java.util.regex.Pattern;
import lovelace.util.ThrowingConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;

/**
 * The main writer-to-XML class in the "fluid XML" implementation.
 */
public class SPFluidWriter implements SPWriter {
	// Note Ceylon regex was 'global', matching all rather than just the first
	private static final Pattern SNUG_END_TAG = Pattern.compile("([^ ])/>");

	private static int currentTurn = -1;

	private void writeSPObjectImpl(final XMLStreamWriter ostream, final Object obj, final int indentation)
			throws MalformedXMLException {
		for (Class<?> type : new TypeStream(obj)) {
			if (writers.containsKey(type)) {
				writers.get(type).writeCasting(ostream, obj, indentation);
				return;
			}
		}
		throw new IllegalStateException("No writer present for " + obj.getClass().getName());
	}

	@Override
	public void writeSPObject(final ThrowingConsumer<String, IOException> ostream, final Object obj)
			throws MalformedXMLException, IOException {
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		StringWriter writer = new StringWriter();
		try {
			XMLStreamWriter xsw = xof.createXMLStreamWriter(writer);
			xsw.setDefaultNamespace(SP_NAMESPACE);
			writeSPObjectImpl(xsw, obj, 0);
			xsw.writeEndDocument();
			xsw.flush();
			xsw.close();
		} catch (final XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
		for (String line : writer.toString().split(System.lineSeparator())) {
			ostream.accept(SNUG_END_TAG.matcher(line).replaceAll("$1 />"));
			ostream.accept(System.lineSeparator());
		}
	}

	@Override
	public void writeSPObject(final Path file, final Object obj) throws MalformedXMLException, IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			writeSPObject(writer::write, obj);
		}
	}

	@Override
	public void write(final Path arg, final IMapNG map) throws MalformedXMLException, IOException {
		writeSPObject(arg, map);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> arg, final IMapNG map) throws MalformedXMLException, IOException {
		writeSPObject(arg, map);
	}

	/**
	 * TODO: Does this really need to be an instance (non-static) method?
	 */
	private void writePlayer(final XMLStreamWriter ostream, final Player obj, final int indentation)
			throws MalformedXMLException {
		if (!obj.getName().isEmpty()) {
			writeTag(ostream, "player", indentation, true);
			writeAttributes(ostream, Pair.with("number", obj.getPlayerId()),
				Pair.with("code_name", obj.getName()));
			writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
			if (obj.getCountry() != null) {
				writeNonEmptyAttributes(ostream, Pair.with("country", obj.getCountry()));
			}
		}
	}

	private static class SimpleFixtureWriter<Type> implements FluidXMLWriter {
		public SimpleFixtureWriter(final Class<Type> cls, final String tag) {
			this.cls = cls;
			this.tag = tag;
		}

		private final Class<Type> cls;
		private final String tag;

		@Override
		public void write(final XMLStreamWriter ostream, final Object obj, final int indentation)
				throws MalformedXMLException {
			if (!cls.isInstance(obj)) {
				throw new IllegalArgumentException("Can only write " + cls.getName());
			} else if (!(obj instanceof IFixture)) {
				throw new IllegalArgumentException("Can only \"simply\" write fixtures.");
			}
			writeTag(ostream, tag, indentation, true);
			if (obj instanceof HasKind) {
				writeAttributes(ostream, Pair.with("kind", ((HasKind) obj).getKind()));
			}
			writeAttributes(ostream, Pair.with("id", ((IFixture) obj).getId()));
			if (obj instanceof HasImage) {
				writeImage(ostream, (HasImage) obj);
			}
		}

		public Class<Type> getType() {
			return cls;
		}
	}

	private void writeUnitOrders(final XMLStreamWriter ostream, final int indentation, final int turn,
	                             final String tag, final String text) throws MalformedXMLException {
		// assert (tag == "orders" || tag == "results");
		if (text.isEmpty()) {
			return;
		}
		writeTag(ostream, tag, indentation, false);
		if (turn >= 0) {
			writeAttributes(ostream, Pair.with("turn", turn));
		}
		try {
			ostream.writeCharacters(text);
			ostream.writeEndElement();
		} catch (final XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
	}

	private void writeUnit(final XMLStreamWriter ostream, final IUnit obj, final int indentation)
			throws MalformedXMLException {
		boolean empty = obj.isEmpty() &&
			obj.getAllOrders().values().stream().allMatch(String::isEmpty) &&
			obj.getAllResults().values().stream().allMatch(String::isEmpty);
		writeTag(ostream, "unit", indentation, empty);
		writeAttributes(ostream, Pair.with("owner", obj.getOwner().getPlayerId()));
		writeNonEmptyAttributes(ostream, Pair.with("kind", obj.getKind()),
			Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		for (Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
			writeUnitOrders(ostream, indentation + 1, entry.getKey(), "orders",
				entry.getValue().trim());
		}
		for (Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
			writeUnitOrders(ostream, indentation + 1, entry.getKey(), "results",
				entry.getValue().trim());
		}
		for (UnitMember member : obj) {
			writeSPObjectImpl(ostream, member, indentation + 1);
		}
		if (!empty) {
			indent(ostream, indentation);
			try {
				ostream.writeEndElement();
			} catch (final XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
	}

	private void writeFortress(final XMLStreamWriter ostream, final IFortress obj, final int indentation)
			throws MalformedXMLException {
		writeTag(ostream, "fortress", indentation, false);
		writeAttributes(ostream, Pair.with("owner", obj.getOwner().getPlayerId()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		if (!TownSize.Small.equals(obj.getTownSize())) {
			writeAttributes(ostream, Pair.with("size", obj.getTownSize().toString()));
		}
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (obj.iterator().hasNext()) {
			for (FortressMember member : obj) {
				writeSPObjectImpl(ostream, member, indentation + 1);
			}
			indent(ostream, indentation);
		}
		try {
			ostream.writeEndElement();
		} catch (final XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
	}

	private void writeMap(final XMLStreamWriter ostream, final IMapNG obj, final int indentation)
			throws MalformedXMLException {
		writeTag(ostream, "view", indentation, false);
		writeAttributes(ostream, Pair.with("current_player", obj.getCurrentPlayer().getPlayerId()),
			Pair.with("current_turn", obj.getCurrentTurn()));
		currentTurn = obj.getCurrentTurn();
		writeTag(ostream, "map", indentation + 1, false);
		MapDimensions dimensions = obj.getDimensions();
		writeAttributes(ostream, Pair.with("version", dimensions.getVersion()),
			Pair.with("rows", dimensions.getRows()),
			Pair.with("columns", dimensions.getColumns()));
		for (Player player : obj.getPlayers()) {
			writePlayer(ostream, player, indentation + 2);
		}
		for (int i = 0; i < dimensions.getRows(); i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dimensions.getColumns(); j++) {
				Point loc = new Point(i, j);
				TileType terrain = obj.getBaseTerrain(loc);
				if (!obj.isLocationEmpty(loc)) {
					if (rowEmpty) {
						writeTag(ostream, "row", indentation + 2, false);
						rowEmpty = false;
						writeAttributes(ostream, Pair.with("index", i));
					}
					writeTag(ostream, "tile", indentation + 3, false);
					writeAttributes(ostream, Pair.with("row", i),
						Pair.with("column", j));
					if (terrain != null) {
						writeAttributes(ostream, Pair.with("kind",
							terrain.getXml()));
					}
					boolean anyContents = false;
					for (Player bookmarkPlayer : obj.getAllBookmarks(loc)) {
						anyContents = true;
						writeTag(ostream, "bookmark", indentation + 4, true);
						writeAttributes(ostream,
							Pair.with("player", bookmarkPlayer.getPlayerId()));
					}
					if (obj.isMountainous(loc)) {
						anyContents = true;
						writeTag(ostream, "mountain", indentation + 4, true);
					}
					// Ceylon code made a point of sorting the rivers; that's hard
					// to do here, and I think in Java we use an EnumSet, which should
					// be sorted anyway
					for (River river : obj.getRivers(loc)) {
						anyContents = true;
						writeSPObjectImpl(ostream, river, indentation + 4);
					}
					// Similarly, roads are in an EnumMap, which is automatically sorted
					for (Map.Entry<Direction, Integer> entry :
							obj.getRoads(loc).entrySet()) {
						writeTag(ostream, "road", indentation + 4, true);
						writeAttributes(ostream,
							Pair.with("direction", entry.getKey().toString()),
							Pair.with("quality", entry.getValue()));
					}
					// To avoid breaking map-format-conversion tests, and to
					// avoid churn in existing maps, put the first Ground and Forest
					// before other fixtures.
					Ground ground = obj.getFixtures(loc).stream()
						.filter(Ground.class::isInstance).map(Ground.class::cast)
						.findFirst().orElse(null);
					if (ground != null) {
						anyContents = true;
						writeSPObjectImpl(ostream, ground, indentation + 4);
					}
					Forest forest = obj.getFixtures(loc).stream()
						.filter(Forest.class::isInstance).map(Forest.class::cast)
						.findFirst().orElse(null);
					if (forest != null) {
						anyContents = true;
						writeSPObjectImpl(ostream, forest, indentation + 4);
					}
					for (TileFixture fixture : obj.getFixtures(loc)) {
						if (fixture == forest || fixture == ground) {
							continue;
						}
						anyContents = true;
						writeSPObjectImpl(ostream, fixture, indentation + 4);
					}
					if (anyContents) {
						indent(ostream, indentation + 3);
					}
					try {
						ostream.writeEndElement();
					} catch (final XMLStreamException except) {
						throw new MalformedXMLException(except);
					}
				}
			}
			if (!rowEmpty) {
				indent(ostream, indentation + 2);
				try {
					ostream.writeEndElement();
				} catch (final XMLStreamException except) {
					throw new MalformedXMLException(except);
				}
			}
		}
		if (obj.streamLocations().filter(((Predicate<Point>) Point::isValid).negate())
				.map(obj::getFixtures).anyMatch(((Predicate<Collection<TileFixture>>)
					Collection::isEmpty).negate())) {
			writeTag(ostream, "elsewhere", indentation +2, false);
			for (TileFixture fixture : obj.streamLocations()
					.filter(((Predicate<Point>) Point::isValid).negate())
					.flatMap(p -> obj.getFixtures(p).stream())
					.collect(Collectors.toList())) {
				writeSPObjectImpl(ostream, fixture, indentation + 3);
			}
			indent(ostream, indentation + 2);
			try {
				ostream.writeEndElement();
			} catch (final XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
		indent(ostream, indentation + 1);
		try {
			ostream.writeEndElement();
			indent(ostream, indentation);
			ostream.writeEndElement();
		} catch (final XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
	}

	private final Map<Class<?>, FluidXMLWriter<?>> writers;

	private static <Type> void addWriterToMap(final Map<Class<?>, FluidXMLWriter<?>> map,
	                                          final Class<Type> cls, final FluidXMLWriter<Type> writer) {
		map.put(cls, writer);
	}

	public SPFluidWriter() {
		Map<Class<?>, FluidXMLWriter<?>> temp = new HashMap<>();
		addWriterToMap(temp, River.class, FluidTerrainHandler::writeRiver);
		addWriterToMap(temp, AdventureFixture.class, FluidExplorableHandler::writeAdventure);
		addWriterToMap(temp, Portal.class, FluidExplorableHandler::writePortal);
		addWriterToMap(temp, Battlefield.class, FluidExplorableHandler::writeBattlefield);
		addWriterToMap(temp, Cave.class, FluidExplorableHandler::writeCave);
		addWriterToMap(temp, Ground.class, FluidTerrainHandler::writeGround);
		addWriterToMap(temp, Forest.class, FluidTerrainHandler::writeForest);
		addWriterToMap(temp, Animal.class, UnitMemberHandler::writeAnimal);
		addWriterToMap(temp, AnimalTracks.class, UnitMemberHandler::writeAnimalTracks);
		for (SimpleFixtureWriter writer : Arrays.asList(
				new SimpleFixtureWriter<>(Hill.class, "hill"),
				new SimpleFixtureWriter<>(Oasis.class, "oasis"),
				new SimpleFixtureWriter<>(Centaur.class, "centaur"),
				new SimpleFixtureWriter<>(Dragon.class, "dragon"),
				new SimpleFixtureWriter<>(Fairy.class, "fairy"),
				new SimpleFixtureWriter<>(Giant.class, "giant"))) {
			temp.put(writer.getType(), writer);
		}
		addWriterToMap(temp, SimpleImmortal.class, UnitMemberHandler::writeSimpleImmortal);
		addWriterToMap(temp, ImmortalAnimal.class, UnitMemberHandler::writeSimpleImmortal);
		addWriterToMap(temp, TextFixture.class, FluidExplorableHandler::writeTextFixture);
		addWriterToMap(temp, Implement.class, FluidResourceHandler::writeImplement);
		addWriterToMap(temp, IResourcePile.class, FluidResourceHandler::writeResource);
		addWriterToMap(temp, CacheFixture.class, FluidResourceHandler::writeCache);
		addWriterToMap(temp, Meadow.class, FluidResourceHandler::writeMeadow);
		addWriterToMap(temp, Grove.class, FluidResourceHandler::writeGrove);
		addWriterToMap(temp, Mine.class, FluidResourceHandler::writeMine);
		addWriterToMap(temp, MineralVein.class, FluidResourceHandler::writeMineral);
		addWriterToMap(temp, Shrub.class, FluidResourceHandler::writeShrub);
		addWriterToMap(temp, StoneDeposit.class, FluidResourceHandler::writeStone);
		addWriterToMap(temp, IWorker.class, UnitMemberHandler::writeWorker);
		addWriterToMap(temp, IJob.class, UnitMemberHandler::writeJob);
		addWriterToMap(temp, ISkill.class, UnitMemberHandler::writeSkill);
		addWriterToMap(temp, WorkerStats.class, UnitMemberHandler::writeStats);
		addWriterToMap(temp, IUnit.class, this::writeUnit);
		addWriterToMap(temp, IFortress.class, this::writeFortress);
		addWriterToMap(temp, Village.class, FluidTownHandler::writeVillage);
		addWriterToMap(temp, AbstractTown.class, FluidTownHandler::writeTown);
		addWriterToMap(temp, IMapNG.class, this::writeMap);
		addWriterToMap(temp, Player.class, this::writePlayer);
		addWriterToMap(temp, CommunityStats.class, FluidTownHandler::writeCommunityStats);
		writers = Collections.unmodifiableMap(temp);
	}
}
