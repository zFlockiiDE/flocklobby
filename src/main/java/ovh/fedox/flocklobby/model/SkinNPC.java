package ovh.fedox.flocklobby.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;

/**
 * SkinNPC.java -
 * <p>
 * Created on 4/5/2025 at 4:27 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
public class SkinNPC {

	private final String skin;
	private final Location location;

}
