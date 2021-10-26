//package DA3;
//package dA2;

/** Subscriber class for custom Publish-Subscribe protocol. Takes user input via a
 * terminal and interacts with the broker to subscribe and unsubscribe from topics.
 * Due to problems with threading I could not figure out how to get the subscriber
 * to wait for user input to subscribe and unsubscribe from topics while also waiting
 * on incoming packets to print messages of the topics that it is subscribed to. As a 
 * result the current implementation can subscribe to exactly one topic, then going to 
 * a state where it waits for messages from that topic. @author: Jack Gilbride
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class Subscriber extends Node   {
	
	Scanner sc=new Scanner (System.in);
	private static final String SUBSCRIBE = "SUB";
	public static final String UNSUBSCRIBE = "UNSUB";
	private Hashtable<String, Integer> subscriberMap;
	boolean notified=false;
	private Hashtable<String, Integer> Rooms;

	 int SUB_POR=0;
	private InetSocketAddress dstAddress;
	private boolean invalidInput;
    String data;
	public void port(String data) {
    	 this.data=data;
     }
	public int getseq(String data) {
//System.out.println("have ent");
		int seq = Rooms.get(data);
	//System.out.println("Seq " +seq);
	return seq;
	}
	
	Subscriber() {
		invalidInput = true;
		try {
		
			subscriberMap = new Hashtable<String, Integer>();
			Rooms= new Hashtable<String, Integer>();
			 notified=false;
/*		socket = new DatagramSocket(SUB_PORT);
			System.out.println(socket);
			listener.go();
	*/		
				subscriberMap.put("1",50003);
				subscriberMap.put("2",50004);
				subscriberMap.put("3",50005);
				Rooms.put("2", 2);
				Rooms.put("3", 3);
				       
			dstAddress = new InetSocketAddress(SUB_DST, BKR_PORT);
			//String data;
			
	//		System.out.println("hell0000000000000000kmksdkasndkasndkasndkandkansdkasnd"+data);
			
			
		} catch (java.lang.Exception e) {
		}
	}

	

	
public synchronized void start() throws Exception {
	J1:
	while (invalidInput == true) {
			
			System.out.println("Enter SUBSCRIBE to subscribe to a topic or UNSUBSCRIBE to unsubscribe from a topic: ");
					String startingString = sc.next();
					
			System.out.println("Enter SUBSCRIBE to subscribe to a topic or\nUNSUBSCRIBE to unsubscribe from a topic: "+ startingString);
			if (startingString.toUpperCase().contains(UNSUBSCRIBE)) {
//				if(SUB_POR==0)
			{System.out.println("Subscribe First");}
		//		else {
					unsubscribe();
				
			this.wait(); // wait for ACK
		//	System.out.println("1");
			//this.wait();//}// wait for MESSAGE 
	//		System.out.println("2");
		    continue J1;
			} else if (startingString.toUpperCase().contains(SUBSCRIBE)) {
				System.out.println("Please enter Room to subscribe to: ");
				String data = sc.next(); 
				port(data);
				subscriberMap.put(data,50001+Integer.parseInt(data));
				SUB_POR=subscriberMap.get(data); 
			//	 System.out.println(SUB_POR);
				 if(SUB_POR!=0) {
			socket = new DatagramSocket(SUB_POR);
			 //System.out.println(SUB_POR);
					listener.go();
				 }
				//	System.out.println(socket);
				//port(data);
				subscribe();
				//System.out.println("Not listening");
			    this.wait(); // wait for ACK
			    //System.out.println("Not listening2");
				this.wait(); // wait for MESSAGE
			} else {
				System.out.println("Invalid input.");
				invalidInput = true;
			}
		
		}
		while (!notified) {
			this.wait();
		}
	}
	public void Set(String data) {
		for(int i=1;i<Rooms.size();i++)
	   	{
	   		Rooms.put(data, i);
	   	}
//	System.out.println("hee"+ Rooms.size());
	}

	
	public synchronized void subscribe() throws SocketException {
//System.out.println("re");
		Set(data);
	//	System.out.println("no");
		
		//System.out.println("yes");
		int seq= getseq(data);
		
		
		
		//System.out.println("ye");
		System.out.println("Please enter a Room to subscribe to: " + data);
		System.out.println("Sending packet..."+seq);
	//	System.out.println(dstAddress);
		DatagramPacket packet = createPackets(SUB, seq, data, dstAddress)[0];
		try {
			socket.send(packet);
		} catch (IOException e) {
		}
		System.out.println("Packet sent");
	}

	
	public synchronized void unsubscribe() throws SocketException {
		
	  System.out.println("Please enter a Room to unsubscribe from: ");
	  String data = sc.next();
	  subscriberMap.put(data,50001+Integer.parseInt(data));
		SUB_POR=subscriberMap.get(data); 
//		 System.out.println(SUB_POR);
		 if(SUB_POR!=0) {
	socket = new DatagramSocket(SUB_POR);
	// System.out.println(SUB_POR);
			listener.go();
		 }
	//  int seq=getseq(data);
	  System.out.println("Please enter a Room to unsubscribe from: " + data);
	  System.out.println("Sending packet..." );
		DatagramPacket packet = createPackets(USUB, 0, data, dstAddress)[0];
		try {
			socket.send(packet);
		} catch (IOException e) {
		}
		System.out.println("Packet sent");
	}

	
	public static void main (String[] args) {
		try {
		
			new Subscriber().start();
		} catch (java.lang.Exception e) {
		}
	}


	@Override
	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			this.notify();
			
			byte[] data = packet.getData();
//			System.out.println("I have recieved");
			if (getType(data)==BRK){
			     
				System.out.println("We have exited the Room " +getMessage(data));
				sendAck(packet);
				notified=true;
				//this.start();
				try {
	
						Subscriber s1=	new Subscriber();
							s1.start(); 
				} catch (java.lang.Exception e) {
				}
			}
			else if (getType(data) == ACK) {
				System.out.println("ACK received for Sequence Number " + getSequenceNumber(data) + ".");
			} else if (getType(data) == Mes) {
				System.out.println("Message received: " + getMessage(data)); 
				sendAck(packet);
				if (getMessage(data).equals("This topic does not exist.")) {
					invalidInput = true;
					 
				} else  {
					invalidInput = false;
				}
			}else if (getType(data) == Pub) {
				System.out.println("New publication: " + getMessage(data));
				sendAck(packet);
			}
		} catch (Exception e) {
		}
	}
}
