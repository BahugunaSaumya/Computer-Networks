//package DA3;
//package dA2;

/** Publisher class for custom Publish-Subscribe protocol. Takes user input and creates
 * topics, which it can then publish messages for. Interacts with the Broker who stores
 * what topics exist as well as the list of subscribers to each topic. @author: Jack Gilbride
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Publisher extends Node {

	/** Constant substrings to recognize user input. */
	private static final String CREATE = "CRE";
	private static final String PUBLISH = "PUB";
    Scanner sc = new Scanner(System.in);
	//Terminal terminal; 
	InetSocketAddress dstAddress;
	/** Map topic numbers to topic names, map agreed with Broker. */
	private Map<Integer, String> topicNumbers;

	
	Publisher() {
		try {
			//this.terminal = terminal;
			dstAddress = new InetSocketAddress(PUB_DST, BKR_PORT);
			socket = new DatagramSocket(PUB_PORT);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		topicNumbers = new HashMap<Integer, String>();
		String[] rooms={"1","2","3"};
		for(int i=0; i < rooms.length;i++) {
			topicNumbers.put(i, rooms[i]);
	//		System.out.println("Topic " + topicNumbers.get(i) + " was created.");
		}
	}

	
	public static void main(String[] args) {
		try {
		//	Terminal terminal = new Terminal("Publisher");
			new Publisher().start();
			System.out.println("Program completed");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	
	private void createTopic() throws SocketException {
		
		
	    System.out.println("Please enter a room to create: ");
	    String topic = sc.next(); 
	    System.out.println("Please enter a room to create: " + topic);
	    System.out.println("Sending packet...");

		DatagramPacket[] packets = createPackets(NEW, topicNumbers.size(), topic, dstAddress);
		topicNumbers.put(topicNumbers.size(), topic);
		try {
			socket.send(packets[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		  System.out.println("Packet sent");
	}
	
        public static String RandomNumbers(){
        	String arr[]= new String [10];
        	int i=0;
            while(i<10) {   
        	Random rand = new Random();
                  //10 random numbers
                Integer random=rand.nextInt(90)+10; //generates random no. between 10 and 100
                arr[i]=random.toString();
  //              System.out.println(arr[i]);
                i=i+1;
                }
                Random r=new Random();        
              	int randomNumber=r.nextInt(arr.length);
//              	System.out.println(arr[randomNumber]);
              	return arr[randomNumber];

        }

	public static String data()  
    {        
      //	String[] arr={"20", "26", "30", "17", "23"};
      	//Random r=new Random();        
      	//int randomNumber=r.nextInt(arr.length);
		//System.out.println("1234"+RandomNumbers());
    	return "the Temp is "+RandomNumbers()+" degree ";
    }


	private boolean publishMessage() throws SocketException {
		
		System.out.println("Please enter the name of the room you want to publish the details for: ");
		String topic = sc.next(); 
		//String message=topic;
	//	terminal.println("Please enter the name of the topic you want to publish a message for: " + topic);
	//	String message = terminal.read("Please enter the message that you would like to publish: ");
		String message=data();
	//	terminal.println("Please enter the message that you would like to publish: " + message);
		int topicNumber = Integer.MAX_VALUE;
		for (int i = 0; i < topicNumbers.size(); i++) {
			if ((topicNumbers.get(i)).equals(topic)) {
				topicNumber = i;
			}
		}
		if (topicNumber == Integer.MAX_VALUE) {
			System.out.println("This topic does not exist.");
		} else {
			DatagramPacket[] packets = createPackets(Pub, topicNumber, message, dstAddress);
			try {
				System.out.println("Sending packet...");
				socket.send(packets[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Packet sent");
			return true;
		}
		return false;
	}


	public synchronized void start() throws Exception {
		while (true) {
		
			System.out.println("Enter CREATE to create a new topic or PUBLISH to publish a new message: ");
			String startingString =sc.next();
			System.out.println(
					"Enter CREATE to create a new topic or \nPUBLISH to publish a new message: " + startingString);
			if (startingString.toUpperCase().contains(CREATE)) {
				createTopic();
	     	    this.wait(); // wait for ACK
		//		this.wait(); // wait for MESSAGE
			} else if (startingString.toUpperCase().contains(PUBLISH)) {
				if (publishMessage()) {
				this.wait(); // wait for ACK
			//		this.wait(); // wait for MESSAGE
				}
			} else {
				System.out.println("Invalid input.");
			}
		}
	}


	@Override
	public synchronized void onReceipt(DatagramPacket packet) {
		this.notify();
		byte[] data = packet.getData();
		if (getType(data) == ACK) {
			System.out.println("ACK received for Sequence Number " + getSequenceNumber(data));
		} else if (getType(data) == Mes) {
			System.out.println("Message received: " + getMessage(data));
			sendAck(packet);
		}
	}

}
