package dev.bukkit.Nols1000.MineWire;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.bukkit.ChatColor;

import com.github.Nols1000.ts.TelnetServer;

public class LogHandler extends Handler {

	private TelnetServer tServer;
	
	private boolean isClosed = false;
	
	public LogHandler(TelnetServer server){
		
		tServer = server;
	}
	
	@Override
	public void close() throws SecurityException {
		
		isClosed = true;
	}
	
	@Override
	public void flush(){
		
		if(!isClosed){
			
			try {
			
				tServer.sendMsg("\n\r");
			} catch (IOException e) {
			
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void publish(LogRecord arg0) {
		
		if(!isClosed){
		
			try {
			
				tServer.sendMsg("["+arg0.getLevel().toString()+"]"+ChatColor.stripColor(arg0.getMessage()));
				tServer.sendMsg("\n\r");
			} catch (IOException e) {
			
				e.printStackTrace();
			}	
		}
	}	
}
