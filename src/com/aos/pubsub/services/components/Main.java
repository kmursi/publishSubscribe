package com.aos.pubsub.services.components;

//
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.aos.pubsub.services.model.Message;
import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;

/*********************************************************************************************/
public class Main extends Thread{
    static int port;
    Main(int port)							// Main constructor receiving port number
    {
        this.port = port;					//store port to local variable
    }
    /*********************************************************************************************/
    public static void main(String[] args) throws InterruptedException {
        String userInput,serverIP,topicToPubSub;               //define user input variable
        System.out.println("Enter the Indexing Server IP:");
        Scanner uIn = new Scanner(System.in);
        serverIP=uIn.nextLine().trim();
        System.out.println("Enter the Peer ID:");
        String peerID=uIn.nextLine().trim();     			// the peer ID used as a port listener as well
        /////////////////////////////////////////////////////////////////////////////
        try {
            												// Check if the server is up !
            if(InetAddress.getByName(serverIP).isReachable(5000))
                System.out.println("Server is up..!");
            else
            {
                System.out.println("Server not found..!");
                System.out.println("Exiting..");
                System.exit(0);								//exit the application
            }
        } catch (IOException ex) {
            System.out.println("Server not found..!");
            System.out.println("Exiting..");
            System.exit(0);
        }
        /////////////////////////////////////////////////////////////////////////////
        Thread thread;                      						//define thread
        try
        {
            thread = new Thread (new Main(Integer.parseInt(peerID))); //initiate listener thread
            thread.start();                             			 //start listener
        } catch(Exception e){                           			//track general errors
            e.printStackTrace();
        }
        /////////////////////////////////////////////////////////////////////////////
        MessageHandler fh = new MessageHandler(peerID,serverIP);     //define and initiate handler object from the main thread
        /////////////////////////////////////////////////////////////////////////////
        while (true)
        {
            //Printing the available services
        	System.out.println("=======================================================");
            System.out.println("Type the action number as following:");
            System.out.println("1. Register a topic on eventbus.");
            System.out.println("2. Publish  messages in a topic.");
            System.out.println("3. Subscribe a topic.");
            System.out.println("4. Messages pull request from a specific date.");
            System.out.println("5. To exit.");
            System.out.println("=======================================================\n");
            Scanner in = new Scanner(System.in);
            userInput = in.nextLine();                         			//get the chosen service from the user
           
             if (userInput.equals("1"))                    				//if user entered 2
            {
                System.out.println("Enter the topic name:");
                String topicName = in.nextLine();                     // get file name that user want to register
            	 System.out.println("Registering the topic "+topicName+"...");
                MessageMarker mm = null;
                TopicModel tModel = new TopicModel();
                tModel.setTopicName(topicName);
                mm = tModel;
                tModel.setDurable(true);
                fh.publishTopic(mm);             					//call register function and attach the file name
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("2"))                    		//if user entered 3
            {
            	int trails=0;
            	 System.out.println("Enter the topic name:");
                 String topicName = in.nextLine();                     // get file name that user want to register
                 
                 String isDurabe="";
                 String line;
                 while (isDurabe.equals("")) {							//wait for input to be filled
                	 System.out.println("Is the message duarable? yes/no");
 						
 							line = in.nextLine();
 							if(line.trim().equals("yes"))				//input = yes
 							{
 								isDurabe="true";						//message is durable
 								break;
 							}
 							else if(line.trim().equals("no"))			//input = no
 							{
 								isDurabe="false";						//message is not durable
 								break;
 							}
 							else if(trails==3)							//user can try for three times only
 							{
 								System.out.println("Exiting...");
 				                System.exit(0);  
 							}
 							trails++;
 							System.out.println("Sorry, invalid input! Please try again.");
 						
 					}
                 Integer duration=null;									//time for a message to be expired
                 while (true) {
                	 System.out.println("Enter number of days for the message to be expired:");
  						try {
  							duration = Integer.parseInt(in.nextLine());
  							break;
  						} catch (Exception e) {
  							if(trails==3)
  								break;
  							trails++;
  							System.out.println("Sorry, invalid input! Please try again.");
  						}
  					}
                 Message message = new Message(0,"",topicName);			//initiate a new message
                 fh.publishMessage(message,duration, Boolean.parseBoolean(isDurabe));//call register function and attach the file name
            }
           
            else if (userInput.equals("3"))                    //if user entered 3
            {
            	System.out.println("Enter the topic name:");
                String topicName = in.nextLine();  
                fh.Subscribe_Request(topicName);
            }
            else if (userInput.equals("5"))                    //if user entered 5
            {
                System.out.println("Exiting...");
                System.exit(0);                         		//exit the program
            }
            else if (userInput.equals("4"))                    //if user entered 4
            {
            	System.out.println("Enter the topic name");
                String topicName = in.nextLine();  
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",Locale.US); //the nedded time format
					System.out.println("Enter date and time in the format (yyyy-MM-ddTHH:mm) For example: (2019-01-01T01:01)");
					//System.out.println("For example, it is now " + format.format(new Date()));
					java.util.Date date = null;
					while (date == null) {
					String line = in.nextLine();
						try {
							date = format.parse(line);
						} catch (ParseException e) {
							System.out.println("Sorry, invalid input! Please try again.");
						}
					}
                fh.pullRequest(topicName, date); 					//exit the program
            }
            else
            {
                // awareness for the user of the correct options
                System.out.println("Wrong input! the input should be 1, 2, 3, or 4 ..");
            }
        }
    }


}




