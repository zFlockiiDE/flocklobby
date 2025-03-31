package ovh.fedox.flocklobby;


import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import ovh.fedox.flocklobby.util.FlockLeaderboard;

/**
 * FlockAPI.java - Main instance of the FlockAPI
 * <p>
 * Created on 3/31/2025 at 4:02 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public final class FlockLobby extends SimplePlugin {

	/**
	 * Get the instance of the FlockAPI
	 *
	 * @return the instance
	 */
	public static FlockLobby getInstance() {
		return (FlockLobby) SimplePlugin.getInstance();
	}

	/**
	 * Gets invoked when the plugin loads
	 */
	@Override
	protected void onReloadablesStart() {
		Common.setTellPrefix("&8&l➽ &a&lzFlockii.de &8&l•&7");
	}


	/**
	 * Gets invoked when the plugin starts
	 */
	@Override
	protected void onPluginStart() {
		Common.setTellPrefix("&8&l➽ &a&lzFlockii.de &8&l•&7");

		FlockLeaderboard.createLeaderboard();

		Common.runTimerAsync(20, 20 * 60 * 5, FlockLeaderboard::updateLeaderboard);
	}

	/**
	 * Gets invoked when the plugin stops
	 */
	@Override
	protected void onPluginStop() {
		FlockLeaderboard.deleteLeaderboard();
	}
}
