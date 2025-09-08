package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

/* package */ final class FishingApplet extends HuntGeneralApplet {
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
	public @Nullable String run() {
		return impl("fish", huntingModel::fish);
	}
}
