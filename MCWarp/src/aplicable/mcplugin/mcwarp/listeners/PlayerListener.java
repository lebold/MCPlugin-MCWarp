package aplicable.mcplugin.mcwarp.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import aplicable.mcplugin.mcwarp.MCWarp;

public class PlayerListener implements Listener{
	MCWarp master;
	public PlayerListener(MCWarp master){
		this.master=master;
	}
	@EventHandler
	public void onLogin(PlayerLoginEvent event){
		master.welcome(event.getPlayer());
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		master.welcome(event.getPlayer());
	}
	@EventHandler
	public void onKick(PlayerKickEvent event){
		master.dismiss(event.getPlayer());
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		master.dismiss(event.getPlayer());
	}
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		if(!master.hasPlayer(event.getPlayer()))
				master.welcome(event.getPlayer());
	}
}
