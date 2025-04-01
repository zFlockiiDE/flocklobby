package ovh.fedox.flocklobby.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;

/**
 * NPC.java -
 * <p>
 * Created on 3/31/2025 at 3:44 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
public class NPC {

	private final String SERVER;
	private final String PRETTY_NAME;
	private final String SKIN;
	private final Location LOCATION;

}
