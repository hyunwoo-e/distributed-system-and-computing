import java.io.*;
import java.net.*;

public class Client extends Thread{
	
	public Client()
	{
		
	}
	
	public void run() {

		String text = "abcabaabababc abcabaababcabcabaababc baabcabaababcbc abcabaababc";
		String pattern = "abcabaababc";
		
		try {
			Socket socket = new Socket("192.168.204.103", 10002);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			dos.writeUTF("REQUEST");
			dos.writeUTF(socket.getLocalAddress().toString().replaceAll("/", ""));
			dos.writeUTF(text);
			dos.writeUTF(pattern);
			
			dos.close();
			socket.close();
		} catch (IOException e) {
		
		}
	}
	
	public static void main(String[] args){		
		
		Client client = new Client();
		client.start();
		
		try {
			ServerSocket serverSocket = new ServerSocket(10002);
			Socket socket = serverSocket.accept();
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			System.out.print("Index: ");
			while(dis.available() > 0) {
				System.out.print(dis.readInt() + " ");
			}
			
			dis.close();
			socket.close();
			serverSocket.close();		
		} catch (IOException e) {

		}
	}
}
