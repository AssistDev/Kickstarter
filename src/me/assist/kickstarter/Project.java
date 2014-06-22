package me.assist.kickstarter;


public class Project {
	
	private String owner;
	private String name;
	private double target;
	private double collected;
	
	public Project(String owner, String name, double target, double collected) {
		this.owner = owner;
		this.name = name;
		this.target = target;
		this.collected = collected;
	}

	public String getPlayer() {
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
