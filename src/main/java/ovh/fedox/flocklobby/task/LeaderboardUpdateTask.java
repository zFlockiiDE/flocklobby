package ovh.fedox.flocklobby.task;

import org.mineacademy.fo.model.SimpleRunnable;
import ovh.fedox.flocklobby.util.FlockLeaderboard;

/**
 * LeaderboardUpdateTask.java - Task to update the leaderboard periodically
 * <p>
 * Created on 3/31/2025 by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */
public class LeaderboardUpdateTask extends SimpleRunnable {

	/**
	 * Update the leaderboard
	 */
	@Override
	public void run() {
		FlockLeaderboard.updateLeaderboard();
	}
}