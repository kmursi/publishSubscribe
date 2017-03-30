package com.aos.pubsub.services.eventBus;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.pubsub.services.model.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectMapper;
import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.SubscribtionModel;
import com.aos.pubsub.services.model.TopicModel;


public class SubscriberHandler extends Thread {
	String IP , topicName;
	int port;
	ObjectOutputStream out;
	private ObjectMapper mapper = new ObjectMapper();
	static volatile Map<String, List<Message>> sentMessage = new ConcurrentHashMap<String, List<Message>>();
	public SubscriberHandler(String IP , int port, String topicName)
	{
		this.IP=IP;
		this.port=port;
		this.topicName=topicName;
	}
	public synchronized void run()
	{
		//Socket socket = new Socket(IP, port);
		
	}
	
	boolean chekcIfMessageNew(Map m)
	{
		
		return false;
	}
	
	//This method handles the sending to the subscriber
	void pushToSubscriber(Socket socket, MessageMarker marker )
	{
		try
		{
			System.out.println("\nConnected to the server..\n");
            out = new ObjectOutputStream(socket.getOutputStream());   //initiate writer
            out.flush();
            out.writeObject(mapper.writeValueAsString(marker));                                 //send the message
            out.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//get all the topics that a subscriber is subscribing
	String [] subscriptionList (String IP , int port)
	{
		BufferedReader br = null;
		FileReader fr = null;
		String resultArray [] = null;
		int counter=0;
		String splitter [];
		try {
			File folder = new File(".");
            //FileWriter writer = new FileWriter(folder+"/Subscribtion_Records.txt",true);
			File f = new File (folder+"/Subscribtion_Records.txt");
			fr = new FileReader(folder+"/Subscribtion_Records.txt");
			br = new BufferedReader(fr);
			resultArray= new String[(int)f.length()];
			String sCurrentLine;
			
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.contains(IP+"-"+port))
				{
					splitter = sCurrentLine.split("-");
					resultArray [counter]= splitter[2];
				}
				counter++;
			}
				System.out.println(sCurrentLine);
			}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return resultArray;
	}
}
