import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args){
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
}
