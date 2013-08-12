package com.github.Nols1000.ts.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDB {
	
	private String filename;
	private Connection conn;
	@SuppressWarnings("unused")
	private Statement st;
	
	@SuppressWarnings("unused")
	private boolean initialized = false;
	
	public SQLiteDB(String file){
		
		filename = file;
	}
	
	@SuppressWarnings("unused")
	private void init() throws ClassNotFoundException, SQLException{
		
		Class.forName("org.sqlite.JDBC");
		
		if(filename.equalsIgnoreCase(".sqlite") || filename.equalsIgnoreCase(".db")){
			
			conn = DriverManager.getConnection("sqlite:jdbc:"+filename);
			
			st = conn.createStatement();
			
			initialized = true;
		}else{
			
			System.err.println("File: '"+filename+"' is not a SQLiteDB. If you know it's a SQLiteDB please rename it to '*.sqlite' or '*.db'. '*' stands for a variable char.");
		}
		
	}
}
