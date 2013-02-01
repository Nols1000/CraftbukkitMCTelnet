/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.mctelnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Administrator
 */
public class TelnetListener extends Handler implements CommandSender {

    private boolean run;
    private boolean isAuth;
    private String authUser;
    private boolean isRoot;
    private Server server;
    

    private Thread listenThread;
    Socket clientSocket;
    BufferedReader instream;
    BufferedWriter outstream;
    MCTelnet parent;
    String ip;
    String passRegex = "[^a-zA-Z0-9\\-\\.\\_]";
    String commandRegex = "[^a-zA-Z0-9 \\-\\.\\_\\\"]";

    public TelnetListener(Socket inSock, MCTelnet iparent)
    {
    	server = iparent.getServer();
    	
        run = true;
        clientSocket = inSock;
        parent = iparent;
        passRegex = parent.getConfig().getString("passwordRegex",passRegex);
        commandRegex = parent.getConfig().getString("commandRegex",commandRegex);
        ip = clientSocket.getInetAddress().toString();
        listenThread = new Thread(new Runnable() {
            public void run() {
                mainLoop();
            }
        });
        listenThread.start();
    }

    private void mainLoop()
    {
        try {
            instream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outstream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            //sendTelnetCommand(251,3);
            //sendTelnetCommand(253,3);
            //sendTelnetCommand(251,34);
            //sendTelnetCommand(253,34);
            //sendTelnetCommand(252,1);
            //sendTelnetCommand(253,1);
            outstream.write("MCTelnet " + parent.getDescription().getVersion() + " by bekvon \r\n");
            outstream.write("(updated by Nols1000) \r\n");
            outstream.write("You are connect with "+server.getIp()+":"+server.getPort() + "\r\n");
            outstream.write(server.getName() + " ("+server.getOnlinePlayers().length+"/" + server.getMaxPlayers() + ")\r\n");
            outstream.write(server.getVersion() + "\r\n");
            outstream.flush();
        } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            run = false;
        }
        if(!clientSocket.getInetAddress().isLoopbackAddress() || !parent.getConfig().getBoolean("allowAuthlessLocalhost", false))
        {
            authenticateLoop();
        }
        else
        {
            isAuth = true;
            isRoot = true;
            authUser = parent.getConfig().getString("rootUser");
        }
        commandLoop();
        shutdown();
    }

    private void authenticateLoop()
    {
        int retrys=0;
        while(run && clientSocket.isConnected() && isAuth == false)
        {
            try {
                outstream.write("Username:");
                outstream.flush();
                String username = instream.readLine().replaceAll(passRegex, "");
                //sendTelnetCommand(251,1);
                //sendTelnetCommand(254,1);
                outstream.write("Password:");
                outstream.flush();
                String pw = instream.readLine().replaceAll(passRegex, "");
                outstream.write("\r\n");
                outstream.write("\r\n");
                //sendTelnetCommand(252,1);
                //sendTelnetCommand(253,1);
                String rootuser = parent.getConfig().getString("rootUser");
                String rootpass = parent.getConfig().getString("rootPass");
                if (rootuser != null && !rootuser.equals("") && username.equals(rootuser)) {
                    if(parent.getConfig().getBoolean("rootEncrypted",false))
                    {
                        pw = MCTelnet.hashPassword(pw);
                    }
                    if (rootpass != null && !rootpass.equals("") && pw.equals(rootpass)) {
                        authUser = rootuser;
                        isAuth = true;
                        isRoot = true;
                    }
                } else {
                    ConfigurationSection parentnode = parent.getConfig().getConfigurationSection("users");
                    if (parentnode != null) {
                    	ConfigurationSection userpath = parentnode.getConfigurationSection(username);
                        if (userpath != null) {
                            String userpw = userpath.getString("password");
                            if(userpath.getBoolean("passEncrypted", false))
                            {
                                pw = MCTelnet.hashPassword(pw);
                            }
                            if (pw.equals(userpw)) {
                                authUser = username;
                                isAuth = true;
                            }
                        }
                    }
                }
                if (isAuth) {
                	
                    outstream.write("Logged in as " + authUser + "!\r\n");
                    outstream.write("\r\n");
                    outstream.flush();
                } else {
                	
                    outstream.write("Invalid Username or Password!\r\n");
                    outstream.write("\r\n");
                    outstream.flush();
                }
                retrys++;
                if (retrys == 3 && isAuth == false) {
                    try
                    {
                        outstream.write("Too many failed login attempts!");
                        outstream.flush();
                    } catch (Exception ex)
                    { }
                    return;
                }
            } catch (Exception ex) {
                run = false;
                authUser = null;
                isAuth = false;
                isRoot = false;
            }
        }
    }

    private void commandLoop() {
        try {
            if (isAuth) {
                ConfigurationSection userpath;
                String commands = "";
                String[] validCommands = new String[0];
                if (isRoot) {
                    Logger.getLogger("Minecraft").addHandler(this);
                } else {
                    userpath = parent.getConfig().getConfigurationSection("users").getConfigurationSection(authUser);
                    if (userpath != null) {
                        if (userpath.getString("log", "false").equals("true")) {
                            Logger.getLogger("Minecraft").addHandler(this);
                        }
                        commands = userpath.getString("commands");
                        if (commands != null) {
                            validCommands = commands.split("\\,");
                        }
                        for (int i = 0; i < validCommands.length; i++) {
                            String thisCommand = validCommands[i];
                            validCommands[i] = thisCommand.trim();
                        }
                    }
                }
                while (run && clientSocket.isConnected() && isAuth) {
                    String command = "";
                    command = instream.readLine().replaceAll(commandRegex, "");
                    if (command.equals("exit")) {
                        run = false;
                        clientSocket.close();
                        return;
                    }
                    boolean elevate = false;
                    boolean allowCommand = false;
                    if(command.startsWith("sudo "))
                    {
                        if(!isRoot)
                        {
                            elevate = true;
                        }
                        command = command.substring(5);
                    }
                    if (!isRoot) {
                        for (int i = 0; i < validCommands.length; i++) {
                            if (command.equals(validCommands[i]) || (command.startsWith(validCommands[i] + " "))) {
                                allowCommand = true;
                                i = validCommands.length;
                            }
                        }
                    }
                    if(elevate && !allowCommand)
                    {
                        //sendTelnetCommand(251,1);
                        //sendTelnetCommand(254,1);
                        outstream.write("Root Password:");
                        outstream.flush();
                        String pw = instream.readLine().replaceAll(passRegex, "");
                        outstream.write("\r\n");
                        outstream.write("\r\n");
                        //sendTelnetCommand(252,1);
                        //sendTelnetCommand(253,1);
                        String rootpass = parent.getConfig().getString("rootPass");
                        if(parent.getConfig().getBoolean("rootEncrypted", false))
                        {
                            pw=MCTelnet.hashPassword(pw);
                        }
                        if (pw.equals(rootpass)) {
                            allowCommand = true;
                        }
                    }
                    if (!clientSocket.isClosed()) {
                        if (isRoot || allowCommand) {
                        	
                            parent.getServer().dispatchCommand(this, command);
                        } else {
                            if(!command.equals(""))
                            {
                                outstream.write("You do not have permission to use this command...\r\n");
                                outstream.flush();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public boolean isAlive()
    {
        return run;
    }

    public void killClient()
    {
        try {
            run = false;
            outstream.write("Thank you using MCTelnet.\r\n");
            outstream.write("Quit");
            clientSocket.close();
        } catch (IOException ex) {
        }
    }

    private void shutdown() {
        try {
            run = false;
            Logger.getLogger("Minecraft").removeHandler(this);
            Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] Closing connection: " + ip);
            if(!clientSocket.isClosed())
            {
                outstream.write("Thank you using MCTelnet.");
                clientSocket.close();
            }
            parent = null;
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
            	if(string.indexOf("[MCTelnet]") < 0){
            		
            		string = ChatColor.stripColor(string);
            		outstream.write(string + "\r\n");
            		outstream.flush();
                }
            } catch (IOException ex) {
            }
        }
    }

    public boolean isOp() {
        if(authUser.equalsIgnoreCase("console"))
            return true;
        if(parent.getConfig().getBoolean("allowOPsAll",false))
            return parent.getServer().getPlayer(authUser).isOp();
        return false;
    }

    public boolean isPlayer() {
        return false;
    }

    public Server getServer() {
        return parent.getServer();
    }

    @Override
    public void close() throws SecurityException {
        shutdown();
    }

    private void sendTelnetCommand(int command, int option)
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
    }

    public String getName() {
        return authUser;
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
        return;
    }

    public void recalculatePermissions() {
        return;
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    public void setOp(boolean bln) {
        return;
    }

	public void sendMessage(String[] string) {		
		
		if(clientSocket.isConnected()){
            try {
            	
            	for(int i = 0;i < string.length;i++){
            		
            		if(string[i].indexOf("[MCTelnet]") < 0){
            			
            			string[i] = ChatColor.stripColor(string[i]);
            			outstream.write(string[i] + "\r\n");
            		}
            		
            	}
            	
            	outstream.flush();
            	
            } catch (IOException ex) {
            }
        }
		
	}
}
