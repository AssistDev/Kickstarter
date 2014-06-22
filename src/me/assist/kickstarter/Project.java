package me.assist.kickstarter;

import java.util.HashMap;
import java.util.Map;
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
	
	private Map<String, Double> funders;
	
	public Project(String owner, UUID id, String name, double target, double collected) {
		this.owner = owner;
		this.id = id;
		this.name = name;
		this.target = target;
		this.collected = collected;
		
		funders = new HashMap<>();
		// need to somehow load existing funders
	}
	
	public void addFunder(String name, double amount) {
		funders.put(name, amount);
	}
	
	public Map<String, Double> getFunders() {
		return funders;
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
