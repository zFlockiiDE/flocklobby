package ovh.fedox.flocklobby.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import ovh.fedox.flockapi.database.repository.MongoRepository;
import ovh.fedox.flocklobby.model.ArmorStandSubmission;

import java.util.List;
import java.util.UUID;

/**
 * ArmorStandSubmissionRepository.java - Repository for ArmorStandSubmission
 * <p>
 * Created on 4/5/2025 by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */
public class ArmorStandSubmissionRepository extends MongoRepository<ArmorStandSubmission> {

	/**
	 * Create a new ArmorStandSubmissionRepository
	 *
	 * @param collection The MongoDB collection
	 */
	public ArmorStandSubmissionRepository(MongoCollection<Document> collection) {
		super(collection, ArmorStandSubmission.class, ArmorStandSubmission::new);
	}

	/**
	 * Find submissions by player UUID
	 *
	 * @param uuid The player UUID
	 * @return List of submissions
	 */
	public List<ArmorStandSubmission> findByPlayerUUID(String uuid) {
		return findByFilter(Filters.eq("uuid", uuid));
	}

	/**
	 * Find submissions by player name
	 *
	 * @param name The player name
	 * @return List of submissions
	 */
	public List<ArmorStandSubmission> findByPlayerName(String name) {
		return findByFilter(Filters.eq("name", name));
	}

	/**
	 * Create a new submission
	 *
	 * @param uuid   Player UUID
	 * @param name   Player name
	 * @param notice Notice text
	 * @return The created submission
	 */
	public ArmorStandSubmission createSubmission(UUID uuid, String name, String notice) {
		ArmorStandSubmission submission = new ArmorStandSubmission();
		submission.setId(uuid.toString() + "_" + System.currentTimeMillis());
		submission.setUuid(uuid.toString());
		submission.setName(name);
		submission.setNotice(notice);

		save(submission);
		return submission;
	}
}

