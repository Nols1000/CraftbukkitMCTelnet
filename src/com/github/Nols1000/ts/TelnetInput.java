package com.github.Nols1000.ts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TelnetInput {

	private Socket clientSocket;
	
	private BufferedReader in;
	
	public TelnetInput(Socket s) throws IOException{
		
		clientSocket = s;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}
	
	public String readln() throws IOException{
		
		String ln = in.readLine();
		
		return stripTelnetCmd(ln);
	}
	
	public String readPass(){
		
		return null;
	}
	
	public BufferedReader getReader(){
		
		return in;
	}
	
	public String stripTelnetCmd(String ln){
		
		byte[] cmd = ln.getBytes();
		byte[] temp = new byte[cmd.length];
		
		boolean IAC = false;
		boolean DO = false;
		boolean DONT = false;
		boolean WILL = false;
		boolean WONT = false;
		
		int j = 0;
		
		for(int i = 0;i < cmd.length;i++){
			
			if(IAC){
				
				if(DO){
					
					IAC = false;
					DO = false;
				}
				if(DONT){
					
					IAC = false;
					DONT = false;
				}
				if(WILL){
					
					IAC = false;
					WILL = false;
				}
				if(WONT){
					
					IAC = false;
					WONT = false;
				}
				
				if(cmd[i] == (byte) 253){
					
					DO = true;
				}
				if(cmd[i] == (byte) 254){
					
					DO = true;
				}
				if(cmd[i] == (byte) 251){
					
					DO = true;
				}
				if(cmd[i] == (byte) 252){
					
					DO = true;
				}
			}else if(cmd[i] == (byte) 255){
				
				IAC = true;
			}else{
				
				temp[j] = cmd[i];
				j++;
			}
		}
		
		byte[] out = new byte[j];
		
		for(int i = 0; i < j;i++){
			
			out[i] = temp[i];
		}
		
		
		return new String(out);
	}
}
