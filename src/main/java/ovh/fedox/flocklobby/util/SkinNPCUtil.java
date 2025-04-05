package ovh.fedox.flocklobby.util;


import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import ovh.fedox.flockapi.util.ColorUtil;
import ovh.fedox.flockapi.util.SoundUtil;
import ovh.fedox.flocklobby.FlockLobby;
import ovh.fedox.flocklobby.conversation.WishPrompt;
import ovh.fedox.flocklobby.menu.PromptMenu;
import ovh.fedox.flocklobby.model.ArmorStandSubmission;
import ovh.fedox.flocklobby.model.SkinNPC;

import java.util.ArrayList;
import java.util.List;

/**
 * SkinNPCUtil.java -
 * <p>
 * Created on 4/5/2025 at 4:26 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@UtilityClass
public class SkinNPCUtil {

	@Getter
	private static final List<SkinNPC> npcs = new ArrayList<>();

	/**
	 * Spawn an NPC with retry logic to handle rate limiting
	 *
	 * @param npc The NPC to spawn
	 */
	public static void spawnNPCWithRetry(SkinNPC npc) {
		String skinName = npc.getSkin();

		NPC newNPC = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.PLAYER, npc.getSkin(), npc.getLocation());

		newNPC.setName(ColorUtil.format("<color=#55ab6c><bold>Skin-Anfrage einreichen</bold></color>"));
		newNPC.getOrAddTrait(LookClose.class).lookClose(true);

		SkinTrait skinTrait = newNPC.getOrAddTrait(SkinTrait.class);
		skinTrait.setSkinName(npc.getSkin());
		skinTrait.setShouldUpdateSkins(true);

		npcs.add(npc);

		Common.log("&aSuccessfully spawned Skin-NPC");
	}

	/**
	 * Inner class to handle NPC click events
	 */
	public static class SkinNPCClickListener implements Listener {

		@EventHandler
		public void onNPCRightClick(NPCRightClickEvent event) {
			handleNPCClick(event.getNPC(), event.getClicker());
		}

		@EventHandler
		public void onNPCClick(NPCLeftClickEvent event) {
			handleNPCClick(event.getNPC(), event.getClicker());
		}

		private void handleNPCClick(NPC citizensNPC, Player player) {
			String npcName = citizensNPC.getRawName();

			if (npcName.contains("Skin-Anfrage einreichen")) {

				if (!FlockLobby.getInstance().getSubmissionRepository().findByPlayerUUID(String.valueOf(player.getUniqueId())).isEmpty()) {
					Messenger.error(player, "Du hast bereits eine Anfrage eingereicht!");
					return;
				}

				new PromptMenu((result) -> {
					boolean isYes = (boolean) result;

					if (!isYes) {
						ArmorStandSubmission submission = FlockLobby.getInstance().getSubmissionRepository().createSubmission(
								player.getUniqueId(),
								player.getName(),
								"Ich möchte diesen Skin haben: " + player.getName()
						);

						Messenger.success(player, "Deine Anfrage wurde erfolgreich eingereicht!");
						SoundUtil.playSound(player, SoundUtil.SoundType.SUCCESS);

						FlockLobby.getInstance().getSubmissionRepository().save(submission);
					} else {
						new WishPrompt().show(player);
					}

				}, new String[]{"Hast du eine Wunschposition", "oder ein Wunschplatz?"}, true, "", "").displayTo(player);

			} else {
				Common.log("&cNPC not found or name does not match.");
			}
		}
	}

}
