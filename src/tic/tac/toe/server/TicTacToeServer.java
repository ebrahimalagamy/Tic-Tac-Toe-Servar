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
                start();
            
            
            }
            catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    public static enum requestTypes{
        register,login,getData,setData,setMove,player1,player2,createroom
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
               /////////////////     

                    case getData:
                        //System.out.println("server get data");
                        try {
                    pst = con.prepareStatement("select * from Player where NAME = ?");
                    pst.setString(1,arrOfStrings[1]);
                    rs = pst.executeQuery();
                    if (rs.next())
                       dos.writeUTF(tableData());
                 
                        }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); } 
                        break;
                   ///////////////////// 
                        case setData:
                           try {
                               //check if this part important
                                 pst = con.prepareStatement("select * from Player where NAME = ?");
                                 pst.setString(1,arrOfStrings[1]);
                                rs = pst.executeQuery();
                                ////////
                              if (rs.next()){
                                  int win=rs.getInt(6)+Integer.parseInt(arrOfStrings[2]);
                                  int lose=rs.getInt(7)+Integer.parseInt(arrOfStrings[3]);
                                 // int tie= rs.getInt(8)+Integer.parseInt(arrOfStrings[4]);
                                  int GAMEPLAYED=win+lose/*+tie*/;
                                  //add Tie
                                   pst = con.prepareStatement("UPDATE Player SET GAMEPLAYED=?,WIN=?,LOSE=? WHERE NAME =? ");
                                   pst.setString(4,arrOfStrings[1]);
                                   pst.setInt(2,win); 
                                   pst.setInt(3,lose);
                                // pst.setInt(4,tie);
                                   pst.setInt(1,GAMEPLAYED);
                                   pst.executeUpdate();
                             
                              }
                        
                        }catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); } 
                        break;
           //////////////////////
                     case createroom:
                         
                         dos.writeUTF("Roomclosed");
                         
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
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int PLAYER1_WON = 1;
    public static final int PLAYER2_WON = 2;
    public static final int DRAW = 3;
    public static final int CONTINUE = 4;
    
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
              /*
               System.out.println(new java.util.Date() + ":     Waiting for players to join session " + sessionNum + "\n");
                
                //connection to player1
                Socket firstPlayer = serverSocket.accept();
                System.out.println(new java.util.Date() + ":     Player 1 joined session " + sessionNum + ". Player 1's IP address " + firstPlayer.getInetAddress().getHostAddress() + "\n");
                //notify first player that he is first player
                new DataOutputStream(firstPlayer.getOutputStream()).writeInt(PLAYER1);

                //connection to player2
                Socket secondPlayer = serverSocket.accept();
                System.out.println(new java.util.Date() + ":     Player 2 joined session " + sessionNum + ". Player 2's IP address " + secondPlayer.getInetAddress().getHostAddress() + "\n");
                //notify second player that he is second player
                new DataOutputStream(secondPlayer.getOutputStream()).writeInt(PLAYER2);

                //starting the thread for two players
                System.out.println(new java.util.Date() + ":Starting a thread for session " + sessionNum++ + "...\n");
                NewSession task = new NewSession(firstPlayer, secondPlayer);
                Thread t1 = new Thread(task);
                t1.start();*/
                

            
            }
        }
    
 
}
   
    

