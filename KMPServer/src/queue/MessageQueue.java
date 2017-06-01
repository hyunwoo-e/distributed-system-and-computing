package queue;

import java.util.LinkedList;

public class MessageQueue {
	private static LinkedList<Message> list = new LinkedList<Message>();
	
	public static synchronized void enqueue(Message msg) {
		list.addLast(msg);
	}
	
	public static synchronized Message dequeue() {
		if(list.isEmpty())
			return null;
		
		return list.removeFirst();
	}
	
	public static synchronized boolean isEmpty() {
		return list.isEmpty();
	}
	
	public static synchronized int size() {
		return list.size();
	}
}