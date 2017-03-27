package com.aos.pubsub.services.eventBus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;



/**
 * Created by kmursi on 3/10/17.
 */
public class Listener extends Thread {
    Socket conn;
    String input;
    int listeningPort, registeringPort = 60000, searchingPort = 60001;    //each port hold a deffirent function
    static int maxsize = 0;
    /* create a hash map table that holds a concurrent hash map to assure synchronization
    *  each hash element contains a string ID (file name) and array of Messages*/
    static volatile Map<String, Message[] > indexBus = new ConcurrentHashMap<String, Message[] >();

    /*********************************************************************************************/

    public Listener(Socket s, int port) {
        conn = s;                                       // let the local socket to have the value of the received one
        this.listeningPort = port;                      // let the local port to have the value of the received one
    }

    /*********************************************************************************************/

    public synchronized void run() {
        if (listeningPort == registeringPort)           //call Register_a_File() if its port is connected with a peer
            Register_a_File();
            /////////////////////////////////////////////////////////////////////////////
        else if (listeningPort == searchingPort)        //call Register_a_File() if its port is connected with a peer
            Search_for_a_File();
    }

    /*********************************************************************************************/

    synchronized void Register_a_File() {
        try {
            int peerID;                             // define an integer peer ID which is the  peer port
            Message m;                              //define a message object
            String[] inputArray;                    //String array used for splitting the received message
            /////////////////////////////////////////////////////////////////////////////
            String peerIP = conn.getInetAddress().getHostName();    //save the peer IP into peerIP
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream()); //initiate object input stream to read from peer
            input = (String) in.readObject();               //read
            /////////////////////////////////////////////////////////////////////////////
            inputArray = input.split("-");            //split the incoming message to adapt the local format
            peerID = Integer.parseInt(inputArray[0]);        // store peer ID
            System.out.println("Peer number: " + peerIP +" "+ peerID+ " connected.\n");
            System.out.println("File " + input + " index created in the server \n");
            /////////////////////////////////////////////////////////////////////////////
            Date date = new Date();
            /////////////////////////////////////////////////////////////////////////////
            m = new Message(Integer.toString(peerID), peerIP, "R", inputArray[1], date); //new Message object with values
            List<Message> current1 = new ArrayList<Message>();      //initiating a list
            Message[] current = indexBus.get(inputArray[1]);        //store the current list of a hasmapkey into the list
            Message[] m2;                                           //m2 will hold the new list
            if(indexBus.containsKey(inputArray[1])) {               //check if there is an existing list of the same topic
                m2 = new Message[current.length + 1];
                for (int i = 0; i <= current.length; i++) {
                    if (i < current.length) {                       //store the old list to the new one
                        m2[i] = current[i];
                    } else {
                        m2[i] = m;                                  //store the new value to the list
                    }
                }
            }
            /////////////////////////////////////////////////////////////////////////////
                else                                                //if the is no existing topic, store new topic
                {
                    m2 = new Message[1];
                    m2[0] = m;
                }
            /////////////////////////////////////////////////////////////////////////////
                indexBus.put(inputArray[1].trim(), m2);             //store the hashmap element
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
            System.out.println("1. To exit.");
            Thread.currentThread().stop();
        }
    }

    /*********************************************************************************************/

    synchronized void Search_for_a_File() {
        String reply="";
        try {
            /////////////////////////////////////////////////////////////////////////////
            String peerIP = conn.getInetAddress().getHostName(); //store peer IP
            System.out.println("Peer number: " + peerIP + " connected.\n");
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream()); //define object reader
            input = (String) in.readObject();                           //read
            /////////////////////////////////////////////////////////////////////////////
            if (indexBus.containsKey(input.trim())) {                   //if topic list exist
                Message [] current = indexBus.get(input.trim());        //store topic list into array
                 for(int i=0; i<current.length;i++) {                   //read the array elements and store as a reply
                     reply = reply+"Peer ID: "+current[i].id + ", Peer IP: " + current[i].ip + "\n";
                }
            } else {
                reply = "File not found\n";                             //topic with received name is not registered on the server
            }
            /////////////////////////////////////////////////////////////////////////////
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream()); //define object writer
            out.writeObject(reply);                                                  //write the reply to the peer
            out.flush();
            /////////////////////////////////////////////////////////////////////////////
            in.close();                                                 //close reader
            out.close();                                                //close writer
            conn.close();                                               //close connection to the peer
        }
        catch(UnknownHostException unknownHost){                                           //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                                                    //To Handle Input-Output Exception
            ioException.printStackTrace();
        }
        catch (Exception e) {                                         //track general errors
            System.out.println(e.toString());
            e.printStackTrace();
        }
        finally {
            System.out.println("Type the action number as following:");
            System.out.println("1. To exit.");
            Thread.currentThread().stop();
        }
    }
}