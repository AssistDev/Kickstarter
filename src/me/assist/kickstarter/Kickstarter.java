package me.assist.kickstarter;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Kickstarter extends JavaPlugin {

	private ProjectManager manager;

	public Economy economy;

	private File f;
	private FileConfiguration c;

	private String prefix = "[" + ChatColor.BLUE + "Kickstarter" + ChatColor.WHITE + "] " + ChatColor.RESET;
	private String noPerm = ChatColor.RED + "You don't have permission to perform this command.";

	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("Vault") != null) {
			getLogger().info("Found Vault, hooking...");

			if (setupEconomy()) {
				getLogger().info("Succesfully hooked to Vault!");
			} else {
				getLogger().severe("Unable to hook to Vault, disabling plugin...");
				getServer().getPluginManager().disablePlugin(this);
			}

		} else {
			getLogger().severe("Vault not found, disabling plugin...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.f = new File(getDataFolder() + "projects.yml");

		if (!this.f.exists()) {
			try {
				this.f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.c = YamlConfiguration.loadConfiguration(this.f);
		this.manager = new ProjectManager(this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("kickstarter")) {
			if (!(sender instanceof Player)) {
				return true;
			}

			Player p = (Player) sender;

			if (args.length == 0) {
				p.sendMessage(prefix + "Showing all Kickstarter commands:");
				p.sendMessage(ChatColor.BLUE + "/kickstarter - display all the Kickstarter commands");
				p.sendMessage(ChatColor.BLUE + "/kickstarter create <name> <target> - create a project");
				p.sendMessage(ChatColor.BLUE + "/kickstarter end - end your project");
				p.sendMessage(ChatColor.BLUE + "/kickstarter project - show your project");
				p.sendMessage(ChatColor.BLUE + "/kickstarter fund <name> <amount> - fund a project");
				p.sendMessage(ChatColor.BLUE + "/kickstarter browse - display all the projects");
			} else if (args.length > 0) {
				if (args[0].equalsIgnoreCase("create")) {
					if (!p.hasPermission("kickstarter.create")) {
						p.sendMessage(this.noPerm);
						return true;
					}

					if (args.length == 3) {
						String name = args[1];
						double target = 0.0D;

						try {
							target = Double.parseDouble(args[2]);
						} catch (NumberFormatException ex) {
							p.sendMessage(ChatColor.RED + "Target amount must be a number!");
							return true;
						}

						if (!this.manager.hasProject(p)) {
							if (this.manager.getProject(name) == null) {
								this.manager.createProject(p, name, target);
								p.sendMessage(this.prefix + "You have created a Kickstarter project called " + ChatColor.BLUE + name + ChatColor.WHITE + " with a target of " + ChatColor.BLUE + target + ChatColor.WHITE + "!");
							} else {
								p.sendMessage(ChatColor.RED + "The project " + ChatColor.DARK_RED + name + ChatColor.RED + " already exists!");
							}

						} else {
							p.sendMessage(ChatColor.RED + "You already have a Kickstarter project! If you wish to create a new project, end the current one first by typing " + ChatColor.DARK_RED + " /kickstarter end");
						}
					}

				} else if (args[0].equalsIgnoreCase("end")) {
					if (!p.hasPermission("kickstarter.end")) {
						p.sendMessage(this.noPerm);
						return true;
					}

					if (this.manager.hasProject(p)) {
						p.sendMessage(this.prefix + "You have ended your Kickstarter project. You collected " + ChatColor.BLUE + this.manager.getCollected(this.manager.getPlayerProject(p).getName()) + ChatColor.WHITE + "$.");
						this.manager.endProject(p);
					} else {
						p.sendMessage(ChatColor.RED + "You don't have a Kickstarter project!");
					}

				} else if (args[0].equalsIgnoreCase("project")) {
					if (!p.hasPermission("kickstarter.project")) {
						p.sendMessage(this.noPerm);
						return true;
					}

					if (this.manager.hasProject(p)) {
						Project project = this.manager.getPlayerProject(p);

						p.sendMessage(prefix + "Showing your project:");
						p.sendMessage(ChatColor.WHITE + "Name: " + ChatColor.BLUE + project.getName());
						p.sendMessage(ChatColor.WHITE + "Target: " + ChatColor.BLUE + project.getTarget());
						p.sendMessage(ChatColor.WHITE + "Collected: " + ChatColor.BLUE + project.getCollected());

						double k = project.getTarget() - project.getCollected();
						p.sendMessage(ChatColor.WHITE + "Remaining: " + ChatColor.BLUE + (k <= 0 ? "You've reached your target!" : k + ChatColor.WHITE.toString() + "$."));

						StringBuilder builder = new StringBuilder();

						for (Entry<String, Double> entry : project.getFunders().entrySet()) {
							builder.append(ChatColor.BLUE + entry.getKey() + ChatColor.WHITE + "(" + ChatColor.BLUE + entry.getValue() + ChatColor.WHITE + ")").append(ChatColor.GRAY + ", " + ChatColor.RESET);
						}

						p.sendMessage(ChatColor.WHITE + "Funders: " + ChatColor.BLUE + (builder.toString().isEmpty() ? "You have no funders yet :(" : builder.toString()));
					} else {
						p.sendMessage(ChatColor.RED + "You don't have a Kickstarter project!");
					}

				} else if (args[0].equalsIgnoreCase("fund")) {
					if (!p.hasPermission("kickstarter.fund")) {
						p.sendMessage(this.noPerm);
						return true;
					}

					if (args.length == 3) {
						String projectName = args[1];
						double amount = 0.0D;

						try {
							amount = Double.parseDouble(args[2]);
						} catch (NumberFormatException ex) {
							p.sendMessage(ChatColor.RED + "Amount must be a number!");
							return true;
						}

						double money = this.economy.getBalance(p);

						if (money >= amount) {
							this.economy.withdrawPlayer(p, amount);
							this.manager.fundProject(projectName, p, amount);

							p.sendMessage(this.prefix + "You have funded the project " + ChatColor.BLUE + projectName + ChatColor.WHITE + " with " + ChatColor.BLUE + amount + ChatColor.WHITE + "$.");
						} else {
							p.sendMessage(ChatColor.RED + "You don't have enough money to fund this project.");
						}
					}

				} else if (args[0].equalsIgnoreCase("browse")) {
					if (!p.hasPermission("kickstarter.browse")) {
						p.sendMessage(this.noPerm);
						return true;
					}

					if (args.length == 1 || args.length == 2) {
						int page = 1;

						if (args.length == 2) {
							try {
								page = Integer.parseInt(args[1]);
							} catch (NumberFormatException ex) {
								p.sendMessage(ChatColor.RED + "Page must be a number!");
								return true;
							}
						}

						int all = this.manager.getProjects().size();
						int pages = all % 10;

						if (page > pages) {
							p.sendMessage(ChatColor.RED + "Invalid page. Pages: " + ChatColor.DARK_RED + pages);
							return true;
						}

						for (int i = 0; i < 10; i++) {
							int j = (page == 1 ? 0 : page) * 10 + i;
							Project project = (Project) this.manager.getProjects().get(j);

							if (project == null) {
								break;
							}

							p.sendMessage("" + ChatColor.BLUE + j + ChatColor.WHITE + ". Name: " + ChatColor.BLUE + project.getName() + ChatColor.WHITE + ", Owner: " + ChatColor.BLUE + toPlayer(project.getOwner()).getName() + ChatColor.WHITE + ", Target: " + ChatColor.BLUE + project.getTarget() + ChatColor.WHITE + ", Collected: " + ChatColor.BLUE + project.getCollected() + ChatColor.WHITE + "$.");
						}

						p.sendMessage(prefix + "Page " + ChatColor.BLUE + page + ChatColor.WHITE + "/" + ChatColor.BLUE + pages);
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
			this.c.save(this.f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfiguration() {
		return this.c;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

		if (economyProvider != null) {
			this.economy = ((Economy) economyProvider.getProvider());
		}

		return this.economy != null;
	}
}
