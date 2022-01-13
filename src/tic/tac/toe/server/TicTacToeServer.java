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
    public static
    
     
    Connection con ;
    ResultSet rs;
    PreparedStatement pst;
    DataInputStream dis;
    DataOutputStream dos;
    Socket s,s1;
    public static DataHandle First_Player;
    public Room myroom;
    
    public DataHandle(Socket s){
            try {
                
                try {
                    DriverManager.registerDriver( new org.apache.derby.jdbc.ClientDriver());
                     con = DriverManager.getConnection("jdbc:derby://localhost:1527/IdDbs","Mohamed16","161996");}
                catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
                
                this.s = s;
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                start();
            }catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    public static enum requestTypes{
        register,login,getData,setData,setMove,player1,player2,createroom
    }
    
    @Override
    public void run(){
        while(true){
            try {
                 String msg = dis.readUTF();
                 String[] arrOfStrings = msg.split("\\+");
                 requestTypes Key = requestTypes.valueOf(arrOfStrings[0]);

                switch(Key){
                    //sign up
                    case register:
                            try {   
                                pst = con.prepareStatement("select * from Player where NAME= ?");
                                pst.setString(1,arrOfStrings[1]);
                                rs = pst.executeQuery();
                                 if (rs.next()){
                                    dos.writeUTF("Duplicated");//}
                                 }else{
                                    pst = con.prepareStatement("insert into Player(NAME,PASSWORD,EMAIL) values(?,?,?)");
                                    pst.setString(1,arrOfStrings[1]);
                                    pst.setString(2,arrOfStrings[2]);
                                    pst.setString(3,arrOfStrings[3]);
                                    pst.executeUpdate();
                                    dos.writeUTF("SignUp");}
                                }
                             catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); }
                                break;
                    //Login
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
                                }
                         }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); }
                             break;
                    //get table player data    
                    case getData:    
                        try {
                                pst = con.prepareStatement("select * from Player where NAME = ?");
                                pst.setString(1,arrOfStrings[1]);
                                rs = pst.executeQuery();
                                if (rs.next())
                                  dos.writeUTF(tableData());
                            }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); } 
                              break;

                    //set table player data  
                    case setData:
                           try {
                                 pst = con.prepareStatement("select * from Player where NAME = ?");
                                 pst.setString(1,arrOfStrings[1]);
                                rs = pst.executeQuery();
                         
                              if (rs.next()){
                                  int win=rs.getInt(6)+Integer.parseInt(arrOfStrings[2]);
                                  int lose=rs.getInt(7)+Integer.parseInt(arrOfStrings[3]);
                                  int tie= rs.getInt(8)+Integer.parseInt(arrOfStrings[4]);
                                  int GAMEPLAYED=win+lose+tie;
                                   pst = con.prepareStatement("UPDATE Player SET GAMEPLAYED=?,WIN=?,LOSE=?,TIE=? WHERE NAME =? ");
                                   pst.setString(5,arrOfStrings[1]);
                                   pst.setInt(2,win); 
                                   pst.setInt(3,lose);
                                   pst.setInt(4,tie);
                                   pst.setInt(1,GAMEPLAYED);
                                   pst.executeUpdate();}
                                 }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); } 
                                    break;
               
                    case createroom:
                         if(First_Player==null){
                              First_Player = this;
                         }
                         else{
                             myroom=new Room(First_Player,this);
                             First_Player.myroom=myroom;
                             First_Player=null;
                         }
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
    
  public class Room{
     public DataHandle player1;
     public DataHandle player2;
     
   
     
     Room(DataHandle player1,DataHandle player2){
         this.player1=player1;
         this.player2=player2;}
     
     public void sendToALl(String message) throws IOException{
        player1.dos.writeUTF(message);
        player2.dos.writeUTF(message);}
    
            
    }

        
    
   
     
   
   
 public String tableData() throws SQLException{
   
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
   
     NewSession player = new NewSession();
    
    ServerSocket serverSocket;
    Connection con;

public static void main(String[] args) throws IOException
        {
        new TicTacToeServer();
        }

        public TicTacToeServer() throws IOException
        {
            serverSocket = new ServerSocket(6060);
            System.out.println(new java.util.Date() + ":     Server started at socket 8000\n");
           
            int sessionNum = 1;
            
            while(true){
                
                Socket s = serverSocket.accept();
                 new DataHandle(s);
                player.firstPlayer=serverSocket.accept();
                 new DataHandle( player.firstPlayer);
                 player.secondPlayer=serverSocket.accept();
                 new DataHandle( player.secondPlayer);
                 
              
                

            
            }
        }
    
 
}
   
    

