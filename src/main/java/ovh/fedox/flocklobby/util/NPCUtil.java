package ovh.fedox.flocklobby.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import ovh.fedox.flockapi.database.RedisManager;
import ovh.fedox.flocklobby.model.NPC;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NPCUtil.java - NPC Utils for the lobby system
 * <p>
 * Created on 3/31/2025 at 3:44 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@UtilityClass
public class NPCUtil {

	@Getter
	private static final List<NPC> npcs = new ArrayList<>();

	@Getter
	private static final Map<String, Hologram> holograms = new HashMap<>();
	private static final int spawnDelay = 20;
	private static BukkitTask updateTask;
	private static Plugin plugin;

	/**
	 * Initialize the NPC system and register plugin messaging
	 *
	 * @param pluginInstance The plugin instance
	 */
	public static void initialize(Plugin pluginInstance) {
		plugin = pluginInstance;
		startUpdateTask();
	}

	public static void addNPC(String object) {
		object = object.replace("{", "").replace("}", "");

		String[] parts = object.split(", ");

		String server = parts[0].split("=")[1];
		String prettyName = parts[1].split("=")[1];
		String skin = parts[2].split("=")[1];

		String worldName = parts[3].split("=")[1];
		double x = Double.parseDouble(parts[4]);
		double y = Double.parseDouble(parts[5]);
		double z = Double.parseDouble(parts[6]);

		Location location = new Location(Bukkit.getWorld(worldName), x, y, z);

		NPC npc = new NPC(server, prettyName, skin, location);

		int currentStep = npcs.size();

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (!npcs.contains(npc)) {
				npcs.add(npc);

				spawnNPC(npc);
				createHologram(npc);
			}
		}, (long) currentStep * spawnDelay);
	}

	public static void spawnNPC(NPC npc) {
		try {
			net.citizensnpcs.api.npc.NPC newNPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npc.getSKIN(), npc.getLOCATION());

			newNPC.setName(Common.colorize("&8&m                "));
			newNPC.getOrAddTrait(LookClose.class).lookClose(true);
			newNPC.getOrAddTrait(SkinTrait.class).setSkinName(npc.getSKIN());
		} catch (Exception e) {
			Common.logFramed("Failed to spawn NPC: " + e.getMessage(), e + "");
		}
	}

	/**
	 * Create a hologram for the NPC
	 *
	 * @param npc The NPC to create a hologram for
	 */
	private static void createHologram(NPC npc) {
		String hologramName = "npc_hologram_" + npc.getSERVER().toLowerCase();

		Location hologramLocation = npc.getLOCATION().clone().add(0, 2.8, 0);

		if (DHAPI.getHologram(hologramName) != null) {
			DHAPI.removeHologram(hologramName);
		}

		List<String> lines = getHologramLines(npc);

		Hologram hologram = DHAPI.createHologram(hologramName, hologramLocation, lines);

		holograms.put(npc.getSERVER().toLowerCase(), hologram);
	}

	/**
	 * Get the formatted lines for an NPC hologram
	 *
	 * @param npc The NPC
	 * @return List of formatted lines
	 */
	private static List<String> getHologramLines(NPC npc) {
		List<String> lines = new ArrayList<>();
		String serverName = npc.getSERVER().toLowerCase();

		boolean serverIsAvailable = false;
		String playerCount = "0";

		try {
			Jedis jedis = RedisManager.getJedis();
			if (jedis != null && jedis.isConnected()) {
				serverIsAvailable = jedis.sismember("available-server", serverName);

				if (serverIsAvailable) {
					String count = jedis.hget("players", serverName);
					if (count != null && !count.isEmpty()) {
						playerCount = count;
					}
				}
			} else {
				Common.log("&cRedis connection is not available for NPC hologram update");
			}
		} catch (JedisConnectionException e) {
			Common.log("&cRedis connection error: " + e.getMessage());
		} catch (Exception e) {
			Common.log("&cError getting Redis data: " + e.getMessage());
		}

		String color = serverIsAvailable ? "#12c742" : "#a31515";

		lines.add(color + "§l" + npc.getPRETTY_NAME());

		if (serverIsAvailable) {
			lines.add("#68ed8c" + playerCount + " #e0e0e0Spieler online");
		} else {
			lines.add("#e03636Server ist nicht verfügbar");
		}

		return lines;
	}

	/**
	 * Update all NPC holograms
	 */
	public static void updateHolograms() {
		try {
			Jedis jedis = RedisManager.getJedis();
			if (jedis == null || !jedis.isConnected()) {
				Common.log("&cSkipping hologram update - Redis connection is not available");
				return;
			}

			String pong = jedis.ping();
			if (!"PONG".equals(pong)) {
				Common.log("&cSkipping hologram update - Redis ping failed");
				return;
			}
		} catch (JedisConnectionException e) {
			Common.log("&cSkipping hologram update - Redis connection error: " + e.getMessage());
			return;
		} catch (Exception e) {
			Common.log("&cSkipping hologram update - Error checking Redis: " + e.getMessage());
			return;
		}

		for (NPC npc : npcs) {
			try {
				String serverKey = npc.getSERVER().toLowerCase();
				Hologram hologram = holograms.get(serverKey);

				if (hologram != null) {
					List<String> updatedLines = getHologramLines(npc);
					DHAPI.setHologramLines(hologram, updatedLines);
					hologram.updateAll();
				}
			} catch (Exception e) {
				Common.log("&cError updating hologram for " + npc.getPRETTY_NAME() + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Start the task to update server player counts and holograms
	 */
	private static void startUpdateTask() {
		if (updateTask != null) {
			updateTask.cancel();
		}

		updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			try {
				updateHolograms();
			} catch (Exception e) {
				Common.log("&cError in hologram update task: " + e.getMessage());
			}
		}, 20L, 100L); // Every 5 seconds (100 ticks)
	}

	/**
	 * Clear all NPCs and their holograms
	 */
	public static void clearNPCs() {
		for (Hologram hologram : holograms.values()) {
			if (hologram != null) {
				DHAPI.removeHologram(hologram.getName());
			}
		}

		holograms.clear();

		CitizensAPI.getNPCRegistry().deregisterAll();

		npcs.clear();

		if (updateTask != null) {
			updateTask.cancel();
			updateTask = null;
		}
	}
}