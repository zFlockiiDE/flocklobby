package ovh.fedox.flocklobby;

import lombok.Getter;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import ovh.fedox.flockapi.database.MongoDBAPI;
import ovh.fedox.flocklobby.listener.DoubleJumpListener;
import ovh.fedox.flocklobby.model.SkinNPC;
import ovh.fedox.flocklobby.repository.ArmorStandSubmissionRepository;
import ovh.fedox.flocklobby.settings.Setting;
import ovh.fedox.flocklobby.task.LobbyWorldTask;
import ovh.fedox.flocklobby.util.FlockLeaderboard;
import ovh.fedox.flocklobby.util.LocationUtil;
import ovh.fedox.flocklobby.util.NPCUtil;
import ovh.fedox.flocklobby.util.SkinNPCUtil;

/**
 * FlockAPI.java - Main instance of the FlockAPI
 * <p>
 * Created on 3/31/2025 at 4:02 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public final class FlockLobby extends SimplePlugin {

	private MongoDBAPI mongoDBAPI;
	@Getter
	private ArmorStandSubmissionRepository submissionRepository;

	/**
	 * Get the instance of the FlockAPI
	 *
	 * @return the instance
	 */
	public static FlockLobby getInstance() {
		return (FlockLobby) SimplePlugin.getInstance();
	}

	/**
	 * Gets invoked when the plugin loads
	 */
	@Override
	protected void onReloadablesStart() {
		Common.setTellPrefix("&8&l➽ &a&lzFlockii.de &8&l•&7");

	}


	/**
	 * Gets invoked when the plugin starts
	 */
	@Override
	protected void onPluginStart() {

		this.mongoDBAPI = new MongoDBAPI(Setting.MongoDB.MONGO_CONNECTION_STRING, Setting.MongoDB.MONGO_DATABASE);

		this.submissionRepository = new ArmorStandSubmissionRepository(
				mongoDBAPI.getMongoDBManager().getCollection("armor_stand_submissions")
		);

		mongoDBAPI.getMongoDBManager().registerRepository("armorStandSubmissions", submissionRepository);

		Common.setTellPrefix("&8&l➽ &a&lzFlockii.de &8&l•&7");

		NPCUtil.initialize(this);
		FlockLeaderboard.createLeaderboard();

		Common.runTimerAsync(20, 20 * 60 * 5, FlockLeaderboard::updateLeaderboard);
		Common.runLater(20 * 60 * 5, new LobbyWorldTask());

		Common.runLater(20, () -> {
			Setting.NPCS.forEach(NPCUtil::addNPC);
			SkinNPCUtil.spawnNPCWithRetry(new SkinNPC(Setting.SkinNPC.SKIN, LocationUtil.stringToLocation(Setting.SkinNPC.LOCATION)));
		});

		registerEvents(new DoubleJumpListener(this));
		registerEvents(new SkinNPCUtil.SkinNPCClickListener());

	}

	/**
	 * Gets invoked when the plugin stops
	 */
	@Override
	protected void onPluginStop() {
		try {
			FlockLeaderboard.deleteLeaderboard();
		} catch (Exception e) {
			Common.log("&cError deleting leaderboard: " + e.getMessage());
		}

		try {
			NPCUtil.clearNPCs();
		} catch (Exception e) {
			Common.log("&cError clearing NPCs: " + e.getMessage());
		}

		try {
			getServer().getMessenger().unregisterIncomingPluginChannel(this);
			getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		} catch (Exception e) {
			Common.log("&cError unregistering plugin channels: " + e.getMessage());
		}

		if (mongoDBAPI != null) {
			mongoDBAPI.close();
		}
	}
}
