//package DA3;



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


	private static final String CREATE = "CRE";
	private static final String PUBLISH = "PUB";
    Scanner sc = new Scanner(System.in);
	
	InetSocketAddress dstAddress;

	private Map<Integer, String> RoomNumbers;

	
	Publisher() {
		try {
			
			dstAddress = new InetSocketAddress(PUB_DST, BKR_PORT);
			socket = new DatagramSocket(PUB_PORT);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		RoomNumbers = new HashMap<Integer, String>();
		String[] rooms={"1","2","3"};
		for(int i=0; i < rooms.length;i++) {
			RoomNumbers.put(i, rooms[i]);
	//		System.out.println("Room " + RoomNumbers.get(i) + " was created.");
		}
	}

	
	public static void main(String[] args) {
		try {

			new Publisher().start();
			System.out.println("Program completed");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	
	private void createRoom() throws SocketException {
		
		
	    System.out.println("Please enter a room to create: ");
	    String Room = sc.next(); 
	    System.out.println("Sending packet...");

		DatagramPacket[] packets = createPackets(NEW, RoomNumbers.size(), Room, dstAddress);
		RoomNumbers.put(RoomNumbers.size(), Room);
		try {
			socket.send(packets[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		  System.out.println("Packet sent");
	}
	
        public static String RandomTemp(){
        	String arr[]= new String [10];
        	int i=0;
            while(i<10) {   
        	Random rand = new Random();
                
                Integer random=rand.nextInt(90)+10; 
                arr[i]=random.toString();

                i=i+1;
                }
                Random r=new Random();        
              	int randomNumber=r.nextInt(arr.length);

              	return arr[randomNumber];

        }

	public static String data()  
    {        
    	return "the Temp is "+RandomTemp()+" degree ";
    }


	private boolean publishMessage() throws SocketException {
		
		System.out.println("Please enter the name of the room you want to publish the details for: ");
		String Room = sc.next(); 
		//String message=Room;
	
		String message=data();
	
		int RoomNumber = Integer.MAX_VALUE;
		for (int i = 0; i < RoomNumbers.size(); i++) {
			if ((RoomNumbers.get(i)).equals(Room)) {
				RoomNumber = i;
			}
		}
		if (RoomNumber == Integer.MAX_VALUE) {
			System.out.println("This Room does not exist.");
		} else {
			DatagramPacket[] packets = createPackets(Pub, RoomNumber, message, dstAddress);
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
		//	System.out.println("\b\b\b\b\b\b\b\b\b"); 
			System.out.println("Enter CREATE to create a new Room or PUBLISH to publish a new message: ");
			String startingString =sc.next();
			//System.out.println("Enter CREATE to create a new Room or \nPUBLISH to publish a new message: " + startingString);
			if (startingString.toUpperCase().contains(CREATE)) {
				createRoom();
	     	    this.wait(); // wait for ACK
			  //   this.wait(); // wait for MESSAGE
			} else if (startingString.toUpperCase().contains(PUBLISH)) {
				if (publishMessage()) {
				this.wait(); // wait for ACK
					//this.wait(); // wait for MESSAGE
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
