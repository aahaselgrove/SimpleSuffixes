package me.dimpl.SimpleSuffixes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleSuffixes extends JavaPlugin {
	
	public final Logger log = Logger.getLogger("Minecraft");
	public String suffixCmd;
	public String prefixCmd;
	public int sufMax;
	public int preMax;
	public String[] allowedRegex;
	public String allowedRegexString;
	public String[] wordBlacklist;
	public String wordBlacklistString;
	public String[] wordStafftags;
	public String wordStafftagsString;
	private final ChatListener chatListener = new ChatListener(this);
	public final List<Player> Following = new ArrayList<Player>();
	String RED = ChatColor.RED.toString();
	
	CommandSender Console;

	@Override
	public void onEnable() {
		//plugin enabled
		PluginDescriptionFile pdffile = this.getDescription();
		Console = getServer().getConsoleSender();
		this.saveDefaultConfig();
		suffixCmd = this.getConfig().getString("suffix-cmd");
		prefixCmd = this.getConfig().getString("prefix-cmd");
		sufMax = this.getConfig().getInt("suffix-max-length");
		preMax = this.getConfig().getInt("prefix-max-length");
		allowedRegexString = this.getConfig().getString("allowed-regex");
		allowedRegex = allowedRegexString.split(" ");
		wordBlacklistString = this.getConfig().getString("word-blacklist");
		wordBlacklist = wordBlacklistString.split(", ");
		wordStafftagsString = this.getConfig().getString("word-stafftags");
		wordStafftags = wordStafftagsString.split(", ");
		//set up listener
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(chatListener, this);
		this.log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
	}
	
	@Override
	public void onDisable() {
		//plugin disabled
		PluginDescriptionFile pdffile = this.getDescription();
		this.log.info(pdffile.getName() + " is now disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equals("suffix")) {
			return onCommandDo(sender, args, "suf", suffixCmd, sufMax);
		}
		
		else if (cmd.getName().equals("prefix")) {
			return onCommandDo(sender, args, "pre", prefixCmd, preMax);
		}
		
		else if (cmd.getName().equals("suffixr")) {
			return onCommandDo(sender, args, "sufr", suffixCmd, 0);
		}
		
		else if (cmd.getName().equals("prefixr")) {
			return onCommandDo(sender, args, "prer", prefixCmd, 0);
		}
		
		else if (cmd.getName().equals("follow")) {
			if (sender.getName() == "CONSOLE") {
				sender.sendMessage(RED + "This command cannot be executed from the console.");				
				return false;
			}
			if (args.length == 0) {
				sender.sendMessage(RED + "Please specify a player to follow.");
				return false;
			}
			
			else if (args.length >= 2) {
				sender.sendMessage(RED + "Too many args.");
				return false;
			}
			
			Player player = getServer().getPlayer(args[0]);
			if (player == null) {
				sender.sendMessage(RED + "This player is offline.");				
				return false;
			}
			//toggle on
			if (/*the sender*/!isFollowing(player)) {
				/*for the sender*/Following.add(player);
				sender.sendMessage(ChatColor.DARK_GREEN + "You are now following" + player);
			}
			//toggle off
			else {
				/*for the sender*/Following.remove((Player) sender);
				sender.sendMessage(ChatColor.BLUE + "You are no longer following" + player);
			}
			return false;
		}
		else if (cmd.getName().equals("following")) {
			if (sender.getName() == "CONSOLE") {
				sender.sendMessage(RED + "This command cannot be executed from the console.");				
				return false;
			}
			
			if(args.length >= 2) {
				sender.sendMessage(RED + "Too many args.");				
				return false;
			}
			else if(args.length == 1) {
				Player player = getServer().getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage(RED + "This player is offline.");				
					return false;
				}
				
			}
			
			return false;
		}
		
		else return false;	
	}
	
	public boolean onCommandDo(CommandSender sender, String[] args, String type, String cmd_fix, Integer max) {
		if (type == "pre" || type == "suf") {
			if (!sender.hasPermission("simsuf." + type.substring(0, 1))) {
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to perform that command.");				
				return false;
			}
			if (sender.getName() == "CONSOLE") {
				sender.sendMessage(RED + "This command cannot be executed from the console.");				
				return false;
			}
			if (args.length == 0) {
				sender.sendMessage(RED + "Please specify a " + type + "fix.");
				return false;
			}
			else if(args.length >= 2) {
				sender.sendMessage(RED + "The suffix must be all 1 word.");
			}
			
			//String[] --> String
			String strArgs = Arrays.toString(args);
			strArgs = strArgs.substring(1, strArgs.length()-1).replaceAll(",", "");
			
			int length = Count(strArgs, sender.hasPermission("simsuf.staff"));
			
			//blacklisted word
			if (length == -1) {
				sender.sendMessage(RED + "That " + type + "fix is not allowed.");				
				return false;				
			}
			
			//test length
			if (length > max) {
				sender.sendMessage(RED + WordUtils.capitalize(type) + "fix too long (max " + max + " chars).");
				return false;
			}
			else {
				String cmd = cmd_fix.replace("%P", sender.getName());
				cmd = cmd.replace("%M", strArgs);
				log.info(cmd);
				Bukkit.getServer().dispatchCommand(Console, cmd);
				sender.sendMessage(ChatColor.AQUA + "Your " + type + "fix = \"" + strArgs + "\"");
				return true;
			}
		}
		
		else if(type == "prer" || type == "sufr") {
			if (!sender.hasPermission("simsuf.r")) {
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to perform that command.");				
				return false;
			}
			
			if (args.length == 0) {
				sender.sendMessage(RED + "Please specify a player.");
				return false;
			}
			else if (args.length >= 2) {
				sender.sendMessage(RED + "Too many args.");
				return false;
			}
			else {
				Player player = this.getServer().getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Player is offline.");				
					return false;
				}
				String cmd = cmd_fix.replace("%P", player.getName());
				cmd = cmd.replace("%M &f", "");
				cmd = cmd.replace(" %M&f", "");
				Bukkit.getServer().dispatchCommand(Console, cmd);
				sender.sendMessage(ChatColor.AQUA + player.getName() + "'s " + type.substring(0, 3) + "fix has been reset.");
				return true;
			}
		}
		return false;
	}
	
	public int Count(String string, Boolean staff) {
		//count extra chars in string
		String allowed = string;
		for (String regex : allowedRegex)
			allowed = allowed.replaceAll("\\" + regex, "");
		for (String blacklist : wordBlacklist) {
			if (allowed.toLowerCase().contains(blacklist))
				return -1;
		}
		if (staff) {
			for (String stafftags : wordStafftags) {
				if (allowed.toLowerCase().contains(stafftags))
					return -1;
			}
		}
		return allowed.length();
	}
	
	public boolean isFollowing(Player player) {
		return Following.contains(player);
	}
	
	public void stopFollowing(Player player) {
		Following.remove(player);
	}
	
}