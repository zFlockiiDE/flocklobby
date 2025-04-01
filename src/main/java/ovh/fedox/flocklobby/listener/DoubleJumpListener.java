package ovh.fedox.flocklobby.listener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ovh.fedox.flocklobby.FlockLobby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * DoubleJumpListener.java - Enhanced double jump with fancy effects
 * <p>
 * Created on 4/1/2025 at 5:00 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public class DoubleJumpListener implements Listener {

	private final HashSet<String> players = new HashSet<>();
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private final FlockLobby plugin;

	public DoubleJumpListener(FlockLobby plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void setFly(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		player.setAllowFlight(true);
		player.setFlying(false);

		showReadyEffect(player);
	}

	@EventHandler
	public void setVelocity(PlayerToggleFlightEvent e) {
		Player player = e.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		}

		if (cooldowns.containsKey(player.getUniqueId())) {
			long COOLDOWN_TIME = 1500;
			long timeLeft = (cooldowns.get(player.getUniqueId()) + COOLDOWN_TIME) - System.currentTimeMillis();
			if (timeLeft > 0) {
				e.setCancelled(true);
				return;
			}
		}

		if (!players.contains(player.getName())) {
			players.add(player.getName());
			e.setCancelled(true);

			player.setAllowFlight(false);
			player.setFlying(false);

			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

			Vector direction = player.getLocation().getDirection().multiply(1.8).setY(1.2);
			player.setVelocity(direction);

			player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.7f, 1.2f);
			player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 0.5f, 1.5f);

			createJumpEffect(player);

			createTrailEffect(player);

			player.setFallDistance(0);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();

		if (players.contains(player.getName()) && player.isOnGround()) {
			players.remove(player.getName());

			createLandingEffect(player);

			player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SMALL_FALL, 0.7f, 0.8f);

			new BukkitRunnable() {
				@Override
				public void run() {
					player.setAllowFlight(true);
					showReadyEffect(player);
				}
			}.runTaskLater(plugin, 5L);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player player && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			if (players.contains(player.getName())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void removePlayer(PlayerQuitEvent e) {
		players.remove(e.getPlayer().getName());
		cooldowns.remove(e.getPlayer().getUniqueId());
	}

	private void createJumpEffect(Player player) {
		Location loc = player.getLocation();

		for (int i = 0; i < 3; i++) {
			final int iteration = i;
			new BukkitRunnable() {
				@Override
				public void run() {
					for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
						double x = Math.cos(angle) * (iteration * 0.5);
						double z = Math.sin(angle) * (iteration * 0.5);
						Location particleLoc = loc.clone().add(x, 0.1, z);

						player.getWorld().spawnParticle(
								Particle.WITCH,
								particleLoc,
								1, 0, 0, 0, 0
						);
					}
				}
			}.runTaskLater(plugin, i * 2L);
		}

		player.getWorld().spawnParticle(
				Particle.EXPLOSION,
				loc.clone().add(0, 0.5, 0),
				15, 0.3, 0.3, 0.3, 0.05
		);

		player.getWorld().spawnParticle(
				Particle.CLOUD,
				loc.clone().add(0, 0.2, 0),
				25, 0.3, 0.1, 0.3, 0.05
		);
	}

	private void createTrailEffect(Player player) {
		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks > 20 || player.isOnGround()) {
					this.cancel();
					return;
				}

				Location loc = player.getLocation();

				Particle.DustOptions dustOptions = new Particle.DustOptions(
						Color.fromRGB(
								0,
								(int) (Math.cos(ticks * 0.5) * 127 + 128),
								0
						),
						1.0f
				);

				player.getWorld().spawnParticle(
						Particle.DUST,
						loc.clone().add(0, 0.1, 0),
						10, 0.2, 0.1, 0.2, 0, dustOptions
				);

				if (ticks % 2 == 0) {
					player.getWorld().spawnParticle(
							Particle.FIREWORK,
							loc.clone().add(0, 0.1, 0),
							3, 0.2, 0.1, 0.2, 0.01
					);
				}

				ticks++;
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	private void createLandingEffect(Player player) {
		Location loc = player.getLocation();

		player.getWorld().spawnParticle(
				Particle.BLOCK_CRUMBLE,
				loc.clone().add(0, 0.1, 0),
				50, 0.5, 0.1, 0.5, 0.1,
				loc.getBlock().getBlockData()
		);

		for (int i = 0; i < 3; i++) {
			final int radius = i + 1;
			new BukkitRunnable() {
				@Override
				public void run() {
					for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
						double x = Math.cos(angle) * radius;
						double z = Math.sin(angle) * radius;
						Location particleLoc = loc.clone().add(x, 0.1, z);

						player.getWorld().spawnParticle(
								Particle.END_ROD,
								particleLoc,
								1, 0, 0, 0, 0
						);
					}
				}
			}.runTaskLater(plugin, i * 2L);
		}
	}

	private void showReadyEffect(Player player) {
		Location loc = player.getLocation().add(0, 1, 0);

		player.getWorld().spawnParticle(
				Particle.INSTANT_EFFECT,
				loc,
				15, 0.3, 0.3, 0.3, 0.02
		);

		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
	}
}