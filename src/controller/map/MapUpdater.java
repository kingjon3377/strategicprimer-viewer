package controller.map;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.xml.stream.XMLStreamException;

import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A driver to update derived maps (such as players' maps) from a master map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapUpdater {
	/**
	 * The master map
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
		for (int i = 0; i < master.rows(); i++) {
			for (int j = 0; j < master.cols(); j++) {
				if (compareTiles(derived.getTile(i, j), master.getTile(i, j))) {
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
	public boolean compareTiles(final Tile masterTile, final Tile tile) {
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
		if (args.length < 2) {
			throw new IllegalArgumentException("Not enough arguments");
		}
		final MapReader reader = new MapReader();
		final MapUpdater updater;
		try {
			updater = new MapUpdater(reader.readMap(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println("File " + args[0] + " not found");
			System.exit(1);
			return;
		} catch (XMLStreamException e) {
			System.err.println("XML stream error parsing " + args[0]);
			System.exit(2);
			return;
		}
		final SPMap derived;
		try {
			derived = reader.readMap(args[1]);
		} catch (FileNotFoundException e) {
			System.err.println("File " + args[1] + " not found");
			System.exit(3);
			return;
		} catch (XMLStreamException e) {
			System.err.println("XML stream error parsing " + args[1]);
			System.exit(4);
			return;
		}
		updater.update(derived);
		new XMLWriter(new PrintWriter(System.out)).write(derived);
	}
}
