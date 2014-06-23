package me.assist.kickstarter;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Kickstarter extends JavaPlugin {

	public Economy economy;

	private File f;
	private FileConfiguration c;
	
	private ProjectManager manager;

	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("Vault") != null) {
			getLogger().info("Found Vault, hooking...");

			if (setupEconomy()) {
				getLogger().info("Succesfully hooked to Vault!");
			} else {
				getLogger().severe("Unable to hook to Vault, disabling plugin...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

		} else {
			getLogger().severe("Vault not found, disabling plugin...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		f = new File(getDataFolder() + "projects.yml");

		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		c = YamlConfiguration.loadConfiguration(f);

		manager = new ProjectManager(this);
		manager.getProjects();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("kickstarter")) {
			if (!(sender instanceof Player))
				return true;

			Player p = (Player) sender;

			if (args.length == 0) {
				p.sendMessage("Showing all Kickstarter commands:");
				p.sendMessage("/kickstarter - display all the Kickstarter commands");
				p.sendMessage("/kickstarter create <name> <target> - create a project");
				p.sendMessage("/kickstarter end - end your project");
				p.sendMessage("/kickstarter fund <name> <amount> - fund a project");
				p.sendMessage("/kickstarter browse - display all the projects");
			} else if (args.length > 0) {
				if (args[0].equalsIgnoreCase("create")) {
					if (!p.hasPermission("kickstarter.create")) {
						p.sendMessage("You don't have permission to perform this command.");
						return true;
					}

					if (args.length == 3) {
						String name = args[1];
						double target = 0;

						try {
							target = Double.parseDouble(args[2]);
						} catch (NumberFormatException ex) {
							p.sendMessage("Target amount must be a number!");
							return true;
						}

						if (!manager.hasProject(p)) {
							if (manager.getProject(name) == null) {
								manager.createProject(p, name, target);
								p.sendMessage("You have created a Kickstarter project called " + name + " with a target of " + target + "!");
							} else {
								p.sendMessage("The project " + name + " already exists!");
							}
							
						} else {
							p.sendMessage("You already have a Kickstarter project! If you wish to create a new project, end the current one first by typing /kickstarter end");
						}
					}

				} else if (args[0].equalsIgnoreCase("end")) {
					if (!p.hasPermission("kickstarter.end")) {
						p.sendMessage("You don't have permission to perform this command.");
						return true;
					}

					if (manager.hasProject(p)) {
						p.sendMessage("You have ended your Kickstarter project. You collected " + manager.getCollected(manager.getPlayerProject(p).getName()) + "$.");
						manager.endProject(p);
					}

				} else if (args[0].equalsIgnoreCase("fund")) {
					if (!p.hasPermission("kickstarter.fund")) {
						p.sendMessage("You don't have permission to perform this command.");
						return true;
					}

					if (args.length == 3) {
						String projectName = args[1];
						double amount = 0;

						try {
							amount = Double.parseDouble(args[2]);
						} catch (NumberFormatException ex) {
							p.sendMessage("Amount must be a number!");
							return true;
						}

						double money = economy.getBalance(p);

						if (money >= amount) {
							economy.withdrawPlayer(p, amount);
							manager.fundProject(projectName, p, amount);

							p.sendMessage("You have funded the project " + projectName + " with " + amount + "$.");
						} else {
							p.sendMessage("You don't have enough money to fund this project.");
						}
					}

				} else if (args[0].equalsIgnoreCase("browse")) {
					if (!p.hasPermission("kickstarter.browse")) {
						p.sendMessage("You don't have permission to perform this command.");
						return true;
					}

					if (args.length == 1 || args.length == 2) {
						int page = 0;

						if (args.length == 2) {
							try {
								page = Integer.parseInt(args[1]);
							} catch (NumberFormatException ex) {
								p.sendMessage("Page must be a number!");
								return true;
							}
						}

						int projects = manager.getProjects().size();
						int pages = projects % 10;

						if (page > pages) {
							p.sendMessage("Invalid page. Pages: " + pages);
							return true;
						}

						for (int i = 0; i < (projects > 10 ? 10 : projects); i++) {
							Project project = manager.getProjects().get((page * 10) + i);
							p.sendMessage(i + ". Name: " + project.getName() + ", Owner: " + toPlayer(project.getOwner()).getName() + ", Target: " + project.getTarget() + ", Collected: " + project.getCollected() + "$.");
						}

						if (pages > 1) {
							p.sendMessage("Page 1/" + pages);
						}
					}
				}
			}
		}

		return false;
	}

	

	private OfflinePlayer toPlayer(UUID id) {
		return getServer().getOfflinePlayer(id);
	}

	public void save() {
		try {
			c.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfiguration() {
		return c;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return economy != null;
	}
}
