package ovh.fedox.flocklobby.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import ovh.fedox.flockapi.FlockAPI;
import ovh.fedox.flockapi.database.model.ApiPlayer;
import ovh.fedox.flocklobby.settings.Setting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FlockLeaderboard.java - Utility for creating and managing flock leaderboards
 * <p>
 * Created on 3/31/2025 at 9:14 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@UtilityClass
public class FlockLeaderboard {

	private static final String LEADERBOARD_NAME = "flock_leaderboard";
	private static final String[] CIRCLE_NUMBERS = {"➊", "➋", "➌", "➍", "➎", "➏", "➐", "➑", "➒", "➓"};

	/**
	 * Create the leaderboard
	 */
	public static void createLeaderboard() {
		String name = LEADERBOARD_NAME;
		Location location = LocationUtil.stringToLocation(Setting.FLOCK_LEADERBOARD_LOCATION);
		if (DHAPI.getHologram(name) != null) {
			DHAPI.updateHologram(name);
			return;
		}

		DHAPI.createHologram(name, location, getLines());
	}


	/**
	 * Update the leaderboard
	 */
	public static void updateLeaderboard() {
		if (DHAPI.getHologram(LEADERBOARD_NAME) != null) {
			List<String> updatedLines = getLines();

			DHAPI.setHologramLines(Hologram.getCachedHologram(LEADERBOARD_NAME), updatedLines);

			DHAPI.getHologram(LEADERBOARD_NAME).updateAll();
		}
	}

	/**
	 * Get the lines for the leaderboard
	 *
	 * @return List of formatted lines
	 */
	private static List<String> getLines() {
		List<String> lines = new ArrayList<>();

		lines.add("#ICON: PLAYER_HEAD (e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU1Y2JmNTg0OTI4MDE2MDdiZmJiMzNjYzQ5MWZlMDhkMDhjZDdmZTk1YTBkNTc4NTVhZGY2M2EwNGVhZWExZSJ9fX0=)");
		lines.add("#12c742§l★ TOP FLOCKS ★");
		lines.add("&7");

		List<ApiPlayer> topPlayers = getTopPlayers(10);

		for (int i = 0; i < 10; i++) {
			if (i < topPlayers.size()) {
				ApiPlayer player = topPlayers.get(i);
				lines.add(String.format("#12c742§l%s #68ed8c%s §f→ #e0e0e0%d",
						CIRCLE_NUMBERS[i],
						player.getName(),
						(int) Math.round(player.getFlocks())));
			} else {
				lines.add(String.format("#12c742§l%s #68ed8c--- §f→ #e0e0e0---", CIRCLE_NUMBERS[i]));
			}
		}

		if (!topPlayers.isEmpty()) {
			lines.add("&f");
			lines.add(String.format("#12c742§l▶ #68ed8c%s §f→ #e0e0e0%s #12c742§l◀", "%flocklobby_flock_name%", "%flocklobby_flock_count%"));
		} else {
			lines.add("#12c742§l▶ Keine Spieler gefunden §f→ #e0e0e00 #12c742§l◀");
		}

		return lines;
	}

	/**
	 * Get the top players sorted by flocks
	 *
	 * @param limit Maximum number of players to return
	 * @return List of top players
	 */
	private static List<ApiPlayer> getTopPlayers(int limit) {
		List<ApiPlayer> allPlayers = FlockAPI.getMongoManager().getApiPlayerRepository().findAll();

		return allPlayers.stream()
				.sorted(Comparator.comparingDouble(ApiPlayer::getFlocks).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	/**
	 * Delete the leaderboard
	 */
	public static void deleteLeaderboard() {
		if (DHAPI.getHologram(LEADERBOARD_NAME) != null) {
			DHAPI.removeHologram(LEADERBOARD_NAME);
		}
	}
}

