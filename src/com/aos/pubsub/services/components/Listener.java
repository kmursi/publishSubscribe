package com.aos.pubsub.services.components;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;

import org.codehaus.jackson.map.ObjectMapper;


//PeerServer
class Listener extends Thread{
    int port;
    String message;
    ServerSocket server;
    Socket connection;
    BufferedReader br = null;
    ObjectMapper mapper = new ObjectMapper();
    String serverIP, topicName;
    int lastMessageIndex;
    ObjectOutputStream out;
    /*********************************************************************************************/
    public Listener(int port) {
        this.port = port;
        System.out.println("Listening...");
    }
    public Listener(String serverIP,String topicName , int lastMessageIndex) {
        this.serverIP=serverIP;
        this.topicName= topicName;
        this.lastMessageIndex=lastMessageIndex;
    }
    /*********************************************************************************************/
    public synchronized void run() {
    	MessageMarker messageMarker;
    	Message messageModel = null;
    	String message=topicName+"-"+lastMessageIndex;
        try{
            Socket socket = new Socket(serverIP, 60003);              //initiate socket withe the server through server searching port
            System.out.println("\nConnected to the server..\n");
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            
            out.flush();
            //System.out.println("\nhi\n");
            out.writeObject(message);                        //send
            out.flush();
            //System.out.println("\nhi\n");
            /////////////////////////////////////////////////////////////////////////////
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());//initiate reader
            String recievedString;
            while(socket.isConnected())
            {
	            	while(socket.getInputStream().available() != -1)
	            	{//store received message into message
			            /////////////////////////////////////////////////////////////////////////////
			            	recievedString = in.readObject().toString();
			            	
			            try{
			            	//recievedString = (String) in.readObject();               //read
			            	messageMarker = mapper.readValue(recievedString, TopicModel.class);
			           }catch(JsonMappingException  | JsonParseException jEx){
			        	   messageMarker =  mapper.readValue(recievedString, Message.class);
			           }
			            
			            if(messageMarker instanceof Message){
			            	messageModel = (Message)messageMarker;
			            	String topicNameStr = messageModel.getTopicName();
			            	System.out.println("Added new message  "+messageModel.getData() + " in topic "+topicNameStr );
			            }else{
			            	System.out.println("Invalid object passed . returning....");
			            }
			            
	            	}
	            	
            }
        
            /////////////////////////////////////////////////////////////////////////////
            in.close();                                            //close reader
            out.close();                                           //close writer
            socket.close();                                        //close connection
            System.out.println("\nConncetion has lost with the eventBus!\n");
            
            System.out.println("*********************************************************************************************");
            System.out.println("Type the action number as following:");
            System.out.println("1. Register a topic with eventbus.");
            System.out.println("2. Register a message in topic");
            System.out.println("3. Subscription request");
            System.out.println("4. To exit.");
            System.out.println("5. Pull request");
            System.out.println("*********************************************************************************************\n");
        }
        catch (EOFException exc)
    	{
        	System.out.println("Message received successfully ! ");
    	}
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
