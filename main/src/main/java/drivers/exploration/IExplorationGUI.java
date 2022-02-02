package drivers.exploration;

import drivers.common.MultiMapGUIDriver;
import exploration.common.IExplorationModel;

interface IExplorationGUI extends MultiMapGUIDriver {
	@Override
	IExplorationModel getModel();
}
