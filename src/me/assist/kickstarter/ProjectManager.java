package me.assist.kickstarter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ProjectManager {

	private Kickstarter plugin;
	private List<Project> projects;

	public ProjectManager(Kickstarter instance) {
		plugin = instance;
	}

	public void createProject(Player p, String name, double target) {
		plugin.getConfiguration().set(p.getUniqueId().toString() + ".projectName", name);
		plugin.getConfiguration().set(p.getUniqueId().toString() + ".projectTarget", target);
		plugin.getConfiguration().set(p.getUniqueId().toString() + ".totalCollected", 0);

		plugin.save();
	}

	public void endProject(Player p) {
		plugin.getConfiguration().set(p.getName(), null);
		plugin.save();
	}

	public boolean hasProject(Player p) {
		return plugin.getConfiguration().contains(p.getName());
	}

	public Project getProject(String projectName) {
		for (Project project : getProjects()) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}

		return null;
	}

	public List<Project> getProjects() {
		if (!projects.isEmpty()) {
			return projects;
		}

		for (String uuid : plugin.getConfiguration().getKeys(false)) {
			projects.add(new Project(plugin, UUID.fromString(uuid), plugin.getConfiguration().getString(uuid + ".projectName"), plugin.getConfiguration().getDouble(uuid + ".projectTarget"), plugin.getConfiguration().getDouble(uuid + ".totalCollected")));
		}

		return projects;
	}

	public Project getPlayerProject(Player p) {
		for (Project project : getProjects()) {
			if (project.getOwner() == p.getUniqueId()) {
				return project;
			}
		}

		return null;
	}

	public double getCollected(String projectName) {
		Project project = getProject(projectName);

		if (project != null) {
			return project.getCollected();
		}

		return 0;
	}

	public void fundProject(String projectName, Player funder, double amount) {
		Project project = getProject(projectName);

		if (project != null) {
			plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(project.getOwner()), amount);

			String uuid = project.getOwner().toString();
			plugin.getConfiguration().set(uuid + ".totalCollected", getCollected(projectName) + amount);

			List<String> funders;

			if (!plugin.getConfiguration().contains(uuid + ".funders")) {
				funders = new ArrayList<>();
			} else {
				funders = plugin.getConfiguration().getStringList(uuid + ".funders");
			}

			funders.add(funder.getName() + ":" + amount);

			plugin.getConfiguration().set(uuid + ".funders", funders);
			plugin.save();
		}
	}
}
