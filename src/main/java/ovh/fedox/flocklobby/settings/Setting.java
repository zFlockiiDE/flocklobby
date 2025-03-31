package ovh.fedox.flocklobby.settings;


import org.mineacademy.fo.settings.SimpleSettings;

/**
 * Setting.java - Generic settings
 * <p>
 * Created on 3/31/2025 at 7:19 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public class Setting extends SimpleSettings {

	public static String SPAWN_LOCATION;
	public static String FLOCK_LEADERBOARD_LOCATION;
	public static Double MIN_Y;

	/**
	 * Initialize the settings
	 */
	private static void init() {
		setPathPrefix(null);

		SPAWN_LOCATION = getString("Spawn_Location");
		FLOCK_LEADERBOARD_LOCATION = getString("Flock_Leaderboard_Location");
		MIN_Y = getDouble("Min_Y");
	}

}
