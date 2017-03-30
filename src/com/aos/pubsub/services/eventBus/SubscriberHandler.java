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
import java.io.ObjectInputStream;
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
	String subIP;
	Socket socket;
	private ObjectMapper mapper = new ObjectMapper();
	static volatile  List<Message> subscriberMessage ;
	public SubscriberHandler(Socket socket,int port)
	{
		this.port=port;
		subIP = socket.getInetAddress().getHostName(); 
		this.socket=socket;
	}
	public synchronized void run()
	{
		//Socket socket = new Socket(IP, port);
		//ServerSocket socket = null;
		try 
		{
			//socket = new ServerSocket(port);
			String receivedMessage;
			Message message;
			//while(true)
			{
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream()); 
				if(!(receivedMessage = (String)in.readObject()).equals(null))
				{
					System.out.println("\nhi splitter\n");
				String splitter [] = receivedMessage.split("-");
				subscriberMessage = EventBusListener.indexBus.get(splitter[0]);
				if(Integer.parseInt(splitter[1].trim())<subscriberMessage.size())
				{
					for(int i=Integer.parseInt(splitter[1].trim())+1; i<subscriberMessage.size();i++)
					{
						message=subscriberMessage.get(i);
						System.out.println(message.getTopicName());
						//pushToSubscriber(message);
						System.out.println("\nConnected to the subscriber..\n");
			              //initiate writer
			            out.flush();
			            out.writeObject(mapper.writeValueAsString(message));                                 //send the message
			            out.flush();
					}
				}
			}
			}
			socket.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	boolean chekcIfMessageNew(Map m)
	{
		
		return false;
	}
	
	//This method handles the sending to the subscriber
	void pushToSubscriber( MessageMarker marker )
	{
		try
		{
			System.out.println("\nConnected to the subscriber..\n");
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
