package dev.bukkit.Nols1000.MineWire;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.ts.TelnetServer;
import com.github.Nols1000.ts.TelnetThread;
import com.github.Nols1000.ts.lg.LanguageTemplate;

public class MineWire extends JavaPlugin {
	
	private TelnetServer tServer;
	
	private Logger logger = Logger.getLogger("Minecraft-Server");
	private LogHandler handler;
	
	@Override
	public void onEnable(){
		
		this.getLogger().log(Level.INFO, this.getName()+" enabled.");
		
		
		LanguageTemplate langTemplate = new LanguageTemplate();		
		
		try {
			
			tServer = new TelnetServer(this.getConfig().getInt("server.port"), langTemplate);
			tServer.registerOnConnectHandler(new ConnectionHandler(this));
			tServer.start();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		handler = new LogHandler(tServer);
		
		logger.addHandler(handler);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args){
		
		if(lable.equalsIgnoreCase("MineWire")){
			
			if(args.length < 2){
				
				return false;
			}else if(args.length == 2){
				
				if(args[0].equalsIgnoreCase("remove")){
					
					if(sender.hasPermission("MineWire.remove.others")){
						
						try {
							
							tServer.removeUser(args[1]);
							sender.sendMessage("User "+args[1]+" was removed.");
							return true;
						} catch (NoSuchAlgorithmException e) {
							
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							
							e.printStackTrace();
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
					}
					
				}
			}else if(args.length == 3){
				
				if(args[0].equalsIgnoreCase("update")){
					
					if(sender.hasPermission("MineWire.update.others")){
						
						try {
							
							tServer.updateUser(args[1], args[2]);
							sender.sendMessage("User "+args[1]+" was updated.");
							return true;
						} catch (NoSuchAlgorithmException e) {
							
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							
							e.printStackTrace();
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
					}
				}else if(args[0].equalsIgnoreCase("remove")){
					
					if(sender.hasPermission("MineWire.remove")){
						
						try {
							
							if(tServer.loginUser(args[1], args[2])){
								
								tServer.removeUser(args[1]);
								sender.sendMessage("User "+args[1]+" was removed.");
								return true;
							}
							sender.sendMessage("Remove failed.");
						} catch (NoSuchAlgorithmException e) {
							
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							
							e.printStackTrace();
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
					}
				}else if(args[0].equalsIgnoreCase("register")){
					
					if(sender.hasPermission("MineWire.register")){
						
						try {
							
							tServer.registerUser(args[1], args[2]);
							sender.sendMessage("User "+args[1]+" was registerd.");
							return true;
						} catch (NoSuchAlgorithmException e) {
							
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							
							e.printStackTrace();
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
					}
				}
			}else if(args.length == 4){
					
				if(sender.hasPermission("MineWire.update")){
					
					try {
						
						if(tServer.loginUser(args[1], args[2])){
							
							tServer.updateUser(args[1], args[3]);
							sender.sendMessage("User "+args[1]+" was updated.");
							return true;
						}else{
							
							sender.sendMessage("Update failed.");
						}
					} catch (NoSuchAlgorithmException e) {
						
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						
						e.printStackTrace();
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
				}
			}
		}else if(lable.equalsIgnoreCase("admin")){
			
			if(args.length >= 1){
				
				String msg = args[0];
				
				if(args.length > 1){
					
					for(int i = 1;i < args.length;i++){
						
						msg += " "+args[i];
					}
				}
				
				try {
					
					tServer.sendMsg(msg+"\n\r");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
				this.getServer().getConsoleSender().sendMessage(msg);
				
				Player[] player = this.getServer().getOnlinePlayers();
				
				for(int i = 0;i < player.length;i++){
					
					if(player[i].isOp() || player[i].hasPermission("minewire.isadmin")){
						
						player[i].sendMessage(msg);
					}
				}
				
				return true;
			}
			
		}
		
		return false;
	}
	
	@Override
	public void onDisable(){
		
		handler.close();
		
		logger.removeHandler(handler); 
		
		Iterator<TelnetThread> list = tServer.getThreads().iterator();
		
		while(list.hasNext()){
			
			try {
				
				list.next().close();
			} catch (IOException e) {
				
				
			} catch (InterruptedException e) {
				
				
			}
		}
		
		try {
			
			tServer.close();
		} catch (IOException e) {
			
			
		}
		
		this.getLogger().log(Level.INFO, this.getName()+" disabled.");
	}
}
