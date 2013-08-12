package dev.bukkit.Nols1000.MineWire;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.ts.TelnetThread;
import com.github.Nols1000.ts.event.OnCommandEvent;
import com.github.Nols1000.ts.event.OnConnectHandler;

public class ConnectionHandler extends OnConnectHandler {

	private JavaPlugin plugin;
	
	public ConnectionHandler(JavaPlugin plugin){
		
		this.plugin = plugin;
	}
	
	public OnCommandEvent getEvent(TelnetThread thread){
    	
    	return new VirtuellTerminal(plugin, thread);
    }
}
