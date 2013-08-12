package com.github.Nols1000.ts;

import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TelnetUser {

	private Socket socket;
    
	private static String name;
    private boolean isLoggedIn = false;
    
    public TelnetUser(Socket s){
        
        socket = s;
    }
    
    public static boolean login(String userName, String password) throws NoSuchAlgorithmException, ClassNotFoundException, SQLException{
        
        name = userName;
        
        if(nameExists(name)){
            
            if(getPassword(name).equalsIgnoreCase(sha1(password))){
                
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean register(String name, String pass) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException{
        
        if(!nameExists(name)){
            
            Class.forName("org.sqlite.JDBC");
        
            Connection conn = DriverManager.getConnection("jdbc:sqlite:user.db");
            PreparedStatement st = conn.prepareStatement("INSERT INTO user (ID, name, pass) VALUES ( NULL, ? , ? )");
            st.setString(1, name);
            st.setString(2, sha1(pass));
            st.execute();
            
            conn.close();
            
            return true;
        }else{
            
            return false;
        }       
    }
    
    public static void update(String name, String pass) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException{
        
        Class.forName("org.sqlite.JDBC");
        
        Connection conn = DriverManager.getConnection("jdbc:sqlite:user.db");
        PreparedStatement st = conn.prepareStatement("UPDATE user SET pass=? WHERE name=?");
        st.setString(1, sha1(pass));
        st.setString(2, name);
        st.execute();
            
        conn.close();
    }
    
    public static void remove(String name) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException{
            
        Class.forName("org.sqlite.JDBC");
        
        Connection conn = DriverManager.getConnection("jdbc:sqlite:user.db");
        PreparedStatement st = conn.prepareStatement("DELETE FROM user WHERE name=?");
        st.setString(1, name);
        st.execute();
            
        conn.close();
    }
    
    private static boolean nameExists(String name) throws SQLException, ClassNotFoundException{
        
        Class.forName("org.sqlite.JDBC");
        
        Connection conn = DriverManager.getConnection("jdbc:sqlite:user.db");
        Statement st = conn.createStatement();
        
        ResultSet rs = st.executeQuery("SELECT * FROM user;");
        
        while(rs.next()){
            
            if(rs.getString("name").equalsIgnoreCase(name)){
                
                conn.close();
                
                return true;
            }
        }
        
        conn.close();
        
        return false;
    }
    
    private static String getPassword(String name) throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        
        Connection conn = DriverManager.getConnection("jdbc:sqlite:user.db");
        Statement st = conn.createStatement();
        
        ResultSet rs = st.executeQuery("SELECT * FROM user;");
        
        while(rs.next()){
            
            if(rs.getString("name").equalsIgnoreCase(name)){
                
                String pass = rs.getString("pass");
                
                conn.close();
                
                return pass;
            }
        }

        conn.close();
        
        return null;
    }

    public void logout() throws IOException{
        
        isLoggedIn = false;
        socket.close();
    }
     
    public String getName(){
        
        return name;
    }
    
    public boolean isLoggedIn(){
        
        return isLoggedIn;
    }

    public Socket getSocket() {
        
        return socket;
    }
    
    static String sha1(String input) throws NoSuchAlgorithmException {
        
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < result.length; i++) {
                
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
             
        return sb.toString();
    }
}