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
import java.util.Scanner;
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
        this.serverIP=serverIP;											//store IP into serverIP
        this.peerID=peerID;             								//store peer ID into peerID
    }

    /*********************************************************************************************/

    public void publishTopic(MessageMarker topicModel)                      //Register with index server Method
    {
        /////////////////////////////////////////////////////////////////////////////
    	try {
    		System.out.println("=======================================================");
                socket = new Socket(serverIP, 60000);                		//connect to the registration socket on the server
                System.out.println("Connected to the server "+serverIP+" to register a new topic.");
                out = new ObjectOutputStream(socket.getOutputStream());     //initiate writer
                out.flush();
                out.writeObject(mapper.writeValueAsString(topicModel));      //send the message
                out.flush();
                /////////////////////////////////////////////////////////////////////////////
                out.close();                                               //close writer
                socket.close();                                            //close socket
                System.out.println("Your topic has been registered on the eventBus !");
                ////////////////////////////////////////////////////////////////////
                TopicModel topic = null;
                List<Message> messageList;
                String topicName; 
                if(topicModel instanceof TopicModel){						//double check the model type
                	topic = (TopicModel) topicModel;						//cast the model to topic
                	long createdDate = new Date().getTime();				//set issuing time
                    topic.setCreatedOn(createdDate);
                    topic.setUpdatedOn(createdDate);
                    messageList = topic.getMessageList(); 
                    topicName = topic.getTopicName();   					//set topic name
                    if(messageList == null){
                    	messageList = new ArrayList<Message>();
                    	topic.setMessageList(messageList);
                     } 
                localIndexBus.put(topicName, messageList);					//add the message to the local indexbus
                System.out.println("=======================================================\n");
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
    	Scanner in = new Scanner(System.in);
    		System.out.println("Enter the number of messages:");
    		int number = in.nextInt();
    		System.out.println("=======================================================");
    		if(messageModel instanceof Message){
    			Message m1 = (Message) messageModel;
    			if(localIndexBus.containsKey(m1.getTopicName()))
    			{
    				try{
                socket = new Socket(serverIP, 60001);                //connect to the registration socket on the server
                System.out.println("Connected to the server "+serverIP+" to publish new messages.");
                Random ran = new Random();							//create random number
                Date dt = new Date();								//get current date
                int counter = 0;
                long startTime = System.currentTimeMillis();		//current time in msec 
                long avgTime=0;
                while (counter < number) {
                   Message m = new Message();						//create new message object
                   m.setTopicName(m1.getTopicName());
                   m.setDurable(duarapility);						//set durability
                   m.setData(dt.toString()+"_"+ran.nextInt());		//set data
                   long createdDate = new Date().getTime();
                   m.setCreatedOn(createdDate);						//set dateTime
                   m.setExpirationDate(getExpirationDate(duration));
                   out = new ObjectOutputStream(socket.getOutputStream());  //initiate writer
                   out.flush();
                   out.writeObject(mapper.writeValueAsString(m));           //send the message
                   out.flush();
                   counter++;
                   ////////////////////////////////////////////////////////////////////
                   List<Message> messageList;
                   	String topicNameStr = m.getTopicName();					//store topic name
                   	messageList  = localIndexBus.get(topicNameStr);
                   	
                   	if(messageList != null){								//check the list availability
                   		messageList.add(m);
                   	avgTime+=(System.currentTimeMillis()-startTime);
                   	localIndexBus.put(topicNameStr, messageList);
                   	////////////////////////////////////////////////////////////////////
                   	}
                }
                System.out.println("Messages have been successfully published to the event bus.");
                System.out.println("Avrage time to publish "+number+" messages is "+avgTime/number+" msec.");
                /////////////////////////////////////////////////////////////////////////////
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
    				System.out.println("Topice ("+m1.getTopicName()+") is not existed in the EventBus !");
    				System.out.println("Publishing request has been aborted !");
    			}
    			System.out.println("=======================================================\n");
    	}
    	
    }
    
    /*********************************************************************************************/
    
    public long getExpirationDate(int numberOfDays)
    {
    	Date dt = new Date();								//get current time	
    	Calendar c = Calendar.getInstance(); 				//create calendar object
    	c.setTime(dt); 										//set calendar to date
    	c.add(Calendar.DATE, numberOfDays);					//add number of days
    	dt = c.getTime();							
    	return dt.getTime();								//return the expiration dateTime in long format
    }

    /*********************************************************************************************/


    public void Subscribe_Request(String topicName)                  //search file on the server
    {
    	System.out.println("=======================================================");
        String message;
        ObjectInputStream in;
        try{
            socket = new Socket(serverIP, 60002);              		//initiate socket with the server through server searching port
            System.out.println("Connected to the server "+serverIP+" to subscribe topic "+topicName+".");
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            out.flush();
            message = peerID+"-"+topicName;
            out.writeObject(message);                        		//send
            out.flush();
		            in = new ObjectInputStream(socket.getInputStream());//initiate reader
		            message = in.readObject().toString();               //store received message into message
		            /////////////////////////////////////////////////////////////////////////////
		            if  (message.trim().equals("Topic not found")) //check the file index existence in the server based on the server message
		            {        
		                System.out.println("Topic not found !");
		            }
		            else 
		            {
		                //System.out.println( "Your subscribing request to topic ("+topic+ ") succeeded \n"); //list the peers who have the file
		                System.out.print(message+"\n");
		                pullRequest(topicName, 0);
		            }
            	
            /////////////////////////////////////////////////////////////////////////////
        }
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=======================================================\n");
    }
    
    public void pullRequest(String topicName, int lastMessageIndex)
    {
    	new Listener(serverIP,topicName, lastMessageIndex).start();	//start new thread of the listener object
    }
    
    public void pullRequest(String topicName, Date date)
    {
    	System.out.println("=======================================================");
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
            Socket socket = new Socket(serverIP, 60004);              //initiate socket with the server through server searching port
            System.out.println("Connected to the server "+serverIP+" to pull messages from topic "+topicName+".");
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            out.flush();
            out.writeObject(message);                        //send
            out.flush();
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
            System.out.println("\nConncetion has end with the eventBus!");
        }
        catch (EOFException exc)
    	{
        	
    	}
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally{
        	try {
			in.close();                                           //close reader
            out.close();                                           //close writer
            long msgRecievingEndTime = new Date().getTime();
            if(recievedString.equals("null"))
            	System.out.println("There are no published messages available after "+date.toString()+"...!");
            else
        	System.out.println("Received "+msgCount+" messages  in "+ (msgRecievingEndTime -msgRecievingStartTime) +" milliseconds" );
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

    /*********************************************************************************************/

}
