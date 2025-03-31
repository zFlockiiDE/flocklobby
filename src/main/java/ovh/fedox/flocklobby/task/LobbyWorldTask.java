package ovh.fedox.flocklobby.task;


import org.bukkit.Location;
import org.mineacademy.fo.model.SimpleRunnable;
import ovh.fedox.flocklobby.settings.Setting;
import ovh.fedox.flocklobby.util.LocationUtil;

/**
 * LobbyWorldTask.java -
 * <p>
 * Created on 3/31/2025 at 1:51 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public class LobbyWorldTask extends SimpleRunnable {

	@Override
	public void run() {
		String settingLoc = Setting.SPAWN_LOCATION;
		Location loc = LocationUtil.stringToLocation(settingLoc);

		loc.getWorld().setTime(0);
		loc.getWorld().setStorm(false);
		loc.getWorld().setThundering(false);
	}
}
