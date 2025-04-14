package ovh.fedox.flocklobby.conversation;


/**
 * WishPrompt.java -
 * <p>
 * Created on 4/5/2025 at 4:51 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.conversation.SimplePrompt;
import ovh.fedox.flockapi.util.SoundUtil;
import ovh.fedox.flocklobby.FlockLobby;
import ovh.fedox.flocklobby.model.ArmorStandSubmission;

/**
 * r
 * Inner class that handles the conversation prompt for setting the refresh time.
 */
public class WishPrompt extends SimplePrompt {

	/**
	 * Constructs a new prompt.
	 */
	public WishPrompt() {
		super(false);
	}

	/**
	 * Gets the prompt message to display to the player.
	 *
	 * @param context The conversation context
	 * @return The prompt message asking for backpack option
	 */
	@Override
	protected String getPrompt(ConversationContext context) {
		return "Wo möchtest du deinen Armor-Stand stehen haben und im welchen Kontext? (Nicht alle Kontexte sind verfügbar)";
	}

	/**
	 * Gets the prefix for the prompt message.
	 *
	 * @return The prefix for the prompt message
	 */
	@Override
	protected String getCustomPrefix() {
		return Messenger.getQuestionPrefix();
	}

	/**
	 * Gets the text to display when validation fails.
	 *
	 * @param context      The conversation context
	 * @param invalidInput The invalid input
	 * @return The message indicating the input was invalid
	 */
	@Override
	protected String getFailedValidationText(ConversationContext context, String invalidInput) {
		return "Bitte gib einen gültigen Namen für deinen Armor-Stand ein.";
	}

	/**
	 * Accepts the validated input and saves the setting.
	 *
	 * @param conversationContext The conversation context
	 * @param s                   The input
	 * @return The end of the conversation
	 */
	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
		String wish = s;

		final Player player = (Player) conversationContext.getForWhom();

		ArmorStandSubmission submission = FlockLobby.getInstance().getSubmissionRepository().createSubmission(
				player.getUniqueId(),
				player.getName(),
				wish
		);

		Messenger.success(player, "Deine Anfrage wurde erfolgreich eingereicht!");

		FlockLobby.getInstance().getSubmissionRepository().save(submission);
		SoundUtil.playSound(player, SoundUtil.SoundType.SUCCESS);

		return END_OF_CONVERSATION;
	}
}