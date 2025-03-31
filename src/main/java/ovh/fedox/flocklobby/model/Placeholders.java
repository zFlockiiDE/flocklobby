package ovh.fedox.flocklobby.model;


import lombok.Generated;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.model.SimpleExpansion;
import ovh.fedox.flockapi.FlockAPI;

import java.util.HashMap;
import java.util.UUID;

/**
 * Placeholders.java - Placeholder expansion for FlockLobby
 * <p>
 * Created on 3/31/2025 at 9:01 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@AutoRegister
public final class Placeholders extends SimpleExpansion {
	private static final SimpleExpansion instance = new Placeholders();
	private static final HashMap<UUID, Double> cachedFlockCount = new HashMap<>();
	private static long lastUpdate = 0;

	/**
	 * Create a new expansion
	 */
	@Generated
	private Placeholders() {
	}

	/**
	 * Get the instance
	 *
	 * @return the instance
	 */
	@Generated
	public static SimpleExpansion getInstance() {
		return instance;
	}

	/**
	 * Return the plugin name
	 *
	 * @param sender     the player
	 * @param identifier everything after your plugin name such as if user types {flocklobby_flock_count},
	 *                   we return only "flock_count". You can also use {@link #args} here.
	 * @return the replacement
	 */
	@Override
	protected String onReplace(@NonNull CommandSender sender, String identifier) {

		final Player player = (Player) sender;

		if (identifier.equals("flock_count")) {
			if (System.currentTimeMillis() - lastUpdate > 1000 * 60 * 5) {
				lastUpdate = System.currentTimeMillis();

				cachedFlockCount.clear();

				FlockAPI.getMongoManager().getApiPlayerRepository().findAll().forEach(apiPlayer -> {
					cachedFlockCount.put(apiPlayer.getUuid(), apiPlayer.getFlocks());
				});
			}

			return formatFlockCount(cachedFlockCount.getOrDefault(player.getUniqueId(), 0.0));
		} else if (identifier.equalsIgnoreCase("flock_name")) {
			return player.getName();
		}

		return null;
	}

	/**
	 * Format the flock count
	 *
	 * @param count the count
	 * @return the formatted count
	 */
	private String formatFlockCount(double count) {
		return String.format("%,.0f", count);
	}
}
