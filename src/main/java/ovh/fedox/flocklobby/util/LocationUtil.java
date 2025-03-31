package ovh.fedox.flocklobby.util;


import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * LocationUtil.java - Utilities for location related stuff
 * <p>
 * Created on 3/31/2025 at 7:23 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@UtilityClass
public class LocationUtil {

	/**
	 * Convert a location to a string
	 *
	 * @param string the location
	 * @return the string
	 */
	public static Location stringToLocation(String string) {
		String[] parts = string.split(", ");
		String world = parts[0];

		World bukkitWorld = Bukkit.getWorld(world);

		if (bukkitWorld == null) {
			throw new IllegalArgumentException("World " + world + " not found");
		}

		double x = Double.parseDouble(parts[1]);
		double y = Double.parseDouble(parts[2]);
		double z = Double.parseDouble(parts[3]);

		return new Location(bukkitWorld, x, y, z);
	}

}
