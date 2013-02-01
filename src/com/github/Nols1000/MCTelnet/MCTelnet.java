package com.github.Nols1000.MCTelnet;

import java.io.IOException;
//import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
//import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.MCTelnet.Telnet.TelnetServer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map.Entry;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class MCTelnet extends JavaPlugin {
	
    private ServerSocket listenerSocket;
    private ArrayList<TelnetServer> clientHolder;
    private Thread listenerThread;
    private boolean run = false;
    private int port = 23;
    private InetAddress listenAddress;
    
    private Config telnetConfiguration;
    private FileConfiguration telnetConfig;
    
    public MCTelnet()
    {

    }
    
    //public MCTelnet(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
    //    super(pluginLoader, instance, desc, folder, plugin, cLoader);
    //}

    public void onDisable()
    {
        run = false;
        if(listenerSocket != null){
            
        	try{
                synchronized (listenerSocket){
                	
                    if(listenerSocket!=null)
                        listenerSocket.close();
                }
            } catch (IOException ex){
            	
                Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            }
        }
        try {
        	
            Thread.sleep(1000);
        } catch (InterruptedException ex){
        	
            Logger.getLogger(MCTelnet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onEnable(){
        
    	Config telnetConfiguration = new Config("telnet.yml", this);
    	telnetConfig = telnetConfiguration.getConfig();
    	
    	try{
        	
            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] Starting Up! Version: " + this.getDescription().getVersion() + " by bekvon");
            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] (updated by Nols1000)");
            
            run = true;

            port = telnetConfig.getInt("port");
            
            try{
            	String address = telnetConfig.getString("adress",null);
            	
            	if(address != null){
            	
                	listenAddress = InetAddress.getByName(address);
            	}
            }
            catch (Exception ex){
                System.out.println("[MCTelnet] Exception when trying to binding to custom address:" + ex.getMessage());
            }
            if(listenAddress != null)
            {
                listenerSocket = new java.net.ServerSocket(port, 10, listenAddress);
            }
            else
            {
                listenerSocket = new java.net.ServerSocket(port,10);
            }
            clientHolder = new ArrayList<TelnetServer>();
            listenerThread = new Thread(new Runnable() {
                public void run() {
                    acceptConnections();
                }
            });
            listenerThread.start();

            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] - Listening on: " + listenerSocket.getInetAddress().getHostAddress() + ":" + port);
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, "[MCTelnet] - Unable to Enable! Error: " + ex.getMessage());
            this.setEnabled(false);
        }
    }

    private void acceptConnections()
    {
        while (run)
        {
            try {
                Socket client = listenerSocket.accept();
                if(client != null)
                {
                    clientHolder.add(new TelnetServer(client,this));
                    System.out.print("[MCTelnet] - Client connected: " + client.getInetAddress().toString());
                }
                for(int i = 0; i < clientHolder.size(); i++)
                {
                    TelnetServer thisListener = clientHolder.get(i);
                    if(thisListener.isAlive() == false)
                        clientHolder.remove(i);
                }
            } catch (IOException ex) {
                run = false;
            }
        }
        Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] - Shutting Down!");
        for(int i = 0; i < clientHolder.size(); i ++)
        {
            TelnetServer temp = clientHolder.get(i);
            temp.killClient();
        }
        listenerSocket = null;
        clientHolder.clear();
        clientHolder = null;
        this.setEnabled(false);
    }
    
    private void sendMessage(String[] msg){
    	
    	for(int i = 0; i < clientHolder.size(); i ++){
    		
			TelnetServer thisListener = clientHolder.get(i);
			thisListener.sendMessage(msg);
		}	
    }
    
    private void sendMessage(String msg) {
		
    	for(int i = 0; i < clientHolder.size(); i ++){
    		
			TelnetServer thisListener = clientHolder.get(i);
			thisListener.sendMessage(msg);
		}			
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(this.isEnabled())
        {
            if(cmd.getName().equals("telnet"))
            {
            	if(args.length != 0 && args[0].equals("reload")){
            		
            		sender.sendMessage("[MCTelnet] Reloading Config...");
            		sendMessage("Reloading Config...");
            		this.reloadConfig();

            		sender.sendMessage("[MCTelnet] Config Reloaded.");
            		sendMessage("Config Reloaded.");
            		
            		for(int i = 0; i < clientHolder.size(); i ++)
            		{
            			TelnetServer thisListener = clientHolder.get(i);
            			thisListener.sendMessage("[MCTelnet] - Telnet Restarting...");
            			thisListener.killClient();
            		}
            	}else if(args.length != 0 && args[0].equals("info")){
            	   
            		PluginDescriptionFile desc = this.getDescription();
            		
            		String[] msg = new String[4];
            		String[] msgcmd = new String[3];
            		
            		msg[0] = "MCTelnet v." + desc.getVersion();
            		msg[1] = "by " + desc.getAuthors().toString();
            		msg[2] = "";
            		msg[3] = "" + desc.getDescription();
            		
            		msgcmd[0] = "[MCTelnet] MCTelnet v." + desc.getVersion();
            		msgcmd[1] = "[MCTelnet] by " + desc.getAuthors().toString();
            		msgcmd[2] = "[MCTelnet] " + desc.getDescription();
            		
            		sender.sendMessage(msgcmd);
            		sendMessage(msg);
            	}else if(args.length != 0 && args[0].equals("quit")){
             	   
            		for(int i = 0; i < clientHolder.size(); i ++){
            			
            			TelnetServer thisListener = clientHolder.get(i);
            			thisListener.killClient();
            		}
             	}
            	else{
            		
            		return true;
            	}
            	
            	return true;
            }
            
        }
        return super.onCommand(sender, cmd, commandLabel, args);
    }
}
