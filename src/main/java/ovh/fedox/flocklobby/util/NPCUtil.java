package ovh.fedox.flocklobby.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import ovh.fedox.flockapi.database.RedisManager;
import ovh.fedox.flockapi.util.SoundUtil;
import ovh.fedox.flocklobby.FlockLobby;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * NPCUtil.java - NPC Utils for the lobby system
 * <p>
 * Created on 3/31/2025 at 3:44 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@UtilityClass
public class NPCUtil implements Listener {

	@Getter
	private static final List<ovh.fedox.flocklobby.model.NPC> npcs = new ArrayList<>();

	@Getter
	private static final Map<String, Hologram> holograms = new HashMap<>();

	private static final Map<Integer, String> npcServerMap = new HashMap<>();

	private static final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();
	private static final long REQUEST_COOLDOWN = TimeUnit.SECONDS.toMillis(2);
	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY = TimeUnit.SECONDS.toMillis(5);

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

		Bukkit.getPluginManager().registerEvents(new NPCClickListener(), plugin);

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
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

		ovh.fedox.flocklobby.model.NPC npc = new ovh.fedox.flocklobby.model.NPC(server, prettyName, skin, location);

		int currentStep = npcs.size();

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (!npcs.contains(npc)) {
				npcs.add(npc);

				spawnNPC(npc);
				createHologram(npc);
			}
		}, (long) currentStep * spawnDelay);
	}

	public static void spawnNPC(ovh.fedox.flocklobby.model.NPC npc) {
		spawnNPCWithRetry(npc, 0);
	}

	/**
	 * Spawn an NPC with retry logic to handle rate limiting
	 *
	 * @param npc        The NPC to spawn
	 * @param retryCount Current retry attempt
	 */
	private static void spawnNPCWithRetry(ovh.fedox.flocklobby.model.NPC npc, int retryCount) {
		try {
			String skinName = npc.getSKIN();
			long currentTime = System.currentTimeMillis();
			Long lastRequestTime = lastRequestTimes.get(skinName);

			if (lastRequestTime != null) {
				long timeSinceLastRequest = currentTime - lastRequestTime;
				if (timeSinceLastRequest < REQUEST_COOLDOWN) {
					long waitTime = REQUEST_COOLDOWN - timeSinceLastRequest;
					Common.log("&eWaiting " + waitTime + "ms before spawning NPC with skin: " + skinName);

					Bukkit.getScheduler().runTaskLater(plugin, () -> spawnNPCWithRetry(npc, retryCount), (waitTime / 50) + 1);
					return;
				}
			}

			lastRequestTimes.put(skinName, currentTime);

			NPC newNPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npc.getSKIN(), npc.getLOCATION());

			newNPC.setName(Common.colorize("&8&m                "));
			newNPC.getOrAddTrait(LookClose.class).lookClose(true);

			SkinTrait skinTrait = newNPC.getOrAddTrait(SkinTrait.class);
			skinTrait.setSkinName(npc.getSKIN());
			skinTrait.setShouldUpdateSkins(true);

			newNPC.getEntity().setMetadata(NPC.Metadata.SHOULD_SAVE.getKey(), new FixedMetadataValue(plugin, false));


			npcServerMap.put(newNPC.getId(), npc.getSERVER().toLowerCase());

			Common.log("&aSuccessfully spawned NPC with skin: " + npc.getSKIN());

		} catch (Exception e) {
			String errorMsg = e.getMessage();
			Common.log("&cFailed to spawn NPC: " + errorMsg);

			if ((errorMsg != null && errorMsg.contains("403")) ||
					(e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("403"))) {

				if (retryCount < MAX_RETRIES) {
					int nextRetry = retryCount + 1;
					long delayTicks = RETRY_DELAY / 50;

					Common.log("&eRate limit detected. Retrying in " + (RETRY_DELAY / 1000) + " seconds. Attempt " + nextRetry + "/" + MAX_RETRIES);

					Bukkit.getScheduler().runTaskLater(plugin, () -> spawnNPCWithRetry(npc, nextRetry), delayTicks);
				} else {
					Common.log("&cMax retries reached for NPC with skin: " + npc.getSKIN() + ". Using fallback skin.");
					try {
						NPC fallbackNPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Steve", npc.getLOCATION());
						fallbackNPC.setName(Common.colorize("&8&m                "));
						fallbackNPC.getOrAddTrait(LookClose.class).lookClose(true);

						npcServerMap.put(fallbackNPC.getId(), npc.getSERVER().toLowerCase());
					} catch (Exception ex) {
						Common.logFramed("&cFailed to create fallback NPC: " + ex.getMessage());
					}
				}
			} else {
				Common.logFramed("&cNon-rate-limit error spawning NPC: " + e.getMessage(), e + "");
			}
		}
	}

	/**
	 * Create a hologram for the NPC
	 *
	 * @param npc The NPC to create a hologram for
	 */
	private static void createHologram(ovh.fedox.flocklobby.model.NPC npc) {
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
	private static List<String> getHologramLines(ovh.fedox.flocklobby.model.NPC npc) {
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

		for (ovh.fedox.flocklobby.model.NPC npc : npcs) {
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
				try {
					DHAPI.removeHologram(hologram.getName());
				} catch (Exception e) {
					Common.log("&cError removing hologram: " + e.getMessage());
				}
			}
		}
		holograms.clear();

		try {
			Iterable<NPCRegistry> registeredNPCS = CitizensAPI.getNPCRegistries();
			for (NPCRegistry registry : registeredNPCS) {
				try {
					for (NPC npc : registry.sorted()) {
						if (npc != null) {
							npc.destroy();
						}
					}
				} catch (Exception e) {
					Common.log("&cError processing NPC registry: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			Common.log("&cError accessing NPC registries: " + e.getMessage());
		}

		deleteSavesDirectory();

		npcs.clear();
		npcServerMap.clear();
		lastRequestTimes.clear();

		if (updateTask != null) {
			try {
				updateTask.cancel();
			} catch (Exception e) {
				Common.log("&cError cancelling update task: " + e.getMessage());
			}
			updateTask = null;
		}
	}

	/**
	 * Deletes the Citizens/saves.yml directory, im doing this cause im too dumb to using their API
	 */
	private void deleteSavesDirectory() {
		try {
			String citizensFolder = FlockLobby.getPlugin(Citizens.class).getDataFolder().getAbsolutePath();
			File savesFile = new File(citizensFolder, "saves.yml");

			if (savesFile.exists()) {
				savesFile.delete();
				Common.log("&aDeleted Citizens/saves.yml file.");
			} else {
				Common.log("&cCitizens/saves.yml file does not exist.");
			}

		} catch (Exception e) {
			Common.log("&cError deleting Citizens/saves.yml file: " + e.getMessage());
		}
	}

	/**
	 * Check if a server is available
	 *
	 * @param serverName The server name to check
	 * @return True if the server is available
	 */
	public static boolean isServerAvailable(String serverName) {
		try {
			Jedis jedis = RedisManager.getJedis();
			if (jedis != null && jedis.isConnected()) {
				return jedis.sismember("available-server", serverName.toLowerCase());
			}
		} catch (Exception e) {
			Common.log("&cError checking server availability: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Send a player to a BungeeCord server
	 *
	 * @param player     The player to send
	 * @param serverName The server to send them to
	 */
	public static void sendPlayerToServer(Player player, String serverName) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("Connect");
			out.writeUTF(serverName);

			player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());

			Messenger.success(player, "Verbinde mit '" + serverName + "'...");
			SoundUtil.playSound(player, SoundUtil.SoundType.SUCCESS);
		} catch (Exception e) {
			Common.log("&cError sending player to server: " + e.getMessage());
			Messenger.error(player, "&cFehler beim Verbinden mit dem Server. Bitte versuche es später erneut.");
			SoundUtil.playSound(player, SoundUtil.SoundType.FAILURE);
		}
	}

	/**
	 * Inner class to handle NPC click events
	 */
	public static class NPCClickListener implements Listener {

		@EventHandler
		public void onNPCRightClick(NPCRightClickEvent event) {
			handleNPCClick(event.getNPC(), event.getClicker());
		}

		@EventHandler
		public void onNPCClick(NPCLeftClickEvent event) {
			handleNPCClick(event.getNPC(), event.getClicker());
		}

		private void handleNPCClick(NPC citizensNPC, Player player) {
			String serverName = npcServerMap.get(citizensNPC.getId());

			if (serverName == null) {
				return;
			}

			if (isServerAvailable(serverName)) {
				sendPlayerToServer(player, serverName);
			} else {
				Messenger.error(player, "&c&lServer nicht verfügbar!");
				Messenger.error(player, "&cDieser Server ist derzeit nicht erreichbar.");
				SoundUtil.playSound(player, SoundUtil.SoundType.FAILURE);
			}
		}
	}
}

