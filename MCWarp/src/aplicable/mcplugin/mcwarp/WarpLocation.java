package aplicable.mcplugin.mcwarp;

import org.bukkit.Location;

public class WarpLocation {
	private String name;
	private String world;
	private Location loc;
	public WarpLocation(String name, String world, Location loc){
		this.name=name;
		this.world=world;
		this.loc=loc;
	}
	public String getName(){
		return this.name;
	}
	public String getWorld(){
		return this.world;
	}
	public Location getLoc(){
		return this.loc;
	}
}
