package me.assist.kickstarter;

import java.util.UUID;


public class Project {

	/*
	 * Required for OfflinePlayer economy deposit
	 */
	private UUID id;
	
	private String owner;
	private String name;
	private double target;
	private double collected;
	
	public Project(String owner, UUID id, String name, double target, double collected) {
		this.owner = owner;
		this.id = id;
		this.name = name;
		this.target = target;
		this.collected = collected;
	}

	public String getPlayer() {
		return owner;
	}
	
	public UUID getPlayerUUID() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public double getTarget() {
		return target;
	}
	
	public double getCollected() {
		return collected;
	}
}
