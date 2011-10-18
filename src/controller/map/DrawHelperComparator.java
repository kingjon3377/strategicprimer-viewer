package controller.map;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.viewer.SPMap;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.MapComponent;
import view.map.main.TileDrawHelper;
import view.util.SystemOut;
import controller.map.simplexml.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A driver to compare the performance of TileDrawHelpers.
 * @author Jonathan Lovelace
 *
 */
public class DrawHelperComparator {
	/**
	 * A driver method to compare the two helpers, and the two map-GUI implementations.
	 * @param args the command-line arguments.
	 */
	public static void main(final String[] args) { // NOPMD
		final Logger logger = Logger.getLogger(DrawHelperComparator.class.getName());
		// ESCA-JAVA0177:
		final SPMap map; // NOPMD
		try {
			map = new SimpleXMLReader().readMap(args[0]);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "I/O error reading map", e);
			return; // NOPMD
		} catch (XMLStreamException e) {
			logger.log(Level.SEVERE, "XML error reading map", e);
			return; // NOPMD
		} catch (SPFormatException e) {
			logger.log(Level.SEVERE, "Map format error reading map", e);
			return;
		}
		final TileDrawHelper helperOne = new CachingTileDrawHelper();
		final TileDrawHelper helperTwo = new DirectTileDrawHelper();
		BufferedImage image = new BufferedImage(MapComponent.getTileSize(), MapComponent.getTileSize(), BufferedImage.TYPE_INT_RGB);
		final int reps = 50; // NOPMD
		long start, end;
		Graphics pen;
		SystemOut.SYS_OUT.println("1. All in one place:");
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(image.createGraphics(),
							map.getTile(i, j), MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Caching:");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(image.createGraphics(),
							map.getTile(i, j), MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Direct: ");
		printStats(end - start, reps);
		SystemOut.SYS_OUT.println("2. Translating:");
		image = new BufferedImage(MapComponent.getTileSize() * map.cols(),
				MapComponent.getTileSize() * map.rows(),
				BufferedImage.TYPE_INT_RGB);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(image.createGraphics(),
							map.getTile(i, j), i * MapComponent.getTileSize(),
							j * MapComponent.getTileSize(),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Caching: ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(image.createGraphics(),
							map.getTile(i, j), i * MapComponent.getTileSize(),
							j * MapComponent.getTileSize(),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Direct: ");
		printStats(end - start, reps);
		SystemOut.SYS_OUT.println("3. In-place, reusing Graphics:");
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(pen, map.getTile(i, j),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Caching: ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(pen, map.getTile(i, j),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Direct: ");
		printStats(end - start, reps);
		SystemOut.SYS_OUT.println("4. Translating, reusing Graphics:");
		image = new BufferedImage(MapComponent.getTileSize() * map.cols(),
				MapComponent.getTileSize() * map.rows(),
				BufferedImage.TYPE_INT_RGB);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(pen, map.getTile(i, j),
							i * MapComponent.getTileSize(),
							j * MapComponent.getTileSize(),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Caching: ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(pen, map.getTile(i, j),
							i * MapComponent.getTileSize(),
							j * MapComponent.getTileSize(),
							MapComponent.getTileSize(),
							MapComponent.getTileSize());
				}
			}
		}
		end = System.nanoTime();
		SystemOut.SYS_OUT.print("Direct: ");
		printStats(end - start, reps);
	}
	/**
	 * A helper method to reduce repeated strings.
	 * @param total the total time
	 * @param reps how many reps there were
	 */
	private static void printStats(final long total, final int reps) {
		SystemOut.SYS_OUT.print('\t');
		SystemOut.SYS_OUT.print(total);
		SystemOut.SYS_OUT.print(", average of\t");
		SystemOut.SYS_OUT.print(Long.toString(total / reps));
		SystemOut.SYS_OUT.println(" ns.");
	}
}
