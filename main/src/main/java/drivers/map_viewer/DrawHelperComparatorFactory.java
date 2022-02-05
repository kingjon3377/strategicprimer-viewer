package drivers.map_viewer;

import com.google.auto.service.AutoService;
import java.awt.Image;
import java.awt.Graphics;

import java.awt.image.BufferedImage;

import drivers.map_viewer.Ver2TileDrawHelper;

import drivers.common.SPOptions;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.IncorrectUsageException;
import drivers.common.FixtureMatcher;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import common.map.MapDimensions;
import common.map.TileFixture;
import common.map.Point;
import common.map.IMapNG;

import impl.xmlio.MapIOHelper;

import common.xmlio.Warning;

import java.util.Map;
import java.util.HashMap;

import java.nio.file.Path;
import java.nio.file.Paths;

import lovelace.util.Accumulator;

/**
 * A factory for a driver to compare the performance of TileDrawHelpers.
 */
@AutoService(DriverFactory.class)
public class DrawHelperComparatorFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(true, "drawing-performance", ParamCount.AtLeastOne,
		"Test drawing performance.",
		"Test the performance of the TileDrawHelper classes---which do the heavy lifting of rendering the map in the viewer---using a variety of automated tests.",
		true, false, "--report=out.csv");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new DrawHelperComparator(cli, options);
	}
}
