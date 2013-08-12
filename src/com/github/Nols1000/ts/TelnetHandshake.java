package com.github.Nols1000.ts;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.github.Nols1000.ts.lg.LanguageTemplate;

public class TelnetHandshake {
	
	private TelnetOutput output;
	private TelnetInput input;
	private TelnetUser user;
	private TelnetThread parent;
	
	private LanguageTemplate lang;
	
	private int trys = 0;
	
	private String[] handshakeMsg;
	
	public TelnetHandshake(TelnetOutput out, TelnetInput in, TelnetUser user, TelnetThread telnetThread, LanguageTemplate lang) {
		
		output = out;
		input = in;
		this.user = user;
		parent = telnetThread;
		this.lang = lang;
		
		handshakeMsg = lang.welcomeMsg;
	}
	
	public void sendHandshake() throws IOException{
		
		output.sendMessage(handshakeMsg);
	}
	
	@SuppressWarnings("static-access")
	public boolean login() throws IOException, NoSuchAlgorithmException, ClassNotFoundException, SQLException{
		
		if(trys < 6){
			
			output.sendMessage(new String[]{"====== Login ====== \r\n"," \r\n","Username:"});
			String username = input.readln();
			
			output.sendMessage("Password:");
			
			output.sendTelnetCommand(new int[]{254, 1});
			output.sendTelnetCommand(new int[]{251, 1});
			
			String password = input.readln();
			
			output.sendMessage(new String[]{"\n\r","\n\r"});
			
			output.sendTelnetCommand(new int[]{253, 1});
			output.sendTelnetCommand(new int[]{252, 1});
			
			output.sendMessage(new String[]{"=== "+lang.getOnLoginProgressing()+"  ===", "\r\n"," \r\n"});
			
			if(user.login(username, password)){
				
				output.sendMessage(new String[]{"=== "+lang.getOnLoginAccepted()+" ===", "\r\n","=== "+lang.getOnAccessGranted()+" ===", "\r\n", "\n\r"});
				System.out.println("["+lang.getServerName()+"] "+username+lang.getOnLoggedIn());
				
				return true;
			}else{
				
				output.sendMessage(new String[]{"=== "+lang.getOnLoginFailed()+" ===", "\r\n", "=== "+lang.getOnAccessDenied()+" ===", "\r\n", "Please try again. You have "+ (5-trys) +" trys.", "\r\n", " \r\n"});
				
				trys++;
				
				login();
			}
		}else{
			
			parent.block(user.getSocket());
			return false;
		}		
		
		return false;
	}
}
