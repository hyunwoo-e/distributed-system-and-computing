import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable, ClientProxy{
	
	private final int broker_port = 10000;
	private final int server_port = 10003;
	private final int client_port = 10006;
	private final int sock_timeout = 2000;
	private boolean shouldStop;
	
	private ServerSocket serverSocket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private String server_address;
	
	String text = "";
	String pattern = "";
	
	public Client()
	{
		shouldStop = false;
		try {
			serverSocket = new ServerSocket(client_port);
		} catch (IOException e) {
			
		}
	}
	
	public void receive() {
		try {
			Socket socket = serverSocket.accept();
			dis = new DataInputStream(socket.getInputStream());
			
			String type = dis.readUTF();
			String flag = dis.readUTF();
			String addr = dis.readUTF();
			String data = dis.readUTF();
			
			Message msg = new Message(type, flag, addr, data);
			
			switch(msg.getType()) {
				case "locate_server":
					if(msg.getFlag().equals("ACK")) {
						server_address = msg.getData();
						System.out.println("Server: " + server_address);
					}
					break;
				case "response":
					System.out.println("Index: " + msg.getData());
					break;
			}

			dis.close();
			socket.close();
		} catch (IOException e) {

		}
	}
	
	public void locate_server() {		
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(borker_address, broker_port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF("locate_server");
			dos.writeUTF("");
			dos.writeUTF("");
			dos.writeUTF("");
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void request() {
		if(server_address.isEmpty()) {
			System.out.println("Server Not Founded");
			return;
		}
		
		try {			
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(server_address, server_port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF("request");
			dos.writeUTF(text);
			dos.writeUTF(socket.getLocalAddress().toString().replaceAll("/", ""));
			dos.writeUTF(pattern);
			
			dos.close();
			socket.close();
		} catch (IOException e) {
		
		}		
	}
	
	public void run() {		
		locate_server();

		while(!shouldStop) {
			receive();
		}

		try {
			serverSocket.close();
		} catch (IOException e) {

		}			
	}
	
	public static void main(String[] args){	
		Client client = new Client();
		Thread t = new Thread(client);
		t.start();
		
			Scanner keyboard = new Scanner(System.in);

			client.text = keyboard.nextLine();
			client.text.trim();
			client.pattern = keyboard.nextLine();
			client.pattern.trim();
			client.request();
			
			client.shouldStop=true;
			
		try {
			t.join();
		} catch (InterruptedException e1) {

		}		
	}
}
