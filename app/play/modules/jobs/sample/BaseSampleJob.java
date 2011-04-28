package play.modules.jobs.sample;

import play.Logger;
import play.jobs.Job;

public class BaseSampleJob extends Job<String> {

	/**
	 * Gets data to be loaded, loop on each one and publish them to RabbitMQ
	 * 
	 * @see play.jobs.Job#doJob()
	 */
	@Override
	public void doJob() {
		Logger.info("SampleJob.doJob() called on: %s", this.getClass());
	}

}
