package com.bekvon.bukkit.mctelnet;

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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
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
    private ArrayList<TelnetListener> clientHolder;
    private Thread listenerThread;
    private boolean run = false;
    int port = 8765;
    InetAddress listenAddress;
    
    public MCTelnet()
    {

    }
    
    //public MCTelnet(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
    //    super(pluginLoader, instance, desc, folder, plugin, cLoader);
    //}

    public void onDisable()
    {
        run = false;
        if(listenerSocket != null)
        {
            try {
                synchronized (listenerSocket)
                {
                    if(listenerSocket!=null)
                        listenerSocket.close();
                }
            } catch (IOException ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MCTelnet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onEnable() {
        try {
            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] - Starting Up! Version: " + this.getDescription().getVersion() + " by bekvon");
            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] - (updated by Nols1000)");
            run = true;
            this.getConfig();
            testConfig();
            if(this.getConfig().getBoolean("encryptPasswords", false))
                encryptPasswords();
            port = this.getConfig().getInt("telnetPort", port);
            try
            {
            String address = this.getConfig().getString("listenAddress",null);
            if(address!=null)
                listenAddress = InetAddress.getByName(address);
            }
            catch (Exception ex)
            {
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
            clientHolder = new ArrayList<TelnetListener>();
            listenerThread = new Thread(new Runnable() {
                public void run() {
                    acceptConnections();
                }
            });
            listenerThread.start();
            //Field cfield = Server.class.getDeclaredField("console");
            //cfield.setAccessible(true);
            Logger.getLogger("Minecraft").log(Level.INFO,"[MCTelnet] - Listening on: " + listenerSocket.getInetAddress().getHostAddress() + ":" + port);
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, "[MCTelnet] - Unable to Enable! Error: " + ex.getMessage());
            this.setEnabled(false);
        }
    }

    private void encryptPasswords()
    {
        boolean isEncrypt = this.getConfig().getBoolean("rootEncrypted",false);
        if(!isEncrypt)
        {
            this.getConfig().set("rootPass", hashPassword(this.getConfig().getString("rootPass")));
            this.getConfig().set("rootEncrypted", true);
            this.saveConfig();
        }
        ConfigurationSection users = this.getConfig().getConfigurationSection("users");
        if(users != null)
        {
            Map<String, Object> temp1 = users.getValues(false);
            Iterator<Entry<String, Object>> thisIt = temp1.entrySet().iterator();
            if(thisIt != null)
            {
                while(thisIt.hasNext())
                {
                    Entry<String, Object> thisEntry = thisIt.next();
                    if(thisEntry != null)
                    {
                        ConfigurationSection thisSection = (ConfigurationSection) thisEntry.getValue();
                        if(thisSection != null && !thisSection.getBoolean("passEncrypted", false))
                        {
                            this.getConfig().set("users."+thisEntry.getKey() +".password", hashPassword(thisSection.getString("password")));
                            this.getConfig().set("users."+thisEntry.getKey() +".passEncrypted", true);
                            this.saveConfig();
                        }
                    }
                }
            }
        }
    }

    public static String hashPassword(String password) {
        String hashword = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(password.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            hashword = hash.toString(16);
        } catch (NoSuchAlgorithmException nsae) {
        }
        return hashword;
    }

    private void acceptConnections()
    {
        while (run)
        {
            try {
                Socket client = listenerSocket.accept();
                if(client != null)
                {
                    clientHolder.add(new TelnetListener(client,this));
                    System.out.print("[MCTelnet] - Client connected: " + client.getInetAddress().toString());
                }
                for(int i = 0; i < clientHolder.size(); i++)
                {
                    TelnetListener thisListener = clientHolder.get(i);
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
            TelnetListener temp = clientHolder.get(i);
            temp.killClient();
        }
        listenerSocket = null;
        clientHolder.clear();
        clientHolder = null;
        this.setEnabled(false);
    }

    private void testConfig()
    {
        String testConfig = this.getConfig().getString("telnetPort");
        if (testConfig == null || testConfig.equals("")) {
            this.getConfig().set("telnetPort", 8765);
        }
        testConfig = this.getConfig().getString("listenAddress");
        if (testConfig == null || testConfig.equals("")) {
            this.getConfig().set("listenAddress", "0.0.0.0");
        }
        testConfig = this.getConfig().getString("rootPass");
        if (testConfig == null || testConfig.equals("")) {
            this.getConfig().set("rootPass", "abcd");
            this.getConfig().set("rootEncrypted", false);
        }
        testConfig = this.getConfig().getString("rootUser");
        if (testConfig == null || testConfig.equals("")) {
            this.getConfig().set("rootUser", "console");
        }
        testConfig = this.getConfig().getString("encryptPasswords");
        if (testConfig == null || testConfig.equals("")) {
            this.getConfig().set("encryptPasswords", true);
        }
        
        this.saveConfig();

    }
    
    private void sendMessage(String[] msg){
    	
    	for(int i = 0; i < clientHolder.size(); i ++){
    		
			TelnetListener thisListener = clientHolder.get(i);
			thisListener.sendMessage(msg);
		}	
    }
    
    private void sendMessage(String msg) {
		
    	for(int i = 0; i < clientHolder.size(); i ++){
    		
			TelnetListener thisListener = clientHolder.get(i);
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
            		testConfig();
            		if(this.getConfig().getBoolean("encryptPasswords", false))
            			encryptPasswords();
            		sender.sendMessage("[MCTelnet] Config Reloaded.");
            		sendMessage("Config Reloaded.");
            		for(int i = 0; i < clientHolder.size(); i ++)
            		{
            			TelnetListener thisListener = clientHolder.get(i);
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
            			
            			TelnetListener thisListener = clientHolder.get(i);
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
