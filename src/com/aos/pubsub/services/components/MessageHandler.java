package com.aos.pubsub.services.components;


import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;
import com.opencsv.CSVReader;

public class MessageHandler {
	private ObjectMapper mapper = new ObjectMapper();
    String peerID;
    String serverIP = "localhost";
    ObjectOutputStream out;
    Socket socket;
    static volatile Map<String, List<Message>> localIndexBus = new ConcurrentHashMap<String, List<Message>>();
    /*********************************************************************************************/

    public MessageHandler (String peerID, String serverIP)
    {
        this.serverIP=serverIP;
        this.peerID=peerID;             //store peer ID into peerID
    }

    /*********************************************************************************************/

    public void publishTopic(MessageMarker topicModel)                      //Register with index server Method
    {
        /////////////////////////////////////////////////////////////////////////////
    	try {
                socket = new Socket(serverIP, 60000);                //connect to the registration socket on the server
                System.out.println("\nConnected to the server..\n");
                out = new ObjectOutputStream(socket.getOutputStream());   //initiate writer
                out.flush();
                out.writeObject(mapper.writeValueAsString(topicModel));                                 //send the message
                out.flush();
                /////////////////////////////////////////////////////////////////////////////
              //  System.out.println("Topic '"+topicModel.getTopicName()+"'  has been published successfully on the server!!\n");
                out.close();                                               //close writer
                socket.close();                                            //close socket
                System.out.println("\nYour topic has been registered on the eventBus ! \n");
                ////////////////////////////////////////////////////////////////////
                TopicModel topic = null;
                List<Message> messageList;
                String topicName; 
                if(topicModel instanceof TopicModel){
                	topic = (TopicModel) topicModel;
                	long createdDate = new Date().getTime();
                    topic.setCreatedOn(createdDate);
                    topic.setUpdatedOn(createdDate);
                    messageList = topic.getMessageList(); 
                    topicName = topic.getTopicName();   
                    if(messageList == null){
                    	messageList = new ArrayList<Message>();
                    	topic.setMessageList(messageList);
                     } 
                localIndexBus.put(topicName, messageList);
                ////////////////////////////////////////////////////////////////////
            }
    	}
            catch(UnknownHostException unknownHost){                       //To Handle Unknown Host Exception
                System.err.println("host not available..!");
            }
            catch(Exception e ){                                           //To Handle Input-Output Exception
                e.printStackTrace();
            }
    }
    
    public void publishMessage(MessageMarker messageModel, int duration, boolean duarapility)                      //Register with index server Method
    {
        /////////////////////////////////////////////////////////////////////////////
    	
    		if(messageModel instanceof Message){
    			Message m1 = (Message) messageModel;
    			if(localIndexBus.containsKey(m1.getTopicName()))
    			{
    				try{
                socket = new Socket(serverIP, 60001);                //connect to the registration socket on the server
                System.out.println("\nConnected to the server..\n");
                
                
              //  while ((line = reader.readNext()) != null) {
                Random ran = new Random();
                Date dt = new Date();
                int counter = 0;
                while (counter < 2) {
                   Message m = new Message();
                   m.setTopicName(m1.getTopicName());
                   m.setDurable(duarapility);
                   m.setData(dt.toString()+"_"+ran.nextInt());
                   long createdDate = new Date().getTime();
                   m.setCreatedOn(createdDate);
                   m.setExpirationDate(getExpirationDate(duration));
                   out = new ObjectOutputStream(socket.getOutputStream());   //initiate writer
                   out.flush();
                   out.writeObject(mapper.writeValueAsString(m));                                 //send the message
                   out.flush();
                   counter++;
                   ////////////////////////////////////////////////////////////////////
                   TopicModel topic = null;
                   List<Message> messageList;
                   String topicName; 
                   
                   	String topicNameStr = m.getTopicName();
                   	messageList  = localIndexBus.get(topicNameStr);
                   	
                   	if(messageList != null){
                   		messageList.add(m);
                   	
                   	localIndexBus.put(topicNameStr, messageList);
                   	////////////////////////////////////////////////////////////////////
                }
                }
               System.out.println(counter);
                /////////////////////////////////////////////////////////////////////////////
              //  System.out.println("Topic '"+topicModel.getTopicName()+"'  has been published successfully on the server!!\n");
                out.close();                                               //close writer
                socket.close();                                            //close socket
            }
    				
            catch(UnknownHostException unknownHost){                       //To Handle Unknown Host Exception
                System.err.println("host not available..!");
            }
            catch(Exception e ){                                           //To Handle Input-Output Exception
                e.printStackTrace();
            }
    	}
    			else{
    				System.out.println("\nTopice ("+m1.getTopicName()+") is not existed in the EventBus !\n");
    				System.out.println("\nPublishing request has been aborted !\n");
    			}
    	}
    	
    }
    
    
    public long getExpirationDate(int numberOfDays)
    {
    	Date dt = new Date();
    	Calendar c = Calendar.getInstance(); 
    	c.setTime(dt); 
    	c.add(Calendar.DATE, numberOfDays);
    	dt = c.getTime();
    	return dt.getTime();
    }

    /*********************************************************************************************/


    public void Subscribe_Request(String topicName)                  //search file on the server
    {
        String message;
        ObjectInputStream in;
        try{
            socket = new Socket(serverIP, 60002);              //initiate socket withe the server through server searching port
            System.out.println("\nConnected to the server..\n");
            /////////////////////////////////////////////////////////////////////////////
            
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            out.flush();
            message = peerID+"-"+topicName;
            out.writeObject(message);                        //send
            out.flush();
            
		            in = new ObjectInputStream(socket.getInputStream());//initiate reader
		            message = in.readObject().toString();                  //store received message into message
		            /////////////////////////////////////////////////////////////////////////////
		            if  (message.trim().equals("Topic not found")) 
		            {        //check the file index existence in the server based on the server message
		
		                System.out.println("Topic not found !\n");
		            }
		            else 
		            {
		                //System.out.println( "Your subscribing request to topic ("+topic+ ") succeeded \n"); //list the peers who have the file
		                System.out.print(message+"\n");
		                pullRequest(topicName, 0);
		            }
            	
            /////////////////////////////////////////////////////////////////////////////
            
            //in.close();                                            //close reader
            //out.close();                                           //close writer
            //socket.close();                                        //close connection
        }
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void pullRequest(String topicName, int lastMessageIndex)
    {
    	new Listener(serverIP,topicName, lastMessageIndex).start();
    }
    
    public void pullRequest(String topicName, Date date)
    {
    	MessageMarker messageMarker;
    	Message messageModel = null;
    	String message=topicName+"-"+date;
    	long milliseconds = date.getTime();
    	message = topicName +"-"+ milliseconds;
    	long msgRecievingStartTime=0;
    	ObjectInputStream in = null;
    	int msgCount=0;
    	String recievedString ="null";
        try{
            Socket socket = new Socket(serverIP, 60004);              //initiate socket withe the server through server searching port
            System.out.println("\nConnected to the server..\n");
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            
            out.flush();
            //System.out.println("\nhi\n");
            out.writeObject(message);                        //send
            out.flush();
            //System.out.println("\nhi\n");
            /////////////////////////////////////////////////////////////////////////////
            in = new ObjectInputStream(socket.getInputStream());//initiate reader
            if(socket.isConnected())
            {
            		msgRecievingStartTime = new Date().getTime();
            		msgCount = 0 ;
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
			            	System.out.println("Received new message  "+messageModel.getData() + " from topic "+topicNameStr );
			            }else{
			            	System.out.println("Invalid object passed . returning....");
			            }
			            msgCount++;
			            
	            	}
	            	
	            	
            }
        
            /////////////////////////////////////////////////////////////////////////////
            in.close();                                            //close reader
            out.close();                                           //close writer
            socket.close();                                        //close connection
            System.out.println("\nConncetion has end with the eventBus!\n");
            

        }
        catch (EOFException exc)
    	{
        	//System.out.println("Message received successfully ! ");
    	}
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally{
        	try {
			in.close();
			                                           //close reader
            out.close();                                           //close writer
            
            long msgRecievingEndTime = new Date().getTime();
            if(recievedString.equals("null"))
            	System.out.println("There are no published messages available after "+date.toString()+"...!\n");
            else
        	System.out.println("Received "+msgCount+" messages  in "+ (msgRecievingEndTime -msgRecievingStartTime) +" milliseconds" );
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
    }
    
    
    
    /*********************************************************************************************/

    public void Create_Local_File(String fileName,String fileContent) //write the downloaded file into the local director
    {
        try
        {
            final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
            File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
            File folder = new File(parentFolder.getParent()+"/peer1/src/main/resources"); //get the resources folder path

            FileWriter writer = new FileWriter(folder+"/"+fileName.trim(),true);//initiate writer
            /////////////////////////////////////////////////////////////////////////////
            writer.write(fileContent+"\n");                                //write
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

    /*********************************************************************************************/

}
