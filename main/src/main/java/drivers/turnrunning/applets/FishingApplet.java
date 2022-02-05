package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/* package */ class FishingApplet extends HuntGeneralApplet {
	public FishingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super("try to catch and process", model, cli, idf);
	}

	@Override
	public String getDescription() {
		return "search for aquatic animals";
	}

	@Override
	public List<String> getCommands() {
		return Collections.singletonList("fish");
	}

	@Override
	@Nullable
	public String run() {
		return impl("fish", huntingModel::fish);
	}
}
