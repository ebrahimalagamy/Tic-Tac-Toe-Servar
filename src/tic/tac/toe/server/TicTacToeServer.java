package tic.tac.toe.server;

import java.net.ServerSocket;
import java.sql.DriverManager;
import java.sql.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

class DataHandle extends Thread {
    
    public static final String separator = ",";
    public static final String iWantToPlay = "iWantToPlay";
    public static final String  letsPlay= "letsPlay";
    public static final String  yourSymbole= "yourSymbole";
    public static final String  move= "move";
    public static final String X="x";
    public static final String O ="o";
    public static DataHandle availToPlay;
    static Vector<DataHandle> clientsVector = new Vector<DataHandle>();
    public String name = "no name";
    
    Connection con ;
    ResultSet rs;
    PreparedStatement pst;
    DataInputStream dis;
    DataOutputStream dos;
    
    DataHandle otherPlayer;
    
    
    public DataHandle(Socket s){
            try {
                
                try {
                    DriverManager.registerDriver( new org.apache.derby.jdbc.ClientDriver());
                     con = DriverManager.getConnection("jdbc:derby://localhost:1527/IdDbs","Mohamed16","161996");}
                catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
                
                //this.s = s;
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                clientsVector.add(this);
                start();
            
            
            }
            catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    public void sendToBothPlayer(String message) throws IOException{
        dos.writeUTF(message);
        otherPlayer.dos.writeUTF(message);
    }
    
    public static enum requestTypes{
        register,login,getData,setData,setMove,player1,player2,createroom,iWantToPlay
    }
    
    @Override
    public void run(){
        while(true){
            try {
                String data = dis.readUTF();
           
                String[] arrOfStrings = data.split("\\+");
                
                   
                    String[] request = data.split(separator);
                    
                    System.out.println(data);
                    System.out.println(request.length!=0);
                    System.out.println(request[0]);
                   
                        if(request[0].equals(iWantToPlay)){
                            //request[1] userName
                            name = request[1];
                            if(availToPlay == null){
                                availToPlay = this;
                                System.out.println(iWantToPlay+" 1");
                            }else{
                                otherPlayer = availToPlay;
                                availToPlay.otherPlayer = this;
                                availToPlay = null;
                                dos.writeUTF(yourSymbole+separator+X+separator+otherPlayer.name);
                                otherPlayer.dos.writeUTF(yourSymbole+separator+O+separator+name);

                                sendToBothPlayer(letsPlay);
                            }
                        }else if(request[0].equals(move)){
                            // request[1] == position
                            // request[2] == symbole

                            String nextTurn;
                            if(request[2].equals(X))
                                nextTurn =O;
                            else
                                nextTurn =X;
                            System.out.println(data+separator+nextTurn);
                            sendToBothPlayer(data+separator+nextTurn);
                        }else{
                            
                requestTypes Key = requestTypes.valueOf(arrOfStrings[0]);

                switch(Key){
                    case register:
                    try {
                        //email check not work
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
                   //////////////////
                    case login:
                    try {
                    pst = con.prepareStatement("select * from Player where NAME = ?");
                    pst.setString(1,arrOfStrings[1]);
                    rs = pst.executeQuery();
                    while (rs.next()){
                    if((rs.getString(3)).equals(arrOfStrings[2]))
                    {
                        name = rs.getString("NAME");
                        dos.writeUTF("validsignin");
                    }
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

    
                        case setData:
                           try {

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
                }
                
            }

                
            }catch(SocketException ex){
                try {
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
    ServerSocket serverSocket;

        public TicTacToeServer() throws IOException
        {
            serverSocket = new ServerSocket(6060);
            while(true){
                Socket s = serverSocket.accept();
                 new DataHandle(s);

            }
        }
        
        public static void main(String[] args) throws IOException{
                 new TicTacToeServer();
        }
    
 
}
   
    

