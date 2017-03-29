package com.aos.pubsub.services;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//PeerServer
class Listener extends Thread{
    int port;
    String message;
    ServerSocket server;
    Socket connection;
    BufferedReader br = null;
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
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream()); //initiate reader
                message = (String)in.readObject();
                /////////////////////////////////////////////////////////////////////////////
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream()); //initiate writer
                out.flush();
                String content=null;
                try
                {
                    final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
                    File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
                    File folder = new File(parentFolder.getParent()+"/src/main/resources"); //get the resources folder path
                    FileReader fr = new FileReader(folder+ "/"+message.trim());          //Reads the filename into file reader
                    BufferedReader br = new BufferedReader(fr);
                    String value=new String();
                    while((value=br.readLine())!=null)                //Appending the content read from the BufferedReader object until it is null and stores it in str
                        content=content+value+"\r\n";                 //append the content out of the read lines
                    br.close();
                    fr.close();
                    System.out.println("File "+message+" has been sent successfully to peer "+peerIP);
                }
                catch(UnknownHostException unknownHost){               //To Handle Unknown Host Exception
                    System.err.println("host not available..!");
                }
                catch(Exception e)
                {
                    System.out.println("File not found");
                    content="File not found".trim();                //file not found message will be sent to the peer
                }
                /////////////////////////////////////////////////////////////////////////////

                out.writeObject(content);                           //content sending to the peer
                out.flush();                                        //close writer
                in.close();                                         //close reader
                connection.close();                                 //close connection

        }

        catch(ClassNotFoundException n){                            //To Handle Exception for Data Received in Unsupported or Unknown Formats
            System.err.println("data format is unknown ..!");
        }
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                            //To Handle Input-Output Exceptions
            ioException.printStackTrace();
        } finally {
            Thread.currentThread().stop();                         //end the current thread
        }
    }
    /*********************************************************************************************/
}