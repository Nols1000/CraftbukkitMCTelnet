package com.github.Nols1000.ts;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.github.Nols1000.ts.event.OnCommandEvent;
import com.github.Nols1000.ts.event.OnConnectHandler;
import com.github.Nols1000.ts.lg.LanguageTemplate;

public class TCPListener{
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private LanguageTemplate lang;
	
	private OnConnectHandler onConHandler = null;
	private OnCommandEvent event = null;
	
	private List<TelnetThread> threads = new ArrayList<TelnetThread>();
	
	private int port;
	private boolean run = false;
	
	
	public TCPListener(int port, LanguageTemplate lang) throws IOException{
			
	    this.port = port;		
		this.lang = lang;
	}
	
	public void start() throws IOException{
		
		serverSocket = new ServerSocket(port);
		
		run = true;
		listen();
	}
	
	public void stop(){
		
		if(run){
			
			try {
				
				serverSocket.close();
			} catch (IOException e) {
				
			}
		}
		
		run = false;
	}
	
	public void listen() throws IOException{
		
		if(isRunning()){
		
			socket = serverSocket.accept();
			
			TelnetThread thread = new TelnetThread(socket, this, lang);
			thread.start();
		
			if(onConHandler != null){
				
				event = onConHandler.getEvent(thread);
				
				thread.registerOnCommandEvent(event);
			}
		
			threads.add(thread);		
		
			listen();
		}		
	}
	
	private boolean isRunning() {
		
		return run ;
	}

	public void unregisterThread(TelnetThread thread){
		
		threads.remove(thread);
	}
	
	public void setPort(int port) throws IOException{
		
	    serverSocket.close();
	    serverSocket = new ServerSocket(port);
	    
		this.port = port;
	}
	
	public int getPort(){
		
		return port;
	}
	
	public Socket getSocket(){
		
		return socket;
	}
	
	public List<TelnetThread> getThreads(){
		
		return threads;
	}
	
	public void setOnConnectHandler(OnConnectHandler handler){
		
		onConHandler = handler;
	}
	
}
