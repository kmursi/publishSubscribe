package com.aos.pubsub.services.model;

import java.io.Serializable;

/**
 * Created by kmursi on 3/11/17.
 */
public class Message implements MessageMarker,Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;        //holds peer ID
    String data;      //holds message data or file name
    String topicName;
    public Message(){
    	
    }
    public Message(int id, String data, String topicName)
    {
        this.id=id;
        this.data=data;
        this.topicName=topicName;
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
    
    
}