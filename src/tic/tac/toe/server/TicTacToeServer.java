/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tic.tac.toe.server;

import java.net.ServerSocket;
import java.sql.DriverManager;
import java.sql.*;
import org.apache.derby.jdbc.ClientDriver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class DataHandle extends Thread {
     
    Connection con ;
    ResultSet rs;
    PreparedStatement pst;
    DataInputStream dis;
    DataOutputStream dos;
    
    public DataHandle(Socket s){
            try {
                try {
                    DriverManager.registerDriver( new org.apache.derby.jdbc.ClientDriver());
                     con = DriverManager.getConnection("jdbc:derby://localhost:1527/IdDbs","Mohamed16","161996");}
                catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
                
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                start();}
            catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    @Override
    public void run(){
        while(true){
            try {
                String data = dis.readUTF();
                String[] arrOfStrings = data.split("\\+");
                switch(arrOfStrings.length){
                    case 3:
                    try {
                    pst = con.prepareStatement("insert into Player(NAME,PASSWORD,EMAIL) values(?,?,?)");
                    pst.setString(1,arrOfStrings[0]);
                    pst.setString(2,arrOfStrings[1]);
                    pst.setString(3,arrOfStrings[2]);
                    pst.executeUpdate();
                    
                    } catch (SQLException ex) {
                        Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                    //not send
                    case 2:
                    try {
                    pst = con.prepareStatement("select * from Player where NAME = ?");
                    pst.setString(1,arrOfStrings[0]);
                    rs = pst.executeQuery();
                    while (rs.next()){
                    if((rs.getString(3)).equals(arrOfStrings[1]))
                        dos.writeUTF("validsignin");
                    else
                        dos.writeUTF("invalidsignin");                    
                    }}
                    catch (SQLException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex); }
                    break;
                }
            } catch (IOException ex) {Logger.getLogger(DataHandle.class.getName()).log(Level.SEVERE, null, ex);}
        }
           
    }
}
    




public class TicTacToeServer {
   
    
ServerSocket serverSocket;
Connection con;

        public TicTacToeServer() throws IOException
        {
                try {
                    DriverManager.registerDriver(new ClientDriver());
                    con=DriverManager.getConnection("jdbc:derby://localhost:1527/IdDbs","Mohamed16","161996");}
                catch (SQLException ex) { Logger.getLogger(TicTacToeServer.class.getName()).log(Level.SEVERE, null, ex);}
                
                
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
   
    

