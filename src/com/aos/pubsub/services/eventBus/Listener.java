package com.aos.pubsub.services.eventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aos.pubsub.services.components.Main;
import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;
import com.aos.pubsub.services.model.SubscribtionModel;



/**
 * Created by kmursi on 3/10/17.
 */
public class Listener extends Thread {
    Socket conn;
    ObjectMapper mapper = new ObjectMapper();
    int listeningPort, publishTopicPort = 60000, publishMessagePort = 60001,SubscribtionRequest = 60002;    //each port hold a deffirent function
    static int maxsize = 0;
    /* create a hash map table that holds a concurrent hash map to assure synchronization
    *  each hash element contains a string ID (file name) and array of Messages*/
    static volatile Map<String, List<Message>> indexBus = new ConcurrentHashMap<String, List<Message>>();

    /*********************************************************************************************/

    public Listener(Socket s, int port) {
        conn = s;                                       // let the local socket to have the value of the received one
        this.listeningPort = port;                      // let the local port to have the value of the received one
    }

    /*********************************************************************************************/

    public synchronized void run() {
        if (listeningPort == publishTopicPort)           //call Register_a_File() if its port is connected with a peer
        	publishTopic();
            /////////////////////////////////////////////////////////////////////////////
        else if (listeningPort == publishMessagePort)        //call Register_a_File() if its port is connected with a peer
        publishMessage();
        else if (listeningPort == SubscribtionRequest)        //call Register_a_File() if its port is connected with a peer
        	Subscribe_Topic_Request();
    }

    /*********************************************************************************************/

    synchronized void publishTopic() {
        try {
            String topicName;                             // define an integer peer ID which is the  peer port
            List<Message> messageList;                    //String array used for splitting the received message
            /////////////////////////////////////////////////////////////////////////////
            String pubIP = conn.getInetAddress().getHostName();    //save the peer IP into peerIP
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream()); //initiate object input stream to read from peer
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
            
            if(messageMarker instanceof TopicModel){
            	topic = (TopicModel) messageMarker;
            	long createdDate = new Date().getTime();
                topic.setCreatedOn(createdDate);
                topic.setUpdatedOn(createdDate);
                messageList = topic.getMessageList(); 
                topicName = topic.getTopicName();   
                if(messageList == null){
                	messageList = new ArrayList<Message>();
                	topic.setMessageList(messageList);
                 }  
              /*  for(int index = 0 ; index < messageList.size() ; index++){
                	Message m = new Message(index, messageList.get(index).getData(),topicName);
                	topic.getMessageList().add(m);
            	 }*/
                	System.out.println("Topic " + topicName + "  created in the event bus \n");
                    /////////////////////////////////////////////////////////////////////////////
                    indexBus.put(topicName, messageList);             //store the hashmap element
               
                 
            }else{
            	System.out.println("Invalid object passed . returning....");
            	
            }
            
            /////////////////////////////////////////////////////////////////////////////
                  //split the incoming message to adapt the local format

             		// store peer ID
            
                in.close();                                         //close reader
                conn.close();                                       //close connection
            }
        /////////////////////////////////////////////////////////////////////////////
        catch(UnknownHostException unknownHost){                                           //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                                                    //To Handle Input-Output Exception
            ioException.printStackTrace();
        }
         catch (Exception e) {                                      //track general errors
            e.printStackTrace();
            System.out.println(e.toString());
        }

        finally {
            System.out.println("Type the action number as following:");
            System.out.println("1. To exit.\n");
            Thread.currentThread().stop();
        }
    }

    /*********************************************************************************************/

    synchronized void Subscribe_Topic_Request() {
        try{
            String topicName,reply=null;                             // define an integer peer ID which is the  peer port
            List<Message> messageList;
            int peerID;
            //String array used for splitting the received message
            /////////////////////////////////////////////////////////////////////////////
            String subIP = conn.getInetAddress().getHostName();    //save the peer IP into peerIP
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream()); //initiate object input stream to read from peer
           // MessageMarker messageMarker;
            String recievedString = null;
            recievedString=(String)in.readObject();
            if(!recievedString.equals(null))
            {
            	String messageArray [] = recievedString.split("-");
            	peerID = Integer.parseInt(messageArray[0]);
            	topicName = messageArray[1];
            	if(indexBus.containsKey(topicName))
            	{
            		System.out.println("Subscribtion request from "+subIP+":"+peerID+" accepted for topic "+topicName+"\n");
                	reply="You are subcribed to "+topicName;
                	Subscribtion_Recorder(subIP+"-"+recievedString);
            	}
            	else
                {
                	System.out.println("No message Received!\n");
                	reply = "Topic not found\n";  
                }
            }
            else
            {
            	System.out.println("No message Received!\n");
            	reply = "Topic not found\n";  
            }
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream()); //define object writer
            out.writeObject(reply);                                                  //write the reply to the peer
            out.flush();
            in.close();   
            out.close();
            conn.close();   
        }
        catch(Exception e)
        {
        	
        }  
    }
    
    public void Subscribtion_Recorder(String record) //write the downloaded file into the local director
    {
        try
        {
            //final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
            //File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
            //File folder = new File(parentFolder.getParent()+"/peer1/src/main/resources"); //get the resources folder path
        	File folder = new File(".");
            FileWriter writer = new FileWriter(folder+"/Subscribtion_Records.txt",true);//initiate writer
            /////////////////////////////////////////////////////////////////////////////
            writer.write(record+"\n");                                //write
            writer.close();                                           //close writer
        }
        catch(UnknownHostException unknownHost){                      //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                               //To Handle Input-Output Exception
            ioException.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    synchronized void publishMessage() {
        try {
            String topicName;                             // define an integer peer ID which is the  peer port
            List<Message> messageList;                    //String array used for splitting the received message
            /////////////////////////////////////////////////////////////////////////////
            String pubIP = conn.getInetAddress().getHostName();    //save the peer IP into peerIP
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream()); //initiate object input stream to read from peer
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
            	messageList  = indexBus.get(topicNameStr);
            	Message m = new Message(messageList.size(), messageModel.getData(),topicNameStr );
            	if(messageList != null){
            		messageList.add(m);
            	}
            	indexBus.put(topicNameStr, messageList);
            	System.out.println("Added new message  "+messageModel.getData() + " in topic "+topicNameStr );
            }else{
            	System.out.println("Invalid object passed . returning....");
            	
            }
            
            /////////////////////////////////////////////////////////////////////////////
                  //split the incoming message to adapt the local format

             		// store peer ID
            
                in.close();                                         //close reader
                conn.close();                                       //close connection
            }
        /////////////////////////////////////////////////////////////////////////////
        catch(UnknownHostException unknownHost){                                           //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                                                    //To Handle Input-Output Exception
            ioException.printStackTrace();
        }
         catch (Exception e) {                                      //track general errors
            e.printStackTrace();
            System.out.println(e.toString());
        }

        finally {
            System.out.println("Type the action number as following:");
            System.out.println("1. To exit.\n");
            Thread.currentThread().stop();
        }
    }
}
