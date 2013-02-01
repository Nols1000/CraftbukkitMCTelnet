package com.github.Nols1000.MCTelnet.Telnet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4 {

	private int[] block = new int[4];
	
	public IPv4(String IP){
		
		
	}
	
	public void getByString(String IP){

	    String regex1="((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(?![\\d])";

	    Pattern p = Pattern.compile(regex1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(IP);
		
		if(m.find()){
			
			String[] tempIP = IP.split(".");
			
			if(tempIP.length == 4){
				
				for(int i = 0; i < 4; i++){
			
					block[i] = Integer.getInteger(tempIP[i]);
				}
			}	
		}
	}
	
	public String toString(){
		
		return block[0]+"."+block[1]+"."+block[2]+"."+block[3];
	}
	
}
