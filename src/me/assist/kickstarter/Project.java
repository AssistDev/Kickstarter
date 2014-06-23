package me.assist.kickstarter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Project {


	private UUID owner;
	private String name;
	private double target;
	private double collected;

	private Map<String, Double> funders;

	public Project(UUID owner, String name, double target, double collected) {
		this.owner = owner;
		this.name = name;
		this.target = target;
		this.collected = collected;

		funders = new HashMap<>();
		
	}

	public void addFunder(String name, double amount) {
		funders.put(name, amount);
	}

	public Map<String, Double> getFunders() {
		return funders;
	}

	public UUID getOwner() {
		return owner;
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
