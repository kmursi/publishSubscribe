package com.aos.pubsub.services.components;


import java.io.BufferedReader;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.SubscribtionModel;
import com.aos.pubsub.services.model.TopicModel;

//PeerServer
class Listener extends Thread{
    int port;
    String message;
    ServerSocket server;
    Socket connection;
    BufferedReader br = null;
    ObjectMapper mapper = new ObjectMapper();
    /*********************************************************************************************/
    public Listener(int port) {
        this.port = port;
        System.out.println("Listening...");
    }
    public Listener(Socket s , int port) {
        connection=s;
        this.port = port;
    }
    /*********************************************************************************************/
    public synchronized void run() {
        try {
                String peerIP = connection.getInetAddress().getHostName();
                System.out.println("** Peer " + peerIP+" connected..\n");
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream()); //initiate writer
                out.flush();
                out.writeObject(1);
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream()); //initiate reader
                message = (String)in.readObject();
                /////////////////////////////////////////////////////////////////////////////
                
                while(true){
                	if(connection.getInputStream().available() != -1)
                	{
                	in = new ObjectInputStream(connection.getInputStream()); //initiate object input stream to read from peer
                	MessageMarker messageMarker;
                	String recievedString = null;
                
                try{
                	recievedString = (String) in.readObject();               //read
                	messageMarker = mapper.readValue(recievedString, TopicModel.class);
               }catch(JsonMappingException  | JsonParseException jEx){
            	   messageMarker =  mapper.readValue(recievedString, Message.class);
               }
                TopicModel topic = null;
                Message messageModel = null;
                
                if(messageMarker instanceof Message){
                	messageModel = (Message)messageMarker;
                	String topicNameStr = messageModel.getTopicName();
                	//messageList  = indexBus.get(topicNameStr);
                	//Message m = new Message(messageList.size(), messageModel.getData(),topicNameStr );
                	
                	System.out.println("Received new news  "+messageModel.getData() + " in topic "+topicNameStr );
                }else{
                	System.out.println("Invalid object passed . returning....");
                	
                }
                
                /////////////////////////////////////////////////////////////////////////////
                      //split the incoming message to adapt the local format

                 		// store peer ID
                
                }
        }
        }
        catch(Exception e)
        {
        	
        }
    /*********************************************************************************************/
        }
    }
