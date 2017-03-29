package com.aos.pubsub.services;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.aos.pubsub.services.model.MessageMarker;
import com.aos.pubsub.services.model.TopicModel;

public class File_Handler {
	private ObjectMapper mapper = new ObjectMapper();
    String peerID;
    String serverIP = "localhost";
    ObjectOutputStream out;
    Socket socket;
    /*********************************************************************************************/

    public File_Handler (String peerID, String serverIP)
    {
        this.serverIP=serverIP;
        this.peerID=peerID;             //store peer ID into peerID
    }

    /*********************************************************************************************/

    boolean isValidName(String text)   //this method used to check the validity of the file name
    {
        if(text.contains("."))         //file name must contains '.'
        {
            String[] textArray = text.split(Pattern.quote(".")); //split
            if(textArray.length==2)    //has two sides
            {
                if (!(textArray[0].equals(null)) && !(textArray[1].equals(null))) //firs side and second side are not empty
                    return true;
            }
        }
        return false;
    }

    /*********************************************************************************************/

    boolean isValidDownloadName(String text)    //this method used to check the validity of the download request format
    {
        if(text.contains("-"))                  //split by '-'
        {
            String[] textArray = text.split(Pattern.quote("-"));

            if (textArray.length == 3)          //must contains three sides
            {
                if(isValidName(textArray[2]))   // the third side which contains the file name must be valid
                {
                    if (!(textArray[0].equals(null)) && !(textArray[1].equals(null))&& !(textArray[2].equals(null))) //the three sides are not empty
                        return true;
                }
            }

        }
        return false;
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
            }
            catch(UnknownHostException unknownHost){                       //To Handle Unknown Host Exception
                System.err.println("host not available..!");
            }
            catch(Exception e ){                                           //To Handle Input-Output Exception
                e.printStackTrace();
            }
    }

    /*********************************************************************************************/

    boolean file_exist(String fileName) {                              //check the file existence on the working directory
        final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
        File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
        File folder = new File(parentFolder.getParent()+"/peer1/src/main/resources"); //get the resources folder path
        File[] listOfFiles = folder.listFiles();                                        //store files into file array
        for (int i = 0; i < listOfFiles.length; i++) {                 //loop through each of the files looking for filenames that match
            String filename = listOfFiles[i].getName();                //store the file name
            if (filename.startsWith(fileName)) {                       //if exist, return true
                return true;
            }

        }
        return false;
    }

    /*********************************************************************************************/

    public void Search_for_a_File(String fileName)                  //search file on the server
    {
        String message;
        try{
            socket = new Socket(serverIP, 60001);              //initiate socket withe the server through server searching port
            System.out.println("\nConnected to the server..\n");
            /////////////////////////////////////////////////////////////////////////////
            out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
            out.flush();
            out.writeObject(fileName);                             //send
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());//initiate reader
            message = in.readObject().toString();                  //store received message into message
            /////////////////////////////////////////////////////////////////////////////
            if  (message.trim().equals("File not found")) {        //check the file index existence in the server based on the server message

                System.out.println("File not found !\n");
            }
            else {
                System.out.println( "File:("+fileName+ ") was found at peer\\peers:\n"); //list the peers who have the file
                System.out.print(message);
            }
            /////////////////////////////////////////////////////////////////////////////
            in.close();                                            //close reader
            out.close();                                           //close writer
            socket.close();                                        //close connection
        }
        catch(UnknownHostException unknownHost){                   //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch (Exception e) {
            e.printStackTrace();
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
            writer.write(fileContent);                                //write
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

    public void Download_From_Peer()                                  //download file from a peer
    {
        Scanner uInput = new Scanner(System.in);
        String fileName;
        /////////////////////////////////////////////////////////////////////////////
        System.out.println("Enter the peer id, IP address, and file name using this format (pppp-IP-file.txt):");
        String uString = uInput.nextLine();                           //get the user input
        /////////////////////////////////////////////////////////////////////////////
        try
        {
            if(isValidDownloadName(uString))                          //check the validity of the input format
            {

                String [] textArray = uString.split(Pattern.quote("-")); //split
                int peerPort = Integer.parseInt(textArray[0]);        //first side contains port number
                String ip = textArray[1].trim();                      //second side contains ip address
                fileName = textArray[2].trim();                       //third side contains the file name
                System.out.println("File name:"+fileName);
                /////////////////////////////////////////////////////////////////////////////
                if(!(peerPort==Main.port)) {
                    socket = new Socket(ip, peerPort);                    //initiate client socket
                    System.out.println("\nConnected to peer : " + ip + " through port : " + peerPort + "\n");
                    out = new ObjectOutputStream(socket.getOutputStream());//initiate writer
                    out.flush();
                    out.writeObject(fileName);                             //write
                    out.flush();
                    /////////////////////////////////////////////////////////////////////////////
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); //initiate reader
                    String fileContent = in.readObject().toString();       //read and store into content
                    /////////////////////////////////////////////////////////////////////////////
                    if (fileContent.trim().equals("File not found".trim())) //check file existence in the peer
                    {
                        System.out.println("File not found");
                    } else {
                        Create_Local_File(fileName, fileContent);          //call create file function and attache file name and content
                        System.out.println(fileName + " has been downloaded successfully\n");
                    }
                    /////////////////////////////////////////////////////////////////////////////
                    in.close();                                            //close reader
                    out.close();                                           //close writer
                    socket.close();                                         //close connection
                }
                else
                {
                    System.out.println("You are not allowed to download from the current peer that you are using !\n");
                }
            }
            else
            {
                System.out.print("Wrong input format");
            }
        }
        catch(UnknownHostException unknownHost){                        //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                                 //To Handle Input-Output Exception
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    /*********************************************************************************************/

}
