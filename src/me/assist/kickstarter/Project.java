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
		plugin = instance;

		this.owner = owner;
		this.name = name;
		this.target = target;
		this.collected = collected;

		funders = new HashMap<>();
		loadFunders();
	}

	private void loadFunders() {
		if (plugin.getConfiguration().contains(getName() + ".funders")) {
			for (String s : plugin.getConfiguration().getStringList(getName() + ".funders")) {
				String[] r = s.split(":");
				String n = r[0];
				double k = 0;

				try {
					k = Double.parseDouble(r[1]);
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					return;
				}
				
				funders.put(n,  k);
			}
		}
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
