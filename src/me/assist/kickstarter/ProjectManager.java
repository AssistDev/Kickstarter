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
		this.plugin = instance;

		this.projects = new ArrayList<>();
		getProjects();
	}

	public void createProject(Player p, String name, double target) {
		this.plugin.getConfiguration().set(p.getUniqueId().toString() + ".projectName", name);
		this.plugin.getConfiguration().set(p.getUniqueId().toString() + ".projectTarget", target);
		this.plugin.getConfiguration().set(p.getUniqueId().toString() + ".totalCollected", 0);

		this.plugin.save();
	}

	public void endProject(Player p) {
		this.plugin.getConfiguration().set(p.getUniqueId().toString(), null);
		this.plugin.save();
	}

	public boolean hasProject(Player p) {
		return this.plugin.getConfiguration().contains(p.getUniqueId().toString());
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
		if (!this.projects.isEmpty()) {
			return this.projects;
		}

		for (String uuid : this.plugin.getConfiguration().getKeys(false)) {
			this.projects.add(new Project(this.plugin, UUID.fromString(uuid), this.plugin.getConfiguration().getString(uuid + ".projectName"), this.plugin.getConfiguration().getDouble(uuid + ".projectTarget"), this.plugin.getConfiguration().getDouble(uuid + ".totalCollected")));
		}

		return this.projects;
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

		return 0.0D;
	}

	public void fundProject(String projectName, Player funder, double amount) {
		Project project = getProject(projectName);

		if (project != null) {
			this.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(project.getOwner()), amount);

			String uuid = project.getOwner().toString();
			this.plugin.getConfiguration().set(uuid + ".totalCollected", Double.valueOf(getCollected(projectName) + amount));
			List<String> funders;

			if (!this.plugin.getConfiguration().contains(uuid + ".funders")) {
				funders = new ArrayList<>();
			} else {
				funders = this.plugin.getConfiguration().getStringList(uuid + ".funders");
			}

			funders.add(funder.getName() + ":" + amount);
			project.addFunder(funder.getName(), amount);

			this.plugin.getConfiguration().set(uuid + ".funders", funders);
			this.plugin.save();
		}
	}
}
