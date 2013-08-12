package com.github.Nols1000.ts;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.github.Nols1000.ts.event.OnCommandEvent;
import com.github.Nols1000.ts.lg.LanguageTemplate;

public class TelnetThread extends Thread {

    private Socket clientSocket;
    
    private TelnetInput in;
    private TelnetOutput out;
    
    private TelnetUser user;
    private TelnetHandshake handshake;
    
    private TCPListener listener;
    
    private boolean isLoggedIn = false;
    
    private LanguageTemplate lang;
    
    private OnCommandEvent onCmdEvent = null;
    
    public TelnetThread(Socket s, TCPListener listener, LanguageTemplate lg) throws IOException{
        
    	lang = lg;
    	
    	clientSocket = s;
        in = new TelnetInput(s);
        out = new TelnetOutput(s);
        this.listener = listener;
        
        System.out.println("["+lang.getServerName()+"] Thread created: "+ s.toString());
    }
    
    public void block(Socket socket) throws IOException{
        
        out.sendMessage(lang.getOnBlocked()[1]);
        
        System.out.println("["+lang.getServerName()+"] "+clientSocket.getLocalAddress()+lang.getOnBlocked()[0]); 
        socket.close();
    }
    
    public void registerOnCommandEvent(OnCommandEvent event){
    	
        onCmdEvent = event;
    }
    
    @Override
    public void run(){
        
        user = new TelnetUser(clientSocket);
        handshake = new TelnetHandshake(out, in, user, this, lang);
        
        try {
            
            handshake.sendHandshake();
            isLoggedIn = handshake.login();
            
            if(onCmdEvent != null){
        		
        		onCmdEvent.onLoggedIn();
        	}
            
            commandLoop();
        } catch (IOException e) {
            
        	e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            
        	e.printStackTrace();
        } catch (ClassNotFoundException e) {
            
        	e.printStackTrace();
        } catch (SQLException e) {
            
            e.printStackTrace();
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
    }
    
    public void commandLoop() throws IOException, InterruptedException{ 	
    	
        String cmd = in.readln();
        
        while(!cmd.equalsIgnoreCase("quit")){
            
            if(cmd.indexOf(" ") > 0){
                
                if(onCmdEvent != null){
                    
                    onCmdEvent.onCommand(cmd);
                }else{
                    
                    out.sendMessage("Can\'t handle command (ERROR: 404). \n\r");
                }
            }
            
            if(cmd.indexOf(" ") < 0){
                
                if(onCmdEvent != null){
				
				    onCmdEvent.onCommand(cmd);
                }else{
			    
                    out.sendMessage("Can\'t handle command (ERROR: 404). \n\r");
                }
            }
            
            cmd = in.readln();
        }
        
        System.out.println("["+lang.getServerName()+"] "+user.getName()+lang.getOnQuit());
        
        close();
    }
    
    public void close() throws IOException, InterruptedException{
        
        out.sendMessage(lang.getQuitMsg());
        
        sleep(5000);
        
        listener.unregisterThread(this);
        
        clientSocket.close();
        this.interrupt();
    }
    
    public void sendMsg(String msg) throws IOException {
        
    	if(isLoggedIn){
    		
    		out.sendMessage(msg);
    	}
    }

    public void sendMsg(String[] msg) throws IOException {
        
    	if(isLoggedIn){
    		
    		out.sendMessage(msg);  
    	}    
    }
    
    public TelnetUser getUser(){
            
        return user;
    }
}
