package com.github.Nols1000.ts.lg;

public class LanguageTemplate {

	// root
	public String lang = "en_EN";
	private String serverName = "TelnetServer";

	// event
	public String onLoggedIn = " logged in.";
	public String[] onBlocked = new String[]{" is Banned from the TelnetServer.","You are banned from this server."};
	public String onQuit = " quit.";
	
	// String
	public String[] welcomeMsg = new String[]{"Welcome,", "\n\r"};
	public String[] quitMsg = new String[]{"<Quit>","Have a nice day."};

	public String onLoginAccepted = "Login Accepted";
	public String onAccessGranted = "Access Granted";
	public String onLoginFailed = "Login Failed";
	public String onAccessDenied = "Access Denied";
	public String onLoginProgressing = "Login progressing. Please be patient.";

	
	
	public void setLang(String arg0){
		
		lang = arg0;
	}
	
	public void setOnLoggedIn(String arg0){
		
		onLoggedIn = arg0;
	}
	
	public void setOnBlocked(String[] arg0){
		
		onBlocked = arg0;
	}
	
	public void setOnQuit(String arg0){
		
		onQuit = arg0;
	}
	
	public void setWelcomeMsg(String[] arg0){
		
		welcomeMsg = arg0;
	}
	
	public void setQuitMsg(String[] arg0){
		
		quitMsg = arg0;
	}
	
	public void setOnLoginAccepted(String arg0){
		
		onLoginAccepted = arg0;
	}
	
	public void setOnAccessGranted(String arg0){
		
		onAccessGranted = arg0;
	}
	
	public void setOnLoginFailed(String arg0){
		
		onLoginFailed = arg0;
	}
	
	public void setOnAccessDenied(String arg0){
		
		onAccessDenied = arg0;
	}

	public void setOnLoginProgressing(String arg0){
		
		onLoginProgressing = arg0;
	}
	
	public void setServerName(String arg0){
		
		serverName = arg0;
	}
	
	public String getLang(){
		
		return lang;
	}
	
	public String getOnLoggedIn(){
		
		return onLoggedIn;
	}
	
	public String[] getOnBlocked(){
		
		return onBlocked;
	}
	
	public String getOnQuit(){
		
		return onQuit;
	}
	
	public String[] getWelcomeMsg(){
		
		return welcomeMsg;
	}
	
	public String[] getQuitMsg(){
		
		return quitMsg;
	}
	
	public String getOnLoginAccepted(){
		
		return onLoginAccepted;
	}
	
	public String getOnAccessGranted(){
		
		return onAccessGranted;
	}
	
	public String getOnLoginFailed(){
		
		return onLoginFailed;
	}
	
	public String getOnAccessDenied(){
		
		return onAccessDenied;
	}

	public String getOnLoginProgressing(){
		
		return onLoginProgressing;
	}

	public String getServerName() {
		
		return serverName ;
	}
}
