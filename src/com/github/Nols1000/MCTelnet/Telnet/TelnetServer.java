package com.github.Nols1000.MCTelnet.Telnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.MCTelnet.Config;

public class TelnetServer extends Handler implements CommandSender {

	public void killClient(String msg) {
		
		
	}

    private boolean run;
    private Config configuration;
    private FileConfiguration config;
    private String prefix = "# ";

    private Thread listenThread;
    private Socket clientSocket;
    private BufferedReader instream;
    private BufferedWriter outstream;
    private String ip;
    
    private User user;
    
    private JavaPlugin plugin;

    public TelnetServer(Socket inSock, JavaPlugin plugin){
    	
    	run = true;
    	
    	configuration = new Config("config.yml", plugin);
    	config = configuration.getConfig();
        
        clientSocket = inSock;
        
        this.plugin = plugin;
        
        ip = clientSocket.getInetAddress().toString();
        
        listenThread = new Thread(new Runnable() {
            public void run() {
                
            	main();
            }
        });
        
        listenThread.start();
    }

    private void main()
    {
        try {
        	
            instream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outstream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            handShake(new IPv4(clientSocket.getInetAddress().toString()));
        }catch (IOException ex){
        	
            sendError(ex.getMessage());
            run = false;
            
        }
        if(!clientSocket.getInetAddress().isLoopbackAddress())
        {
            handShake(new IPv4(ip));
        }
    }
    
    private void handShake(IPv4 IP){
    	
    	List<String> msg_list = config.getStringList("msg.welcome");
    	
    	Object[] msgObj = msg_list.toArray();
    	String[] msg = new String[msgObj.length];
    	
    	for(int i = 0; i < msgObj.length; i++){
    		
    		msg[i] = String.valueOf(msgObj[i]);
    	}
    	
    	sendMessage(msg);
    	
    	login(IP);
    	
    }
    
    private void login(IPv4 IP){
    	
    	try {
    		
			outstream.write(prefix +"UserID: ");
			outstream.flush();
			String user = instream.readLine();
			outstream.write(prefix +"\r\n");
			outstream.write(prefix +"Password: ");
			outstream.flush();
			String pass = instream.readLine();
			outstream.write(prefix +"\r\n");
			outstream.write(prefix +"\r\n");
			outstream.flush();
			
			User loginUser = new User(user, IP, this, plugin);
			
			if(loginUser.login(pass)){
				
				outstream.write(prefix +"Login as "+ loginUser.getName() + ":"+ loginUser.getID() +"\r\n");
				outstream.write(prefix +"From: "+ loginUser.getIP().toString() +"\r\n");
				outstream.write(prefix +"\r\n");
				outstream.flush();
				
				this.user = loginUser;
				
				commandLoop(loginUser);
			}else{
				
				outstream.write(prefix +"Login failed. \r\n");
				outstream.flush();
				login(IP);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private void commandLoop(User sessionUser) {
        
		try {
			
            if(sessionUser.isAuth()){
            	
                while (run && clientSocket.isConnected()){
                	
                	outstream.write(prefix);
                	outstream.flush();
                	
                    String command;
                    command = instream.readLine();
                    
                    outstream.write("\r\n");
                    
                    //sendInfo("User "+ sessionUser.getID() + " want performe command: "+ command);
                    sendInfo(sessionUser.getID()+"");
                    sendInfo(sessionUser.getIP().toString());
                    sendInfo(arrayToString(command.getBytes("utf-8"))[0]);
                    sendInfo(arrayToString(command.getBytes("utf-8"))[1]);
                    
                    if(command.startsWith("sudo ")){
                        
                    	if(sessionUser.isRoot()){
                    		
                    		command = command.substring(5);
                    	}else{
                    		
                    		outstream.write("$ \r\n");
                    		outstream.write("$ Password: ");
                    		outstream.flush();
                    		String pass = instream.readLine();
                    		outstream.write("\r\n");
                    		outstream.flush();
                    		
                    		if(sessionUser.loginAsRoot(pass)){
                    			
                    			prefix = "$";
                    			outstream.write("$ \r\n");
                    			outstream.write("$ Root-Login succeed.\r\n");
                    			outstream.write("$ You've now root access.\r\n");
                    			outstream.flush();
                    		}else{
                    			
                    			outstream.write("$ \r\n");
                    			outstream.write("$ Root-Login failed.\r\n");
                    			outstream.flush();
                    		}
                    	}
                    }
                    
                    if (!clientSocket.isClosed()) {
                        if (sessionUser.isRoot() || sessionUser.hasPermissions(command)) {
                        	
                            plugin.getServer().dispatchCommand(this, command);
                        }else if(!command.equals("")){
                                
                        	outstream.write(prefix +"You do not have permission to use this command...\r\n");
                            outstream.flush();
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
	
	public String[] arrayToString(byte[] arg){
		
		String erg[] = new String[2];
		
		for(int i = 0; arg.length > i; i++){
			
			if( (int) arg[i] > 0){
				
				erg[0] = erg[0] + arg[i];
				erg[1] = erg[1] + String.valueOf((char) (arg[i] &  0x00ff));
			}
		}
	
		return erg;	
	}

    public boolean isAlive(){
    	
        return run;
    }

    public void killClient()
    {
        try {
            run = false;
            outstream.write(prefix +"Thank you using MCTelnet.\r\n");
            outstream.flush();
            clientSocket.close();
        } catch (IOException ex) {
        }
    }

    private void shutdown() {
        try {
            run = false;
            Logger.getLogger("Minecraft").removeHandler(this);
            sendInfo("[MCTelnet] Closing connection: " + ip);
            if(!clientSocket.isClosed())
            {
                outstream.write(prefix +"Thank you using MCTelnet.");
                outstream.flush();
                clientSocket.close();
            }
            plugin = null;
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            run = false;
        }
    }

    @Override
    public void publish(LogRecord record) {
        try {
            if(!clientSocket.isClosed()){
            	
            	if(record.getMessage().indexOf("[MCTelnet]") < 0){
            		
            	}
            	outstream.write(ChatColor.stripColor(record.getMessage())+"\r\n");
                outstream.flush(); 
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void flush() {
        if(clientSocket.isConnected())
        {
            try {
                outstream.flush();
            } catch (IOException ex) {
            }
        }
    }


    public void sendMessage(String string) {
        if(clientSocket.isConnected())
        {
            try {
            	
            	string = ChatColor.stripColor(string);
            	outstream.write(prefix + string +"\r\n");
            	outstream.flush();
                
            } catch (IOException ex) {
            }
        }
    }

    public boolean isOp() {
    	
        return user.isRoot();
    }

    public boolean isPlayer() {
        return false;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public void close() throws SecurityException {
        shutdown();
    }

/*    private void sendTelnetCommand(int command, int option)
    {
        if(clientSocket.isConnected())
        {
            try {
                String tcmd = ("" + ((char) 255) + ((char) command) + ((char) option));
                outstream.write(tcmd);
                outstream.flush();
            } catch (IOException ex) {
            }
        }
    }*/

	private void sendError(String error){
		
		if(error != ""){
			
			plugin.getServer().getLogger().log(Level.WARNING,"[MCTelnet]"+ error);
		}
		
	}
	
	private void sendInfo(String info){
		
		if(info != ""){
			
			plugin.getServer().getLogger().log(Level.INFO, "[MCTelnet]" + info);
		}
	}
    
    public String getName() {
        
    	return user.getName();
    }

    public boolean isPermissionSet(String string) {
        return true;
    }

    public boolean isPermissionSet(Permission prmsn) {
        return true;
    }

    public boolean hasPermission(String string) {
        return true;
    }

    public boolean hasPermission(Permission prmsn) {
        return true;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return null;
    }

    public void removeAttachment(PermissionAttachment pa) {
        
    }

    public void recalculatePermissions() {
    	
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    public void setOp(boolean bln) {
    	
    }

	public void sendMessage(String[] string) {		
		
		if(clientSocket.isConnected()){
            try {
            	
            	for(int i = 0;i < string.length;i++){
            		
            		string[i] = ChatColor.stripColor(string[i]);
            		outstream.write(prefix + string[i] +"\r\n");            		
            	}
            	
            	outstream.flush();
            	
            } catch (IOException ex) {
            	
            }
        }	
	}

}

