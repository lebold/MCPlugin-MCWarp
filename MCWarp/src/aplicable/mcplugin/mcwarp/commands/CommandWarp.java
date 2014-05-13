package aplicable.mcplugin.mcwarp.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import aplicable.mcplugin.mcwarp.MCWarp;
import aplicable.mcplugin.mcwarp.WarpLocation;

public class CommandWarp {

	private MCWarp master;
	private HashMap<Player,ArrayList<WarpLocation>> playerMap;
	private HashMap<Player,Location> lastLocationsMap;

	public CommandWarp(MCWarp master, HashMap<Player,ArrayList<WarpLocation>> playerMap, HashMap<Player,Location> lastLocationsMap){
		this.master=master;
		this.playerMap=playerMap;
		this.lastLocationsMap=lastLocationsMap;
	}

	public void issue(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length==0)
			this.correctFormat(sender);
		else if(args.length==1){
			if(args[0].equalsIgnoreCase("list")){
				if(sender.hasPermission("warp.list"))
					this.warpList(sender,(Player)sender);
				else
					this.insufficientPermissions(sender);
			}
			else if(args[0].equalsIgnoreCase("back")){
				if(sender.hasPermission("warp.back"))
					this.warpBack(sender);
				else
					this.insufficientPermissions(sender);
			}
			else if(args[0].equalsIgnoreCase("help")){
				if(sender.hasPermission("warp.help"))
					this.correctFormat(sender);
				else
					this.insufficientPermissions(sender);
			}
			else{
				if(sender.hasPermission("warp.warp"))
					this.warpTo(sender,args[0]);
				else
					this.insufficientPermissions(sender);
			}
		}
		else if(args.length==2){
			if(args[0].equalsIgnoreCase("add")){
				if(sender.hasPermission("warp.add"))
					this.warpAdd(sender,args[1]);
				else
					this.insufficientPermissions(sender);
			}
			else if(args[0].equalsIgnoreCase("del")){
				if(sender.hasPermission("warp.del"))
					this.warpDel(sender, args[1]);
				else
					this.insufficientPermissions(sender);
			}
			else if(args[0].equalsIgnoreCase("other-list")){
				if(sender.hasPermission("warp.listother"))
					this.listOther(sender,args[1]);
				else
					this.insufficientPermissions(sender);
			}
			else
				this.correctFormat(sender);
		}
		else if(args.length==3){
			if(args[0].equalsIgnoreCase("other")){
				if(sender.hasPermission("warp.warpother"))
					this.warpOther((Player)sender,args[1],args[2]);
				else
					this.insufficientPermissions(sender);
			}
			else
				this.correctFormat(sender);
		}
	}

	private void warpOther(Player sender, String playerName, String warpName){
		if(this.master.playerExists(playerName)){
			ArrayList<WarpLocation> warpLocs = this.master.getOfflineWarps(playerName);
			for(WarpLocation warpLoc:warpLocs){
				if(warpLoc.getName().equalsIgnoreCase(warpName)){
					if(warpLoc.getLoc().getWorld()==null){
						sender.sendMessage(ChatColor.YELLOW + "The world for this Warp-Location is not loaded.");
						return;
					}
					Location oldLoc = sender.getLocation();
					oldLoc.setY(oldLoc.getY()+2);
					this.lastLocationsMap.put(sender, sender.getLocation());
					Location propLoc = warpLoc.getLoc();
					Location newLoc = new Location(propLoc.getWorld(),propLoc.getX(),propLoc.getY()+1,propLoc.getZ());
					Chunk chunk = newLoc.getChunk();
					if(!chunk.isLoaded())
						chunk.load();
					while(!sender.getLocation().equals(newLoc))
						sender.teleport(newLoc);
					return;
				}
			}
			sender.sendMessage(ChatColor.YELLOW + "Specified Warp-Location does not exist.");
		}
		else{
			sender.sendMessage(ChatColor.YELLOW + "Specified player could not be found.");
		}
	}

	private void listOther(CommandSender sender, String playerName){

		if(!this.master.playerExists(playerName)){
			sender.sendMessage(ChatColor.YELLOW + "The specified player does not exist.");
			return;
		}
		ArrayList<WarpLocation> warpLocs = this.master.getOfflineWarps(playerName);
		if(warpLocs.isEmpty()){
			sender.sendMessage(ChatColor.AQUA + playerName + ChatColor.YELLOW + "has no warps.");
			return;
		}
		sender.sendMessage("-----------------------------------------------------");
		sender.sendMessage(ChatColor.AQUA + playerName + ChatColor.WHITE + "'s " + "Warp Locations:");
		sender.sendMessage("-----------------------------------------------------");
		for(WarpLocation loc:warpLocs){
			int x = (int)loc.getLoc().getX();
			int y = (int)loc.getLoc().getY();
			int z = (int)loc.getLoc().getZ();
			if(loc.getWorld().endsWith("_nether"))
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.RED + "(" + x + "," + y + "," + z + ")");
			else if(loc.getWorld().endsWith("_the_end"))
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.BLUE + "(" + x + "," + y + "," + z + ")");
			else
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.WHITE + "(" + x + "," + y + "," + z + ")");
		}
	}

	private void warpDel(CommandSender sender, String warpName){
		Player player = (Player)sender;
		if(this.master.removeWarpLoc(player, warpName))
			sender.sendMessage(ChatColor.WHITE + "Location removed.");
		else
			sender.sendMessage(ChatColor.YELLOW + "The Warp-Location specified could not be found.");
	}

	private void warpAdd(CommandSender sender, String warpName){
		Player player = (Player)sender;
		Location loc = player.getLocation();
		if(warpName.equalsIgnoreCase("list")||warpName.equalsIgnoreCase("back")||warpName.equalsIgnoreCase("help")){
			sender.sendMessage(ChatColor.RED + "The name you have entered for a Warp-Location is invalid.");
			return;
		}
		ArrayList<WarpLocation> warpLocs = this.playerMap.get(player);
		if(warpLocs==null)
			warpLocs = new ArrayList<WarpLocation>();
		for(WarpLocation w:warpLocs){
			if(w.getName().equalsIgnoreCase(warpName)){
				sender.sendMessage(ChatColor.YELLOW + "This Warp-Location already exists!");
				return;
			}
		}
		WarpLocation warpLoc = new WarpLocation(warpName,player.getWorld().getName(),loc);
		this.master.addWarpLoc(player, warpLoc);
		sender.sendMessage(ChatColor.WHITE + "Location added.");
	}

	private void warpTo(CommandSender sender, String warpName){
		Player player = (Player)sender;
		ArrayList<WarpLocation> locs = this.playerMap.get(player);
		if(locs==null)
			locs = new ArrayList<WarpLocation>();
		for(WarpLocation loc:locs){
			if(loc.getName().equalsIgnoreCase(warpName)){
				this.lastLocationsMap.put(player, player.getLocation());
				Chunk chunk = loc.getLoc().getChunk();
				if(!chunk.isLoaded())
					chunk.load();
				World world = loc.getLoc().getWorld();
				double x = loc.getLoc().getX();
				double y = loc.getLoc().getY() + 1;
				double z = loc.getLoc().getZ();
				while(!player.getLocation().equals(new Location(world,x,y,z)))
					player.teleport(new Location(world,x,y,z));
				return;
			}
		}
		sender.sendMessage(ChatColor.YELLOW + "The specified warp-location does not exist.");
	}

	private void warpBack(CommandSender sender){
		Player player = (Player)sender;
		Location oldLoc = this.lastLocationsMap.get(player);

		if(oldLoc==null){
			sender.sendMessage(ChatColor.YELLOW + "Previous warp-location could not be found.");
			return;
		}
		Location oldLoc2 = new Location(player.getWorld(),player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ());
		Chunk chunk = oldLoc.getChunk();
		if(!chunk.isLoaded())
			chunk.load();
		World world = oldLoc.getWorld();
		double x = oldLoc.getX();
		double y = oldLoc.getY() + 1;
		double z = oldLoc.getZ();
		while(!player.getLocation().equals(new Location(world,x,y,z)))
			player.teleport(new Location(world,x,y,z));
		this.lastLocationsMap.put(player, oldLoc2);
	}

	private void warpList(CommandSender sender, Player player){
		ArrayList<WarpLocation> locs = this.playerMap.get(player);
		if(locs==null)
			locs = new ArrayList<WarpLocation>();
		sender.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "Warp Locations:");
		sender.sendMessage("");
		for(WarpLocation loc:locs){
			int x = (int)loc.getLoc().getX();
			int y = (int)loc.getLoc().getY();
			int z = (int)loc.getLoc().getZ();
			if(loc.getWorld().endsWith("_nether"))
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.RED + "(" + x + "," + y + "," + z + ")");
			else if(loc.getWorld().endsWith("_the_end"))
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.BLUE + "(" + x + "," + y + "," + z + ")");
			else
				sender.sendMessage(ChatColor.GOLD + loc.getName() + ChatColor.WHITE + "(" + x + "," + y + "," + z + ")");
		}
	}

	private void insufficientPermissions(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "You do not have sufficient permissions to issue this command.");
	}

	private void correctFormat(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "The correct format for this command is:");
		if(sender.hasPermission("warp.warp"))
			sender.sendMessage(ChatColor.RED + "/warp <location-name>");
		if(sender.hasPermission("warp.back"))
			sender.sendMessage(ChatColor.RED + "/warp back");
		if(sender.hasPermission("warp.list"))
			sender.sendMessage(ChatColor.RED + "/warp list");
		if(sender.hasPermission("warp.add")&&sender.hasPermission("warp.del"))
			sender.sendMessage(ChatColor.RED + "/warp <add/del> <location-name>");
		if(sender.hasPermission("warp.warpother"))
			sender.sendMessage(ChatColor.RED + "/warp other <player-name> <location-name>");
		if(sender.hasPermission("warp.listother"))
			sender.sendMessage(ChatColor.RED + "/warp other-list <player-name>");
		if(sender.hasPermission("warp.help"))
			sender.sendMessage(ChatColor.RED + "/warp help");
	}
}
