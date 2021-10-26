//package DA3;
//package dA2;

/** Broker class for custom Publish-Subscribe protocol. Handles datagram packets to
 * facilitate the creation of, publication of, subscription to and unsubscription from
 * topics. @author: Jack Gilbride
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class Broker extends Node {
//	private Terminal terminal;
	
	private Hashtable<String, /*ArrayList<*/InetSocketAddress/*>*/> subscriberMap;

	private Hashtable<Integer, String> topicNumbers;
	Iterator<String> iterators;
	/*	ArrayList<*/InetSocketAddress/*>*/ socket1= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
	
	    Broker() {
		//this.terminal = terminal;
		try {
			socket = new DatagramSocket(BKR_PORT);
			listener.go();
		} catch (

		java.lang.Exception e) {
			e.printStackTrace();
		}
		subscriberMap = new Hashtable<String, /*ArrayList<*/InetSocketAddress/*>*/>();
		topicNumbers = new Hashtable<Integer, String>();
	       Iterator<String> iterators = subscriberMap.keySet().iterator();
		String[] rooms={"1","2","3"};
		/*	ArrayList<*/InetSocketAddress/*>*/ socketNumbers= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
		for(int i=0; i < rooms.length;i++) {
			topicNumbers.put(i, rooms[i]);
			subscriberMap.put(rooms[i], socketNumbers);
	//		System.out.println("Topic " + topicNumbers.get(i) + " was created.");
		}
			
	}

	public static void main(String[] args) {
		try {
//			Terminal terminal = new Terminal("Broker");
			new Broker().start();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	private boolean createTopic(byte[] data) {
	/*	ArrayList<*/InetSocketAddress/*>*/ socketNumbers= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
	
		
		String topicName = getMessage(data);
		if (!subscriberMap.containsKey(topicName)) {
			subscriberMap.put(topicName, socketNumbers);
			int topicNumber = getTopicNumber(data);
			topicNumbers.put(topicNumber, topicName);
//			System.out.println("Topic " + topicName + " was created.");
			return true;
		}
		return false;
	}


	private boolean publish(byte[] data) throws SocketException {
		int topicNumber = getTopicNumber(data);
	//	System.out.println("getting "+topicNumber);
		setType(data, Pub);
		if (topicNumbers.containsKey(topicNumber)) {
			String topicName = topicNumbers.get(topicNumber);
			//System.out.println("name"+topicName);
			InetSocketAddress dstAddresses = subscriberMap.get(topicName);
//			System.out.println("234   "+dstAddresses);
	//		System.out.println("235   "+socket1);
		//	System.out.println("236"   +subscriberMap);
			if((dstAddresses.equals(new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT)))){
				System.out.println("No one has Subscribed to this Room");
				return false;
			}
			
			else if (!(dstAddresses==null)) {
					//	for (int i = 0; i < dstAddresses.size(); i++) {
							try {DatagramPacket publication = new DatagramPacket(data, data.length, dstAddresses/*.get(i)*/);
							
								socket.send(publication);
								System.out.println("Topic " + topicName + " was published.");
							} catch (IOException e) {
								e.printStackTrace();
							}
					
					
					}
					//}
					return true;
			}
		return false;
	}

	private boolean subscribe(byte[] data, SocketAddress subscriberAddress) {
		String topicName = getMessage(data);
		//System.out.println("ropa " +topicName);
		if (subscriberMap.containsKey(topicName)) {
			/*ArrayList<*/InetSocketAddress/*>*/ subscribers = subscriberMap.get(topicName);
		//	System.out.println(subscriberMap.get(topicName));
		//	System.out.println(subscriberAddress);
			subscriberMap.put(topicName,(InetSocketAddress) subscriberAddress);
			//subscriberMap.remove(topicName);
			//subscriberMap.put(topicName, subscribers);
//			System.out.println(subscriberMap.get(topicName));
			System.out.println("A new subscriber subscribed to " + topicName + ".");
			return true;
		}
		return false;
	}

	
	private boolean unsubscribe(byte[] data, SocketAddress subscriberAddress) {
		boolean unsubscribed = false;
		String topicName = getMessage(data);
		if (subscriberMap.containsKey(topicName)) {
			/*ArrayList<*/InetSocketAddress/*>*/ subscribers = subscriberMap.get(topicName);
			if (!(subscribers==null)/*.isEmpty()*/) {
				/*for (int i = 0; i < subscribers.size(); i++) {*/
					if (subscribers.equals(socket1)){
					System.out.println ("You are not Subscriberd to the room "+topicName);
				    unsubscribed=false;
					}
					else{
					//	System.out.println(subscribers);
					sendBRK(data);
					subscriberMap.put(topicName,socket1);
	//			System.out.println("237"   +subscriberMap);
//						System.out.println("daaaaaaaaaaaaaaaaaaaam " +subscriberMap.get(topicName));
						System.out.println("A subscriber unsubscribed from " + topicName + ".");
						
						unsubscribed = true;
					}
				}
			//}
		/*	subscriberMap.remove(topicName);
			subscriberMap.put(topicName, subscribers);*/
		}
		return unsubscribed;
	}

	
	private void sendMessage(String message, SocketAddress socketAddress) throws SocketException {
		InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
		DatagramPacket packet = createPackets(Mes, 0, message, inetSocketAddress)[0];
		try {
			socket.send(packet);
			System.out.println("Broker sent a message: " + message);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Broker failed to send a message: " + message);
		}
	}

	/* Start function for the Broker. The Broker never initialises contact unless contacted by
	 * another node first, so just waits.
	 */
	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		while (true) {
			this.wait();
		}
	}


	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			this.notify();
			byte[] data = packet.getData();
			switch (getType(data)) {
			case NEW:
		System.out.println("Request recieved to create a topic.");
			sendAck(packet);
				if (!createTopic(data)) {
					sendMessage("This is already a topic.", packet.getSocketAddress());
				} else {
					sendMessage("Topic creation successful.", packet.getSocketAddress());
				}
				break;
			case Pub:
				System.out.println("Request recieved to publish a message.");
				sendAck(packet);
				if (!publish(data)) {
					sendMessage("This Room does not exist on your database.", packet.getSocketAddress());
				} else {
					sendMessage("Publication successful.", packet.getSocketAddress());
				}
				break;
			case SUB:
			System.out.println("Request recieved to subscribe to a topic.");
				sendAck(packet);
				if (!subscribe(data, packet.getSocketAddress())) {
					sendMessage("This Room does not exist on your database.", packet.getSocketAddress());
				} else {
//				System.out.println("789 "+ packet.getSocketAddress());
					sendMessage("Subscription successful.", packet.getSocketAddress());
				}
				break;
			case USUB:
				System.out.println("Request recieved to unsubscribe from a topic.");
				sendAck(packet);
				
				if (!unsubscribe(data, packet.getSocketAddress())) {
					sendMessage("This Room does not exist on your database.", packet.getSocketAddress());
				
				} else {
					sendMessage("Unsubscription successful.", packet.getSocketAddress());
					
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
