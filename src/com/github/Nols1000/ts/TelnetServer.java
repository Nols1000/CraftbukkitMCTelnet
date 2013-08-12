package com.github.Nols1000.ts;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import com.github.Nols1000.ts.event.OnCommandEvent;
import com.github.Nols1000.ts.event.OnConnectHandler;
import com.github.Nols1000.ts.lg.LanguageTemplate;

public class TelnetServer extends Thread {
	
	/* 
	 * Nachrichten schicken
	 * Nachrichten erhalten
	 * Befehle senden
	 * Befehle ausführen
	 * Port erstellen
	 * Neuen Benutzer erstellen
	 * Benutzer bearbeiten
	 * Benutzer löschen
	 * 
	 * */

	
	
	private TCPListener listener;
	
    public TelnetServer() throws IOException{
    	
    	listener = new TCPListener(23, new LanguageTemplate());
    }
    
    public TelnetServer(int port) throws IOException{
    	
    	listener = new TCPListener(port, new LanguageTemplate());
    }
    
    public TelnetServer(int port, LanguageTemplate langTemplate) throws IOException{
    	
    	listener = new TCPListener(port, langTemplate);
    }
    
    @Override
    public void run(){
    	
    	try {
			
    		listener.start();
		} catch (IOException e) {
			
			e.printStackTrace();
		};
    }
    
    public void close() throws IOException{
    	
    	listener.stop();
    	interrupt();
    }
    
    public boolean registerUser(String name, String pass) throws NoSuchAlgorithmException, SQLException, ClassNotFoundException{
    	
     	return TelnetUser.register(name, pass);
    }
    
    public void updateUser(String name, String newpass) throws NoSuchAlgorithmException, ClassNotFoundException, SQLException{
    	   	 		
    	TelnetUser.update(name, newpass);
    }
    
    public void removeUser(String name) throws NoSuchAlgorithmException, ClassNotFoundException, SQLException{
    	
    	TelnetUser.remove(name);
    }
    
    public boolean loginUser(String name, String pass) throws NoSuchAlgorithmException, ClassNotFoundException, SQLException{
    	
    	return TelnetUser.login(name, pass);
    }
    
    public void sendMsg(String msg) throws IOException{
    	
    	Iterator<TelnetThread> temp = getThreads().iterator();
    	
    	while(temp.hasNext()){
    		
    	 	TelnetThread temp2 = temp.next();
    	 	
    	 	temp2.sendMsg(msg);
    	}
    }
    
    public void sendMsg(String[] msg) throws IOException{
    	
    	Iterator<TelnetThread> temp = getThreads().iterator();
    	
    	while(temp.hasNext()){
    		
    	 	TelnetThread temp2 = temp.next();
    	 	
    	 	temp2.sendMsg(msg);
    	}
    }
    
    public void sendMsg(String msg, String name) throws IOException{
    	
    	Iterator<TelnetThread> temp = getThreads().iterator();
    	
    	while(temp.hasNext()){
    		
    	 	TelnetThread temp2 = temp.next();
    	 	
    	 	if(temp2.getUser().getName().equalsIgnoreCase(name)){
    	 		
    	 		temp2.sendMsg(msg);
    	 	}
    	}
    }
    
    public void sendMsg(String[] msg, String name) throws IOException{
	
    	Iterator<TelnetThread> temp = getThreads().iterator();
    	
    	while(temp.hasNext()){
    		
    	 	TelnetThread temp2 = temp.next();
    	 	
    	 	if(temp2.getUser().getName().equalsIgnoreCase(name)){
    	 		
    	 		temp2.sendMsg(msg);
    	 	}
    	}
    }
    
    public void registerOnConnectHandler(OnConnectHandler handler){
    	
    	listener.setOnConnectHandler(handler);
    	
    	Iterator<TelnetThread> temp = getThreads().iterator();
    	
    	while(temp.hasNext()){
    		
    	 	TelnetThread temp2 = temp.next();
    	 	
    	 	OnCommandEvent event = handler.getEvent(temp2);
    	 	
    	 	temp2.registerOnCommandEvent(event);
    	}
    }
    
    public void setPort(int port) throws IOException{
    	
    	listener.setPort(port);
    }
    
    public List<TelnetThread> getThreads(){
    	
    	return listener.getThreads();
    }
}
