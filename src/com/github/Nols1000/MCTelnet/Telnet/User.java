package com.github.Nols1000.MCTelnet.Telnet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.MCTelnet.Config;

public class User {
	
	private final static String LOG_FILE = "user.log";
	
	private int ID;
	private IPv4 IP;
	private String password;
	private String name;
	private int loginFails;
	private TelnetServer server;
	private JavaPlugin plugin;
	
	private FileConfiguration userList;
	private Config userConfiguration;
	private FileConfiguration userConfig;
	
	private boolean isAuth;
	private boolean isRoot;

	
	public User(String name, IPv4 IP, TelnetServer server, JavaPlugin plugin){
		
		this.plugin = plugin;
		
		this.name = name;
		this.IP = IP;
		
		sendError(this.name);
		
		this.server = server;
		
		userList = new Config("userList.yml", plugin).getConfig();
		ID = userList.getInt("user.name."+ name +".id");
		
		userConfiguration = new Config(ID +".yml", plugin);
		userConfig = userConfiguration.getConfig();
		
		loadConfigData();
		
		logToFile("Connected to "+ IP.toString() +"|"+ name);
	}

	public User(int ID, IPv4 IP, TelnetServer server, JavaPlugin plugin){
		
		this.plugin = plugin;
		
		this.ID = ID;
		this.IP = IP;
		
		this.server = server;
		
		userList = new Config("userList.yml", plugin).getConfig();
		name = userList.getString("user.id."+ ID +".name");
		
		userConfiguration = new Config(ID +".yml", plugin);
		userConfig = userConfiguration.getConfig();
		
		loadConfigData();
		
		logToFile("Connected to "+ IP.toString() +"|"+ name);
	}
	
	private void loadConfigData(){
		
		if(userConfig.contains("user")){
			
			if(userConfig.contains("user.root")){
				
				isRoot = userConfig.getBoolean("user.root");
			}else{
				
				userConfig.set("user.root", false);
			}
			
			if(userConfig.contains("user.password")){
				
				if(userConfig.contains("user.password.encrypted")){
					
				}else{
					
					userConfig.set("user.password.encrypted", true);
				}
				
				if(userConfig.contains("user.password.password")){
					
					if(userConfig.getBoolean("user.password.encrypted")){
						
						password = userConfig.getString("user.password.password");
					}else{
						
						password = encryptPass(userConfig.getString("user.password.password"));
						userConfig.set("user.password.password", password);
						userConfig.set("user.password.encrypted", true);
					}
					
				}else{
					
					userConfig.set("user.password.password", encryptPass("pass1234"));
				}
			}
			
			if(userConfig.contains("user.permissions")){
				
			}else{
				
				List<String> permissions = null;
				
				permissions.add("foo.bar");
				permissions.add("mctelnet.reload");
				
				userConfig.set("user.permissions", permissions);
			}
			
		}else{
			
			List<String> permissions = null;
			
			userConfig.set("user.root", false);
			userConfig.set("user.password.encrypted", true);
			userConfig.set("user.password.password", encryptPass("pass1234"));
			
			permissions.add("foo.bar");
			permissions.add("mctelnet.reload");
			userConfig.set("user.permissions", permissions);
		}
		
		userConfiguration.saveConfig();		
	}
	
	private void logToFile(String msg){
		
		try {
			
			FileWriter log = new FileWriter(LOG_FILE);
			
			log.write("["+ new java.util.Date() +"]"+ msg);
			log.flush();
			
			log.close();
			
		} catch (IOException e) {
			
			sendError(e.getMessage());
		}
	}

	public boolean login(String pass){
		
		if(encryptPass(pass).equals(password)){
			
			logToFile(name +"|"+ IP.toString() +" logged in.");
			isAuth = true;
			return true;
		}else if(loginFails <= 3){
			
			logToFile(name +"|"+ IP.toString() +" failed logging in!");
			return false;
		}else{
			
			server.killClient("You've failed more than 3 logins.");
			return false;
		}
	}
	
	public boolean loginAsRoot(String pass){
		
		if(encryptPass(pass).equals(password)){
			
			logToFile(name +"|"+ IP.toString() +" got root access.");
			isRoot = true;
			return true;
		}else{
			
			logToFile(name +"|"+ IP.toString() +" failed get root access!");
			return false;
		}
	}

	public boolean isAuth(){
		
		return isAuth;	
	}
	
	public boolean isRoot(){
		
		return isRoot;		
	}
	
	public boolean hasPermissions(String cmd){
		
		String[] strPerms = (String[]) userConfig.getStringList("permissions").toArray();
		
		for(int i = 0; i < strPerms.length; i++){
			
			if(strPerms[i].equalsIgnoreCase(cmd) || strPerms[i].equalsIgnoreCase("*")){
				
				return true;
			}
		}
		
		return false;
	}
	
	public int getID(){
		
		return ID;
	}
	
	public String getName(){
		
		return name;
	}
	
	public IPv4 getIP(){
		
		return IP;
	}
	
	private static String encryptPass(String password)
	{
	    String sha1 = "";
	    try
	    {
	        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(password.getBytes("UTF-8"));
	        sha1 = byteToHex(crypt.digest());
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return sha1;
	}

	private static String byteToHex(final byte[] hash)
	{
	    Formatter formatter = new Formatter();
	    for (byte b : hash)
	    {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}

	private void sendError(String error){
		
		if(error != ""){
			
			plugin.getServer().getLogger().log(Level.WARNING,"[MCTelnet]"+ error);
		}	
	}
}
