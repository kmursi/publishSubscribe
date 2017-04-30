package com.aos.pubsub.services.components;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;

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
    	for(int i=0;i<10;i++)
    	{
    	//System.out.println("=======================================================");
    	System.out.println("Attempt number "+i+" to connect to the EventBus..!");
    	MessageMarker messageMarker;
    	Message messageModel = null;
    	String message=topicName+"-"+lastMessageIndex;
        try{
            Socket socket = new Socket(serverIP, 60003);              //initiate socket withe the server through server searching port
            if(socket.isConnected())
            {
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            out.flush();
            out.writeObject(message);                        //send
            out.flush();
            /////////////////////////////////////////////////////////////////////////////
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());//initiate reader
            String recievedString;
            while(socket.isConnected())
            {
            		long msgRecievingStartTime = new Date().getTime();
            		int msgCount = 0 ;
	            	while(socket.getInputStream().available() != -1)
	            	{
			            /////////////////////////////////////////////////////////////////////////////
			            	recievedString = in.readObject().toString();
			            try{
			            	messageMarker = mapper.readValue(recievedString, TopicModel.class);
			           }catch(JsonMappingException  | JsonParseException jEx){
			        	   messageMarker =  mapper.readValue(recievedString, Message.class);
			           }
			            
			            if(messageMarker instanceof Message){
			            	messageModel = (Message)messageMarker;
			            	String topicNameStr = messageModel.getTopicName();
			            	System.out.println("Received new message  "+messageModel.getData() + " from topic "+topicNameStr );
			            }else{
			            	System.out.println("Invalid object passed . returning....");
			            }
			            msgCount++;
	            	}
	            	long msgRecievingEndTime = new Date().getTime();
	            	System.out.println("Received "+msgCount+" messages  in "+ (msgRecievingEndTime -msgRecievingStartTime) +" milliseconds." );          	
            }
            /////////////////////////////////////////////////////////////////////////////
            in.close();                                            //close reader
            out.close();                                           //close writer
            socket.close();                                        //close connection
            System.out.println("\nConncetion has lost with the eventBus!\n");
            
            }
        }
        catch (EOFException exc)
    	{
        	System.out.println("Messages received successfully ! ");
    	}
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
            
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        try {
			sleep(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    	System.out.println("=======================================================\n");
        System.out.println("=======================================================");
        System.out.println("Type the action number as following:");
        System.out.println("1. Register a topic on eventbus.");
        System.out.println("2. Publish  messages in a topic.");
        System.out.println("3. Subscribe a topic.");
        System.out.println("4. Messages pull request from a specific date.");
        System.out.println("5. To exit.");
        System.out.println("=======================================================\n");
    }
}
