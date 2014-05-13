package aplicable.mcplugin.mcwarp.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import aplicable.mcplugin.mcwarp.MCWarp;
import aplicable.mcplugin.mcwarp.WarpLocation;

public class SQLManager {
	private MCWarp master;
	private Connection connection;
	private String host;
	private String username;
	private String password;
	private String database;
	public SQLManager(MCWarp master){
		this.master=master;
		FileConfiguration config = this.master.getConfig();
		this.host = config.getString("host");
		this.username = config.getString("username");
		this.password = config.getString("password");
		this.database = config.getString("database");
		establishConnection();
		prepareDatabase();
	}
	private void establishConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.connection=DriverManager.getConnection("jdbc:mysql://"+this.host+":3306/"+database,this.username,this.password);
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
		}catch(ClassNotFoundException e){
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
		}catch(IllegalAccessException e){
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
		}catch(InstantiationException e){
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
		}
	}
	private void prepareDatabase(){
		if(!this.connected())
			return;
		try{
			Statement query = this.connection.createStatement();
			query.executeUpdate("CREATE TABLE IF NOT EXISTS MCWarpSQL (NAME VARCHAR(255),LOCATIONS VARCHAR(255))");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWarp").severe("Error in Database prep.");
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
	private boolean connected(){
		if(this.connection==null){
			Logger.getLogger("Minecraft.MCWarp").severe("Could not connect to database.");
			return false;
		}
		else
			return true;
	}
	public boolean isPlayerLogged(String playername){
		if(!this.connected())
			return false;
		try{
			Statement query = this.connection.createStatement();
			ResultSet set = query.executeQuery("SELECT * FROM MCWarpSQL WHERE NAME='"+playername+"'");
			return set.first();
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWarp").severe("Error in locating Player.");
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
		return false;
	}
	public void logPlayer(String playername){
		if(!this.connected())
			return;
		try{
			Statement query = this.connection.createStatement();
			query.executeUpdate("INSERT INTO MCWarpSQL VALUES ('"+playername+"','')");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWarp").severe("Error in logging player.");
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
	public boolean setLocations(String playername,ArrayList<WarpLocation> locations){
		if(!this.connected())
			return false;
		String locationsStr = "";
		for(WarpLocation loc:locations){
			locationsStr += loc.getName();
			locationsStr += ":";
			locationsStr += loc.getWorld();
			locationsStr += ":";
			locationsStr += loc.getLoc().getX();
			locationsStr += ":";
			locationsStr += loc.getLoc().getY();
			locationsStr += ":";
			locationsStr += loc.getLoc().getZ();
			locationsStr += "|";
		}
		if(locationsStr.length()>0&&locationsStr.substring(locationsStr.length()-1).equalsIgnoreCase("|"))
			locationsStr = locationsStr.substring(0,locationsStr.length()-1);
		try{
			Statement query = this.connection.createStatement();
			query.executeUpdate("UPDATE MCWarpSQL SET LOCATIONS='"+locationsStr+"' WHERE NAME ='"+playername+"'");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWarp").severe("Error in updating player.");
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
		
		return true;
	}
	public ArrayList<WarpLocation> getLocations(String playername){
		if(!this.connected())
			return new ArrayList<WarpLocation>();
		try{
			Statement query = this.connection.createStatement();
			ResultSet set = query.executeQuery("SELECT LOCATIONS FROM MCWarpSQL WHERE NAME ='"+playername+"'");
			set.next();
			String locs = set.getString(1);
			String[] splitLocs = locs.split("\\|");
			ArrayList<WarpLocation> warpLocations = new ArrayList<WarpLocation>();
			for(String splitLoc:splitLocs){
				if(splitLoc.equalsIgnoreCase(""))
					continue;
				String[] locStr = splitLoc.split(":");
				if(locStr.length!=5)
					continue;
				String name = locStr[0];
				String world = locStr[1];
				double x = Double.parseDouble(locStr[2]);
				double y = Double.parseDouble(locStr[3]);
				double z = Double.parseDouble(locStr[4]);
				Location loc = new Location(null,x,y,z);
				WarpLocation warpLoc = new WarpLocation(name,world,loc);
				warpLocations.add(warpLoc);
			}
			return warpLocations;
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWarp").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWarp").severe("Error in fetching locations.");
			Logger.getLogger("Minecraft.MCWarp").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
		return new ArrayList<WarpLocation>();
	}
}
