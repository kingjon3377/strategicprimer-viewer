package drivers.turnrunning.applets;

import drivers.turnrunning.ITurnRunningModel;
import drivers.common.cli.ICLIHelper;
import common.idreg.IDRegistrar;

public interface TurnAppletFactory {
	TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf);
}
