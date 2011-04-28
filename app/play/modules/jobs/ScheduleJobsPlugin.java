package play.modules.jobs;

import play.PlayPlugin;
import play.mvc.Router;

public class ScheduleJobsPlugin extends PlayPlugin {

	/**
	 * On application start.
	 */
	@Override
	public void onApplicationStart() {
		Router.addRoute("GET", "/@jobs", "Jobs.index");
		Router.addRoute("GET", "/@jobs/executeNow", "Jobs.executeNow");
	}

}
