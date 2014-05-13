package aplicable.mcplugin.mcwarp;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import aplicable.mcplugin.mcwarp.commands.CommandWarp;
import aplicable.mcplugin.mcwarp.listeners.PlayerListener;
import aplicable.mcplugin.mcwarp.sql.SQLManager;

public class MCWarp extends JavaPlugin {

	private HashMap<Player,ArrayList<WarpLocation>> playerMap;
	private HashMap<Player,Location> lastLocationMap;
	private SQLManager sqlmanager;
	private CommandWarp warpCommand;

	public void onEnable(){
		this.sqlmanager = new SQLManager(this);
		this.playerMap = new HashMap<Player,ArrayList<WarpLocation>>();
		this.lastLocationMap = new HashMap<Player,Location>();
		this.warpCommand = new CommandWarp(this,playerMap,lastLocationMap);
		for(Player player:Bukkit.getServer().getOnlinePlayers()){
			this.welcome(player);
		}
		this.registerListeners();
	}
	public void onDisable(){
		Player[] p = new Player[0];
		Player[] playerList = this.playerMap.keySet().toArray(p);
		for(int n=playerList.length-1;n>=0;n--)
			this.dismiss(playerList[n]);
	}
	public boolean hasPlayer(Player p){
		return this.playerMap.containsKey(p)&&this.lastLocationMap.containsKey(p);
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(label.equalsIgnoreCase("warp")){
			this.warpCommand.issue(sender,cmd,label,args);
		}
		return false;
	}
	public void welcome(Player player){
		if(player.isWhitelisted()){
			if(!sqlmanager.isPlayerLogged(player.getName())){
				sqlmanager.logPlayer(player.getName());
			}
			ArrayList<WarpLocation> warpLocs = this.sqlmanager.getLocations(player.getName());
			for(WarpLocation loc:warpLocs){
				for(World w:this.getServer().getWorlds()){
					if(loc.getWorld().equals(w.getName()))
						loc.getLoc().setWorld(w);
				}
			}
			this.playerMap.put(player, warpLocs);
		}
	}
	public void dismiss(Player player){
		if(player.isWhitelisted()){
			this.playerMap.remove(player);
		}
	}
	public boolean playerExists(String playername){
		return this.sqlmanager.isPlayerLogged(playername);
	}
	public ArrayList<WarpLocation> getOfflineWarps(String playername){
		ArrayList<WarpLocation> locs = this.sqlmanager.getLocations(playername);
		for(WarpLocation loc:locs){
			for(World w:this.getServer().getWorlds()){
				if(w.getName().equals(loc.getWorld()))
					loc.getLoc().setWorld(w);
			}
		}
		return locs;
	}
	public void addWarpLoc(Player player, WarpLocation loc){
		ArrayList<WarpLocation> playerLocs = this.playerMap.get(player);
		if(playerLocs==null)
			playerLocs = new ArrayList<WarpLocation>();
		playerLocs.add(loc);
		this.playerMap.put(player, playerLocs);
		this.sqlmanager.setLocations(player.getName(), playerLocs);
	}
	public boolean removeWarpLoc(Player player, String warpName){
		ArrayList<WarpLocation> playerLocs = this.playerMap.get(player);
		if(playerLocs==null)
			playerLocs = new ArrayList<WarpLocation>();
		for(int n=playerLocs.size()-1;n>=0;n--){
			String name = playerLocs.get(n).getName();
			if(name.equalsIgnoreCase(warpName)){
				playerLocs.remove(n);
				this.sqlmanager.setLocations(player.getName(), playerLocs);
				return true;
			}
		}
		return false;
	}
	private void registerListeners(){
		PluginManager pm = super.getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this),this);
	}
}
