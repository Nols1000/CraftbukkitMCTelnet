package com.github.Nols1000.ts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TelnetOutput {

	private Socket socket;
	
	private BufferedWriter out;
	
	public TelnetOutput(Socket s) throws IOException{
		
		socket = s;
		
		if(!s.isClosed()){
			
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
	}
	
	public void sendMessage(String msg) throws IOException{
		
		if(!socket.isClosed()){
			
			out.write(msg);
			out.flush();
		}	
	}
	
	public void sendMessage(String[] msg) throws IOException{
		
		if(!socket.isClosed()){
			
			for(int i = 0; i < msg.length; i++){
			
				out.write(msg[i]);
				out.flush();
			}
		}
	}
	
	public void sendTelnetCommand(int cmd) throws IOException{
		
		if(!socket.isClosed()){
			
			out.write(255);
			out.write(cmd);
			out.flush();
		}
	}
	
	public void sendTelnetCommand(int[] cmd) throws IOException{
		
		if(!socket.isClosed()){
			
			if(cmd.length > 2){
			
				out.write("There are to many TelnetArguments. Please report this to the server admin or Nols1000.");
				out.flush();
			}
		
			out.write(255);
			out.write(cmd[0]);
			out.write(cmd[1]);
			out.flush();
		}
	}
	
	public BufferedWriter getWriter(){
		
		return out;
	}
}