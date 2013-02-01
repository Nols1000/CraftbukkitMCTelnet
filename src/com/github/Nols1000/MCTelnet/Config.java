package com.github.Nols1000.MCTelnet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private JavaPlugin plugin;
	
	private String fileName = "config.yml";
	private FileConfiguration config = null;
	private File configFile = null;
	
	public Config(String fileName, JavaPlugin plugin){
		
		this.plugin = plugin;
		
		if(fileName != "" && fileName.indexOf(".yml") > 0){
			
			this.fileName = fileName;
		}else{
			
			sendError("fileName is null or not a YAML-file. Loading: "+ fileName +".");
		}
	}
	
	public void reloadConfig() {
		
		if(configFile == null){
			
			configFile = new File(plugin.getDataFolder(), fileName);
		}
		
	    if(configFile.exists()){
	    	
	    	config = YamlConfiguration.loadConfiguration(configFile);
	    }else{
	    	
	    }	    
	    	
	    InputStream defConfigStream = plugin.getResource(fileName);
	    	
	    if (defConfigStream != null) {
	    	
	    	YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	    	config.setDefaults(defConfig);
	    	saveConfig();
	    }
	}
	
	public FileConfiguration getConfig() {
	    if(config == null) {
	    	
	        reloadConfig();
	    }
	    
	    return config;
	}
	
	public boolean saveConfig() {
	    
		if (config == null || configFile == null) {
	    	
			sendError("Config could't save. Because its null");
	    	return false;
	    }else{
	    	
	    	try {
	    	
	    		config.save(configFile);
	    	} catch (IOException ex) {
	    	
	    		sendError("Config couldn't wrote in " + configFile + ".");
	    	
	        	return false;
	    	}
	    
	    	return true;
	    }
	    
	}

	private void sendError(String error){
		
		if(error != ""){
			
			plugin.getServer().getLogger().log(Level.WARNING,"[MCTelnet]"+ error);
		}
		
	}
	
	
}
