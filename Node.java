	//package DA3;
	//package dA2;
	
	import java.io.IOException;
	import java.net.DatagramPacket;
	import java.net.DatagramSocket;
	import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
	import java.nio.ByteBuffer;
	import java.util.Random;
	import java.util.concurrent.CountDownLatch;
	
	public abstract class Node  {
		static final int PACKETSIZE =500 ;
		static final byte ACK = 0;
		static final byte BRK = 2;
		static final byte NEW = 4;
		static final byte Pub = 5;
		static final byte SUB = 6;
		static final byte USUB = 7;
		static final byte Mes = 8;
		static final String PUB_DST = "172.20.0.0";
		static final String SUB_DST="172.30.0.0";
		static final int PUB_PORT = 50000;
		static final int BKR_PORT = 50001;
	
	
		DatagramSocket socket;
		Listener listener;
		CountDownLatch latch;
	
		Node() {
			latch = new CountDownLatch(1);
			listener = new Listener();
			listener.setDaemon(true);
			listener.start();
		}
		private byte[] createPacketData(int type, int sequenceNumber, int RoomNumber, byte[] message) {
			byte[] data = new byte[PACKETSIZE];
	
			data[0] = (byte) type;
	//		System.out.println("sequence number"+sequenceNumber);
			data[1] = (byte) sequenceNumber;
			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
			byteBuffer.putInt(RoomNumber);
		//	System.out.println("top"+RoomNumber);
			 byteBuffer.rewind();
			byte[] RoomNumberArray =byteBuffer.array();
			for (int i = 0; i < 4; i++) {
				data[i + 2] = RoomNumberArray[i];
//   System.out.println("this " + RoomNumberArray[i] );
			}
			for (int i = 0; i < message.length && i < PACKETSIZE; i++) {
				data[i + 6] = message[i];
			}
			return data;
		}
		
		protected DatagramPacket[] createPackets(int type, int RoomNumber, String message, InetSocketAddress dstAddress) throws SocketException {
			int messageSize = PACKETSIZE - 6;
			byte[] tmpArray = message.getBytes();
			byte[] messageArray = new byte[tmpArray.length];
		
			for (int i = 0; i < tmpArray.length; i++) {
				messageArray[i] = tmpArray[i];
			}
			int numberOfPackets = 0;
			for (int messageLength = messageArray.length; messageLength > 0; messageLength -= messageSize) {
				numberOfPackets++;
			}
			DatagramPacket[] packets = new DatagramPacket[numberOfPackets];
			int offset = 0;
			for (int sequenceNumber = 0; sequenceNumber < numberOfPackets; sequenceNumber++) {
				byte[] dividedMessage = new byte[messageSize];
				for (int j = offset; j < offset + messageArray.length; j++) {
					dividedMessage[j] = messageArray[j + offset];
				}
				byte[] data = createPacketData(type, sequenceNumber, RoomNumber, dividedMessage);
			DatagramPacket packet = new DatagramPacket(data, data.length, dstAddress);
		
				packets[sequenceNumber] = packet;
				offset += messageSize;
			}

			return packets;
		}
		protected int getType(byte[] data) {
			return data[0];
		}
	
		protected int getSequenceNumber(byte[] data) {
			return data[1];
		}
	
		protected int getRoomNumber(byte[] data) {
			byte[] intArray = new byte[4];
			for (int i = 0; i < intArray.length; i++) {
				intArray[i] = data[i + 2];
//				System.out.println("Room number"+intArray[i]);
			}
			return ByteBuffer.wrap(intArray).getInt();
		}
	
		protected String getMessage(byte[] data) {
			byte[] messageArray = new byte[data.length - 6];
			for (int i = 0; i < messageArray.length && data[i + 6] != 0; i++) {
				messageArray[i] = data[i + 6];
			}
			String message = new String(messageArray).trim();
			return message;
		}
	
		protected void setType(byte[] data, byte type) {
			data[0] = type;
		}
	
		protected void sendAck(DatagramPacket receivedPacket) {
			byte[] data = receivedPacket.getData();
			setType(data, ACK);
			//System.out.println("79 "+ receivedPacket.getSocketAddress());
			DatagramPacket ack = new DatagramPacket(data, data.length, receivedPacket.getSocketAddress());
			try {
				socket.send(ack);
				System.out.println("Sent ACK.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		  
		protected void sendBRK(byte[] data1) {
			String data = getMessage(data1);
			byte[] data2=data.getBytes();
			setType(data2, BRK);
		//	System.out.println("79 "+ data);
			InetSocketAddress inet=new InetSocketAddress(SUB_DST,BKR_PORT+Integer.parseInt(data));
			DatagramPacket ack = new DatagramPacket(data2, data2.length,inet );
			try {
				socket.send(ack);
				System.out.println("Sent BKR.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		  
	
		
		
		public abstract void onReceipt(DatagramPacket packet);
	
	
		class Listener extends Thread {

			
			public void go() {                	// Starts Listening on the port of the class where the function go was called and waits
				latch.countDown();
			}
	
			public void run() {
				try {
					latch.await();
					while (true) {
						DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);    //Creates a Packet of Packet Size which is 500 for this code
						socket.receive(packet);
	
	 					onReceipt(packet);       // calls the abstract Class onReceipt which is defined in every sub class
					}
				} catch (Exception e) {
					if (!(e instanceof SocketException))
						e.printStackTrace();
				}
			}
		}
	}