//package DA3;
//package dA2;



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

	
	private Hashtable<String, /*ArrayList<*/InetSocketAddress/*>*/> subscriberList;

	private Hashtable<Integer, String> Room;
	Iterator<String> iterators;
	/*	ArrayList<*/InetSocketAddress/*>*/ socket1= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
	
	    Broker() {

		try {
			socket = new DatagramSocket(BKR_PORT);
			listener.go();
		} catch (

		java.lang.Exception e) {
			e.printStackTrace();
		}
		subscriberList = new Hashtable<String, /*ArrayList<*/InetSocketAddress/*>*/>();
		Room = new Hashtable<Integer, String>();
	      
		String[] rooms={"1","2","3"};
		/*	ArrayList<*/InetSocketAddress/*>*/ socketNumbers= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
		for(int i=0; i < rooms.length;i++) {
			Room.put(i, rooms[i]);
			subscriberList.put(rooms[i], socketNumbers);
	//		System.out.println("Room " + RoomNumbers.get(i) + " was created.");
		}
			
	}

	public static void main(String[] args) {
		try {

			new Broker().start();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	private boolean createRoom(byte[] data) {
	/*	ArrayList<*/InetSocketAddress/*>*/ socketNumbers= new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT);
	
		
		String RoomName = getMessage(data);
		if (!subscriberList.containsKey(RoomName)) {
			subscriberList.put(RoomName, socket1);
			int RoomNumber = getRoomNumber(data);
			Room.put(RoomNumber, RoomName);
//			System.out.println("Room " + RoomName + " was created.");
			return true;
		}
		return false;
	}


	private boolean publish(byte[] data) throws SocketException {
		int RoomNumber = getRoomNumber(data);
	//	System.out.println("getting "+RoomNumber);
		setType(data, Pub);
		if (Room.containsKey(RoomNumber)) {
			String RoomName = Room.get(RoomNumber);
			//System.out.println("name"+RoomName);
			InetSocketAddress dstAddresses = subscriberList.get(RoomName);
//			System.out.println("234   "+dstAddresses);
	//		System.out.println("235   "+socket1);
		//	System.out.println("236"   +subscriberList);
			if((dstAddresses.equals(new /*ArrayList<*/InetSocketAddress/*>*/(SUB_DST,BKR_PORT)))){
				System.out.println("No one has Subscribed to this Room");
				return false;
			}
			
			else if (!(dstAddresses==null)) {
					//	for (int i = 0; i < dstAddresses.size(); i++) {
							try {DatagramPacket publication = new DatagramPacket(data, data.length, dstAddresses/*.get(i)*/);
							
								socket.send(publication);
								System.out.println("Room " + RoomName + " was published.");
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
		String RoomName = getMessage(data);
		//System.out.println("ropa " +RoomName);
		if (subscriberList.containsKey(RoomName)) {
			/*ArrayList<*/InetSocketAddress/*>*/ subscribers = subscriberList.get(RoomName);
		//	System.out.println(subscriberList.get(RoomName));
		//	System.out.println(subscriberAddress);
			subscriberList.put(RoomName,(InetSocketAddress) subscriberAddress);
			//subscriberList.remove(RoomName);
			//subscriberList.put(RoomName, subscribers);
//			System.out.println(subscriberList.get(RoomName));
			System.out.println("A new subscriber subscribed to " + RoomName + ".");
			return true;
		}
		return false;
	}

	
	private boolean unsubscribe(byte[] data, SocketAddress subscriberAddress) {
		boolean unsubscribed = false;
		String RoomName = getMessage(data);
		if (subscriberList.containsKey(RoomName)) {
			/*ArrayList<*/InetSocketAddress/*>*/ subscribers = subscriberList.get(RoomName);
			if (!(subscribers==null)/*.isEmpty()*/) {
				/*for (int i = 0; i < subscribers.size(); i++) {*/
					if (subscribers.equals(socket1)){
					System.out.println ("You are not Subscriberd to the room "+RoomName);
				    unsubscribed=false;
					}
					else{
					//	System.out.println(subscribers);
					sendBRK(data);
					subscriberList.put(RoomName,socket1);
	//			System.out.println("237"   +subscriberList);
//						System.out.println("daaaaaaaaaaaaaaaaaaaam " +subscriberList.get(RoomName));
						System.out.println("A subscriber unsubscribed from " + RoomName + ".");
						
						unsubscribed = true;
					}
				}
			//}
		/*	subscriberList.remove(RoomName);
			subscriberList.put(RoomName, subscribers);*/
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
		System.out.println("Request recieved to create a Room.");
			sendAck(packet);
				if (!createRoom(data)) {
					sendMessage("This is already a Room.", packet.getSocketAddress());
				} else {
					sendMessage("Room creation successful.", packet.getSocketAddress());
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
			System.out.println("Request recieved to subscribe to a Room.");
				sendAck(packet);
				if (!subscribe(data, packet.getSocketAddress())) {
					sendMessage("This Room does not exist on your database.", packet.getSocketAddress());
				} else {
//				System.out.println("789 "+ packet.getSocketAddress());
					sendMessage("Subscription successful.", packet.getSocketAddress());
				}
				break;
			case USUB:
				System.out.println("Request recieved to unsubscribe from a Room.");
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
