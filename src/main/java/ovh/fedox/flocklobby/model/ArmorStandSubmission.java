package ovh.fedox.flocklobby.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import ovh.fedox.flockapi.database.model.GameEntity;

/**
 * ArmorStandSubmission.java - Model for armor stand submissions
 * <p>
 * Created on 4/5/2025 at 3:28 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */
@Getter
@Setter
public class ArmorStandSubmission implements GameEntity {

	private String id;
	private String uuid;
	private String name;
	private String notice;
	private Boolean isApproved;

	@Override
	public Document toDocument() {
		Document document = new Document();

		document.append("uuid", uuid);
		document.append("name", name);
		document.append("notice", notice);
		document.append("id", id);
		document.append("isApproved", isApproved);

		return document;
	}

	@Override
	public void fromDocument(Document document) {
		this.uuid = document.getString("uuid");
		this.name = document.getString("name");
		this.notice = document.getString("notice");
		this.id = document.getString("id");
		this.isApproved = document.getBoolean("isApproved", false);
	}

	@Override
	public String getId() {
		return id;
	}
}

