package drivers;

import drivers.common.DriverFactory;
import drivers.common.IDriverUsage;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.UtilityDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;

import java.awt.image.BufferedImage;

import java.util.Map;
import java.util.HashMap;

import drivers.common.cli.ICLIHelper;

import lovelace.util.ResourceInputStream;

import javax.imageio.ImageIO;

import lovelace.util.EnumCounter;

import common.map.TileType;
import common.map.Point;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.MapDimensionsImpl;
import common.map.PlayerCollection;
import common.map.HasName;
import common.map.IMapNG;

import impl.xmlio.MapIOHelper;

import common.idreg.IDRegistrar;
import common.idreg.IDFactory;

import common.map.fixtures.terrain.Forest;

import exploration.common.SurroundingPointIterable;

import com.google.auto.service.AutoService;

/**
 * A factory for an app to let the user create a map from an image.
 */
@AutoService(DriverFactory.class)
public class ImporterFactory implements UtilityDriverFactory {
	private static final DriverUsage USAGE = new DriverUsage(false, "import",
		ParamCount.AtLeastOne, "Import terrain data from a raster image",
		"Import terrain data from a raster image", false, false, "/path/to/image.png",
		"/path/to/image.png", "--size=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new ImporterDriver(cli, options);
	}
}
