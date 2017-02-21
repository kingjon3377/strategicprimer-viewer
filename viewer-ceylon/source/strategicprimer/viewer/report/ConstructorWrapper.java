package strategicprimer.viewer.report;

import controller.map.report.tabular.CropTabularReportGenerator;
import controller.map.report.tabular.DiggableTabularReportGenerator;
import controller.map.report.tabular.FortressTabularReportGenerator;
import controller.map.report.tabular.WorkerTabularReportGenerator;
import model.map.Player;
import model.map.Point;

/**
 * TODO: explain this class
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @deprecated this is a hack
 * @author Jonathan Lovelace
 */
@Deprecated
public class ConstructorWrapper {
	/**
	 * Ceylon compiler thinks that FortressTabularReportGenerator doesn't have a public
	 * constructor.
	 */
	public static FortressTabularReportGenerator fortressTabularReportGenerator(
			final Player player, final Point hq) {
		return new FortressTabularReportGenerator(player, hq);
	}
	/**
	 * Ceylon compiler thinks that WorkerTabularReportGenerator doesn't have a public
	 * constructor.
	 */
	public static WorkerTabularReportGenerator workerTabularReportGenerator(
			final Point hq) {
		return new WorkerTabularReportGenerator(hq);
	}
	/**
	 * Ceylon compiler thinks that CropTabularReportGenerator doesn't have a public
	 * constructor.
	 */
	public static CropTabularReportGenerator cropTabularReportGenerator(final Point hq) {
		return new CropTabularReportGenerator(hq);
	}
	/**
	 * Ceylon compiler thinks that DiggableTabularReportGenerator doesn't have a public
	 * constructor.
	 */
	public static DiggableTabularReportGenerator diggableTabularReportGenerator(
			final Point hq) {
		return new DiggableTabularReportGenerator(hq);
	}
}
