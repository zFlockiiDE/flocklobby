package ovh.fedox.flocklobby.command;


import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;
import ovh.fedox.flocklobby.FlockLobby;
import ovh.fedox.flocklobby.model.ArmorStandSubmission;
import ovh.fedox.flocklobby.repository.ArmorStandSubmissionRepository;

/**
 * TestCommand.java -
 * <p>
 * Created on 4/5/2025 at 3:37 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@AutoRegister
public final class TestCommand extends SimpleCommand {

	public TestCommand() {
		super("test_submission");
	}

	@Override
	protected void onCommand() {
		FlockLobby plugin = FlockLobby.getInstance();
		ArmorStandSubmissionRepository repository = plugin.getSubmissionRepository();

		final Player player = getPlayer();

		ArmorStandSubmission submission = repository.createSubmission(player.getUniqueId(), player.getName(), "Test Befehl");

		tellSuccess("Deine Submission wurde gespeichert! ID: " + submission.getId());
	}
}
