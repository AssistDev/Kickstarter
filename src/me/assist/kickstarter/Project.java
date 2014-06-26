package me.assist.kickstarter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Project {
	private Kickstarter plugin;

	private UUID owner;
	private String name;
	private double target;
	private double collected;
	private Map<String, Double> funders;

	public Project(Kickstarter instance, UUID owner, String name, double target, double collected) {
		this.plugin = instance;

		this.owner = owner;
		this.name = name;
		this.target = target;
		this.collected = collected;

		this.funders = new HashMap<>();
		loadFunders();
	}

	private void loadFunders() {
		if (this.plugin.getConfiguration().contains(getOwner().toString() + ".funders")) {
			for (String s : this.plugin.getConfiguration().getStringList(getOwner().toString() + ".funders")) {
				String[] r = s.split(":");
				String n = r[0];
				double k = 0.0D;
				
				try {
					k = Double.parseDouble(r[1]);
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					return;
				}
				
				this.funders.put(n, k);
			}
		}
	}

	public void addFunder(String name, double amount) {
		this.funders.put(name, amount);
	}

	public Map<String, Double> getFunders() {
		return this.funders;
	}

	public UUID getOwner() {
		return this.owner;
	}

	public String getName() {
		return this.name;
	}

	public double getTarget() {
		return this.target;
	}

	public double getCollected() {
		return this.collected;
	}
}
