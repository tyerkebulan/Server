/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.server;

import java.awt.Event;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;


public class Client implements Runnable{
    public Socket socket;
    public ObjectInputStream sInput;
    public ObjectOutputStream sOutput;
    public int id;
    public String username;
    public Date ConDate;
    public ClientThread ListenThread;
    
    Client(Socket socket){
        this.id=++Serverr.uniqueId;
        this.socket=socket;
        try {
            this.sOutput=new ObjectOutputStream(socket.getOutputStream());
            this.sInput=new ObjectInputStream(socket.getInputStream());
            this.username=(String) sInput.readObject();
            this.ConDate=new Date();
            this.ListenThread=new ClientThread(this);
        } catch (Exception e) {
            Serverr.display("Exception creating new Input/output Streams: "+e);
        }
    }
    
    public void start(){
       
        this.ListenThread=new ClientThread(this);
        this.ListenThread.start();
    }
    //kapatmak icin
    public void close(){
        try {
            if (this.ListenThread!=null) {
                this.ListenThread.interrupt();
            }
            if (this.sOutput!=null) {
                this.sOutput.close();
            }
            if (this.sInput!=null) {
                this.sInput.close();
            }
        } catch (Exception e) {
        }
    }
    
//cliente mesaj gondermek icin  
    public boolean writeMsg(Object msg){
        if (!this.socket.isConnected()) {
            close();
            return false;
        }
        
        try {
            
            this.sOutput.writeObject(msg);
        } catch (Exception e) {
            Serverr.display("Error sending message to "+username);
            Serverr.display(e.toString());
        }
        return true;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public class ClientThread extends Thread{
        Client TheClient;
        
        ClientThread(Client TheClient){
            this.TheClient=TheClient;
        }
        public void run(){
            while (TheClient.socket.isConnected()) {                
                try {
                    Client otherClient = Serverr.ConnectedClients.get(0);
                    String message=(String) this.TheClient.sInput.readObject();
                    //eger client bir tane olurssa bekle diyen mesaj gonderecez
                    if(Serverr.ConnectedClients.size()%2==1){
                        TheClient.writeMsg("wait oponent");
                    }
                    else{
                        //hangi klientin turni olduguni bilmek icin
                    if((!TheClient.equals(otherClient)) && (!message.equals("you lose")) && (!message.equals("you win"))){
                        TheClient.writeMsg("oponent turn");
                        otherClient.writeMsg("your turn");
                        
                    }
                    else if((TheClient.equals(otherClient) && (!message.equals("you lose")) && (!message.equals("you win")))){
                        otherClient = Serverr.ConnectedClients.get(1);
                        otherClient.writeMsg("your turn");
                        TheClient.writeMsg("oponent turn");
                        
                    }
                    if(message.equals("you lose")){
                        if(!TheClient.equals(otherClient)){
                            TheClient.writeMsg("you lose");
                            otherClient.writeMsg("you win");
                            
                        }
                        else{
                            otherClient = Serverr.ConnectedClients.get(1);
                            TheClient.writeMsg("you lose");
                            otherClient.writeMsg("you win");
                            
                        }
                        
                        //Serverr.broadcast("you win");
                       
                    }else{
                        
                        Serverr.broadcast(message);
//                         sleep(1000);
//                        TheClient.writeMsg("your oponent turn");
                    }
                  
                        
                    Server.Txtarea.setText(Server.Txtarea.getText()+message+"\n");
                    }
                } catch (Exception e) {
                    Serverr.display(this.TheClient.username+" Exception reading Streams: "+e);
                    break;
                }
            }
            Serverr.remove(this.TheClient.id);
        }
    }
    
}
