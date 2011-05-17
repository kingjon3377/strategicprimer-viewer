package controller.map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;
import controller.map.MapReader.MapVersionException;

/**
 * A driver to update derived maps (such as players' maps) from a master map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapUpdater {
	/**
	 * The master map.
	 */
	private final SPMap master;

	/**
	 * 
	 * @param masterMap
	 *            the master map
	 */
	public MapUpdater(final SPMap masterMap) {
		if (masterMap == null) {
			throw new IllegalArgumentException("Null map passed");
		}
		master = masterMap;
	}

	/**
	 * Update a derived map from the master.
	 * 
	 * @param derived
	 *            the derived map to update.
	 */
	public void update(final SPMap derived) {
		if (derived == null) {
			throw new IllegalArgumentException("Null map passed as derived.");
		} else if (master.rows() != derived.rows()
				|| master.cols() != derived.cols()) {
			throw new IllegalArgumentException("Map sizes don't match");
		}
		final int rows = master.rows();
		final int cols = master.cols();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (shouldUpdate(derived.getTile(i, j), master.getTile(i, j))) {
					derived.addTile(master.getTile(i, j));
				}
			}
		}
	}

	/**
	 * @param masterTile
	 *            a tile from the master map
	 * @param tile
	 *            the equivalent tile from a derived map
	 * @return whether the derived map needs updating in this tile
	 */
	public boolean shouldUpdate(final Tile masterTile, final Tile tile) {
		return tile != null && !TileType.NotVisible.equals(tile.getType())
				&& masterTile != null
				&& !TileType.NotVisible.equals(masterTile.getType())
				&& !tile.equals(masterTile);
	}

	/**
	 * Driver. We won't actually overwrite the dependent map with an update, but
	 * rather print the updated version to stdout.
	 * 
	 * @param args
	 *            Command-line arguments: master, then a map to update.
	 */
	public static void main(final String[] args) {
		final Logger logger = Logger.getLogger(MapUpdater.class.getName());
		if (args.length < 2) {
			throw new IllegalArgumentException("Not enough arguments");
		}
		final MapReader reader = new MapReader();
		// ESCA-JAVA0177:
		final MapUpdater updater; // NOPMD // $codepro.audit.disable localDeclaration
		try {
			updater = new MapUpdater(reader.readMap(args[0]));
		} catch (final FileNotFoundException e) {
			logger.log(Level.SEVERE, buildString("File ", args[0], " not found"), e);
			System.exit(1);
			return; // NOPMD
		} catch (final XMLStreamException e) {
			logger.log(Level.SEVERE, buildString("XML stream error parsing ", args[0]), e);
			System.exit(2);
			return; // NOPMD
		} catch (final IOException e) {
			logger.log(Level.SEVERE, buildString("I/O error processing ", args[0]), e);
			System.exit(5);
			return; // NOPMD
		} catch (MapVersionException e) {
			logger.log(Level.SEVERE, buildString(args[0], " map version too old"), e);
			System.exit(7);
			return; // NOPMD
		}
		// ESCA-JAVA0177:
		final SPMap derived; // NOPMD // $codepro.audit.disable localDeclaration
		try {
			derived = reader.readMap(args[1]);
		} catch (final FileNotFoundException e) {
			logger.log(Level.SEVERE, buildString("File ", args[1], " not found"), e);
			System.exit(3);
			return; // NOPMD
		} catch (final XMLStreamException e) {
			logger.log(Level.SEVERE, buildString("XML stream error parsing ", args[1]), e);
			System.exit(4);
			return;
		} catch (final IOException e) {
			logger.log(Level.SEVERE, buildString("I/O error processing ", args[1]), e);
			System.exit(6);
			return;
		} catch (MapVersionException e) {
			logger.log(Level.SEVERE, buildString(args[1], " map version too old"), e);
			System.exit(8);
			return; // NOPMD
		}
		updater.update(derived);
		// ESCA-JAVA0266:
		final PrintWriter writer = new PrintWriter(System.out);
		new XMLWriter(writer).write(derived);
		writer.close();
	}
	
	/**
	 * Build a string.
	 * @param strings the strings to concatenate.
	 * @return the result of the concatenation
	 */
	private static String buildString(final String... strings) {
		final StringBuilder build = new StringBuilder(16);
		for (String str : strings) {
			build.append(str);
		}
		return build.toString();
	}
}
