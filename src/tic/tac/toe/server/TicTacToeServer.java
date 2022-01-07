package tic.tac.toe.server;

import java.net.ServerSocket;
import java.sql.DriverManager;
import java.sql.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

class DataHandle extends Thread {
     
    Connection con ;
    ResultSet rs;
    PreparedStatement pst;
    DataInputStream dis;
    DataOutputStream dos;
    Socket s;
    
    public DataHandle(Socket s){
            try {
                
                try {
                    DriverManager.registerDriver( new org.apache.derby.jdbc.ClientDriver());
                     con = DriverManager.getConnection("jdbc:derby://localhost:1527/IdDbs","Mohamed16","161996");}
                catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
                
                this.s = s;
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                start();}
            catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    public static enum requestTypes{
        register,login,getData,setData,setMove
    }
    
    @Override
    public void run(){
        while(true){
            try {
                String data = dis.readUTF();
           
                String[] arrOfStrings = data.split("\\+");
                requestTypes Key = requestTypes.valueOf(arrOfStrings[0]);

                switch(Key){
                    case register:
                    try {
                        //email check not work
                    pst = con.prepareStatement("select * from Player where NAME= ?");
                    pst.setString(1,arrOfStrings[1]);
                    rs = pst.executeQuery();
                     if (rs.next()){
                      // if((rs.getString(2)).equals(arrOfStrings[1])||(rs.getString(4)).equals(arrOfStrings[3])){
                                 dos.writeUTF("Duplicated");//}
                     }
                        
                        else{
                        pst = con.prepareStatement("insert into Player(NAME,PASSWORD,EMAIL) values(?,?,?)");
                        pst.setString(1,arrOfStrings[1]);
                        pst.setString(2,arrOfStrings[2]);
                        pst.setString(3,arrOfStrings[3]);
                        pst.executeUpdate();
                        dos.writeUTF("SignUp");}
                    }
                    catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); }
                    break;

                    case login:
                    try {
                    pst = con.prepareStatement("select * from Player where NAME = ?");
                    pst.setString(1,arrOfStrings[1]);
                    rs = pst.executeQuery();
                    while (rs.next()){
                    if((rs.getString(3)).equals(arrOfStrings[2]))
                        dos.writeUTF("validsignin");
                    else
                        dos.writeUTF("invalidsignin");                    
                    }}
                    catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); }
                    break;
                    
                    
                    
                    case getData:
                        //System.out.println("server get data");
                        try {
                    pst = con.prepareStatement("select * from Player where NAME = ?");
                    pst.setString(1,arrOfStrings[1]);
                    rs = pst.executeQuery();
                    if (rs.next())
                       dos.writeUTF(getData());
                       
                    else
                        dos.writeUTF("NoSuschPlayer");
                    
                        }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); } 
                        break;
                }
                
                
            }catch(SocketException ex){
                try {
                    s.close();
                    dis.close();
                    dos.close();
                    con.close();
                    
                }catch (IOException | SQLException ex1) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex1);}
                finally{
                   System.out.println("user Closed");
                   break; }
                
            }
            
           catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
        }
           
    }
     public static String getData(String name){
    return (requestTypes.getData.name()+"+"+name);
    }
    
 public String getData() throws SQLException{
   
       return("PlayerData"+"+"
               + rs.getInt(1)+"+"
               + rs.getString(2)+"+"
               + rs.getString(3)+"+"
               + rs.getString(4)+"+"
               + rs.getInt(5)+"+" 
               + rs.getInt(6)+"+"
               + rs.getInt(7)+"+"
               + rs.getInt(8));}
 
}
 





public class TicTacToeServer {  
  
    
ServerSocket serverSocket;
Connection con;

        public TicTacToeServer() throws IOException
        {
            serverSocket = new ServerSocket(6060);
            while(true){
                Socket s = serverSocket.accept();
                new DataHandle(s);}
        }
    
 public static void main(String[] args) throws IOException
        {
        new TicTacToeServer();
        }
}
   
    

