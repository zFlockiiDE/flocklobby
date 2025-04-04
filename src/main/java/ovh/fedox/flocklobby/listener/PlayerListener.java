package ovh.fedox.flocklobby.listener;


import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import ovh.fedox.flockapi.database.service.punishment.PunishmentService;
import ovh.fedox.flocklobby.settings.Setting;
import ovh.fedox.flocklobby.util.LocationUtil;

import java.util.Calendar;

/**
 * PlayerListener.java - Handles player events
 * <p>
 * Created on 3/31/2025 at 7:23 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@AutoRegister
public final class PlayerListener implements Listener {

	/**
	 * Set player's game mode to Adventure, set health to 20, set level to current year, set exp to 0, add blindness effect, teleport player to spawn, play firework sound and spawn firework
	 *
	 * @param event PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		int year = Calendar.getInstance().get(Calendar.YEAR);

		if (PunishmentService.getInstance().isBanned(player.getUniqueId())) {
			return;
		}

		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealthScale(20);
		player.setLevel(year);
		player.setExp(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 1, false, false));

		Common.runLater(() -> {
			player.teleport(LocationUtil.stringToLocation(Setting.SPAWN_LOCATION));
			CompSound.ENTITY_FIREWORK_ROCKET_TWINKLE.play(player, 0.4f, 1.5f);
			CompSound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR.play(player, 0.4f, 1.5f);

			Common.runLater(10, () -> {
				Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);

				FireworkMeta meta = firework.getFireworkMeta();
				meta.addEffect(FireworkEffect.builder().withColor(Color.LIME).withFade(Color.GREEN).trail(true).build());
				meta.setPower(1);

				firework.setFireworkMeta(meta);
			});

		});
	}

	/**
	 * Cancel right-clicking on ArmorStands
	 *
	 * @param e PlayerInteractAtEntityEvent
	 */
	@EventHandler
	public void onManipulate(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof ArmorStand) {
			e.setCancelled(true);
		}
	}

	/**
	 * Teleport player back to spawn if they fall below the minimum Y value
	 *
	 * @param event PlayerMoveEvent
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) { // Yes, i know this Event is heavy, but it's fine for now
		Player player = event.getPlayer();

		if (player.getLocation().getY() < Setting.MIN_Y) {
			player.teleport(LocationUtil.stringToLocation(Setting.SPAWN_LOCATION));
		}
	}

	/**
	 * Handle entity damage by entity event
	 *
	 * @param event EntityDamageByEntityEvent
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() == EntityType.PLAYER && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
			final Player damager = (Player) event.getDamager();

			if (damager.hasPermission("omega.super.flugstunde")) {
				Entity hitEntity = event.getEntity();

				Vector direction = hitEntity.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
				double strength = 3.0;

				direction.setY(0.5);

				direction.multiply(strength);

				hitEntity.setVelocity(direction);

				hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);
				hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.8f);

				spawnKnockbackParticles(hitEntity.getLocation());

				if (hitEntity instanceof Player hitPlayer) {
					Remain.sendTitle(hitPlayer, 5, 15, 5, "", "§a§l✈ Flugstunde! ✈");
				}
			}
		}
	}

	/**
	 * Spawn particles at the given location
	 *
	 * @param location The location to spawn the particles at
	 */
	private void spawnKnockbackParticles(Location location) {
		World world = location.getWorld();

		world.spawnParticle(Particle.EXPLOSION, location, 5, 0.2, 0.2, 0.2, 0.05);

		world.spawnParticle(Particle.SMOKE, location, 15, 0.3, 0.3, 0.3, 0.05);

		world.spawnParticle(Particle.CRIT, location, 20, 0.5, 0.5, 0.5, 0.1);

		try {
			Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GREEN, 2.0f);
			world.spawnParticle(Particle.DUST, location, 15, 0.5, 0.5, 0.5, 0, dustOptions);
		} catch (Exception e) {
			// Fallback für ältere Versionen
			world.spawnParticle(Particle.DUST, location, 15, 0.5, 0.5, 0.5, 0);
		}

		Vector direction = location.getDirection().normalize().multiply(0.5);
		for (int i = 0; i < 5; i++) {
			Location particleLocation = location.clone().add(direction.clone().multiply(i));
			world.spawnParticle(Particle.CLOUD, particleLocation, 3, 0.1, 0.1, 0.1, 0.02);
		}
	}

}
