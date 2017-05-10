/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static tcp.server.Serverr.ConnectedClients;
import static tcp.server.Serverr.display;
import static tcp.server.Serverr.port;


public class Serverr {
    static ServerSocket serverSocket;
    private static ServerThread RunThread;
    public static int port =1500;
    public static boolean keepGoing;
    public static int uniqueId;
    static ArrayList<Client> ConnectedClients;
      
    public Serverr(int port){
        Serverr.port=port;
        Serverr.ConnectedClients=new ArrayList<Client>();
    }
    
    public static void Set_Server(int port){
        try {
            Serverr.serverSocket=new ServerSocket(port);
            Serverr.port=port;
            Serverr.ConnectedClients=new ArrayList<Client>();
            RunThread=new ServerThread();
            RunThread.start();
        } catch (Exception e) {
        }
    }
    
    public static void start(){
        if (Serverr.serverSocket!=null) {
            return;
        }
        try {
            Serverr.serverSocket=new ServerSocket(port);
            Serverr.ConnectedClients=new ArrayList<Client>();
            Serverr.RunThread=new ServerThread();
            RunThread.start();
        } catch (Exception e) {
            Server.Txtarea.setText(Server.Txtarea.getText()+e+"\n");
        }
    }
    
    public static void stop(){
        if (Serverr.serverSocket.isClosed()) {
            return;
        }
        keepGoing=false;
        try {
            for (int i = ConnectedClients.size(); --i >= 0; ) {
                Client ct=ConnectedClients.get(i);
                ct.close();
                ConnectedClients.remove(i);
            }
            Serverr.RunThread.interrupt();
            Serverr.serverSocket.close();
            Serverr.serverSocket=null;
        } catch (Exception e) {
        }
    }
  
 // wait for each client to connect
 
    
    public static void display(String msg){
                Server.Txtarea.setText(Server.Txtarea.getText()+msg+"\n");
    }
    
    public static synchronized void broadcast(String message){
        for (int i = ConnectedClients.size(); --i >= 0; ) {
            Client ct=ConnectedClients.get(i);
            if (!ct.writeMsg(message)) {
                ConnectedClients.remove(i);
                display("Disconnected Client "+ct.username+" remove from list.");
            }
        }
    }
    public static synchronized void broadcast(Object message){
        for (int i = ConnectedClients.size(); --i >= 0; ) {
            Client ct=ConnectedClients.get(i);
            if (!ct.writeMsg(message)) {
                ConnectedClients.remove(i);
                display("Disconnected Client "+ct.username+" remove from list.");
            }
        }
    }
    
    public static synchronized void remove(int id){
        for (int i = 0; i < ConnectedClients.size(); ++i) {
            Client ct =ConnectedClients.get(i);
            if (ct.id==id) {
                ct.close();
                ConnectedClients.remove(i); 
                return;
            }
        }
    }
}
class ServerThread extends Thread{
    public void run(){
        try {
            while(!Serverr.serverSocket.isClosed()){
                display("Server waiting for Clients on port "+port+".");
                Socket socket=Serverr.serverSocket.accept();
                Client newClient=new Client(socket);
                Serverr.ConnectedClients.add(newClient);
                newClient.start();
                newClient.writeMsg("baglandi.");
                
 
//                if(ConnectedClients.size()%2==1){
//                    
//                    Serverr.broadcast("wait oponent");
//                }
//                
            }
            try {
                    Serverr.serverSocket.close();
                    for (int i = 0; i < ConnectedClients.size(); ++i) {
                        Client tc=ConnectedClients.get(i);
                        tc.close();
                    }
                } catch (Exception e) {
                    display("Exception closing the server and clients: "+e);
                }
        } catch (Exception e) {
            String msg=new Date().toString()+"Exception on new ServerSocket: "+e+"\n";
            display(msg);
        }
    }
}