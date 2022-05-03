//package DA3;
//package dA2;

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
	private Hashtable<String, Integer> subscriberList;
	boolean notified;
	
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
		
			subscriberList = new Hashtable<String, Integer>();
			Rooms= new Hashtable<String, Integer>();
			 notified=true;
/*		socket = new DatagramSocket(SUB_PORT);
			System.out.println(socket);
			listener.go();
	*/		
				subscriberList.put("1",50003);
				subscriberList.put("2",50004);
				subscriberList.put("3",50005);
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
			
			System.out.println("Enter SUBSCRIBE to subscribe to a Room or UNSUBSCRIBE to unsubscribe from a Room: ");
					String startingString = sc.next();
					
			System.out.println("Enter SUBSCRIBE to subscribe to a Room or\nUNSUBSCRIBE to unsubscribe from a Room: "+ startingString);
			if (startingString.toUpperCase().contains(UNSUBSCRIBE)) {
//				if(SUB_POR==0)
	//		{System.out.println("Subscribe First");}
		//		else {
					unsubscribe();
				
			this.wait(); // wait for ACK
		//	System.out.println("1");
			this.wait();//}// wait for MESSAGE 
	//		System.out.println("2");
		    continue J1;
			} else if (startingString.toUpperCase().contains(SUBSCRIBE)) {
			
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
		while ((notified==true)) {
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
		System.out.println("Please enter Room to subscribe to: ");
		String data = sc.next(); 
		port(data);
		subscriberList.put(data,BKR_PORT+Integer.parseInt(data));
		SUB_POR=subscriberList.get(data); 
	//	 System.out.println(SUB_POR);
		 if(SUB_POR!=0) {
	socket = new DatagramSocket(SUB_POR);
	 //System.out.println(SUB_POR);
			listener.go();
		 }
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
	  subscriberList.put(data,BKR_PORT+Integer.parseInt(data));
		SUB_POR=subscriberList.get(data); 
//		 System.out.println(SUB_POR);
		 if(SUB_POR!=0) {
	socket = new DatagramSocket(SUB_POR);
	// System.out.println(SUB_POR);
			listener.go();
		 }
	//  int seq=getseq(data);
	//  System.out.println("Please enter a Room to unsubscribe from: " + data);
	  System.out.println("Sending packet..." );
		DatagramPacket packet = createPackets(USUB, 0, data, dstAddress)[0];
		try {
			socket.send(packet);
		} catch (IOException e) {
		}
		System.out.println("Packet sent");
	}
   public void stop() {
	System.exit(0);
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
			     
				System.out.println("You have exited the Room " +getMessage(data));
				sendAck(packet);
				notified=false;
				invalidInput=false;
			
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
				if (getMessage(data).equals("This Room does not exist on your database.")) {
					notified=true;
					
					try {
		
							Subscriber s1=	new Subscriber();
								s1.start(); 
					} catch (java.lang.Exception e) {
					}
				}
					 
				else  {
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
