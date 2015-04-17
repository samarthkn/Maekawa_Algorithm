package firstTry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public class Requests {

	private Queue<String> msgQueue;
	boolean empty;
	
	public Requests(){
		msgQueue = new LinkedList<String>();
		empty = true;
	}
	
	public void printQueue(){
		Iterator<String> it = msgQueue.iterator();
		System.out.println("\nContents of the msgQueue:");
		while(it.hasNext())
			System.out.println(it.next());
	}
	
	public synchronized String take(){
		if(empty)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		System.out.println("Returning head of the queue: " + getMsgQueue().peek());
		empty = false;
		notify();
		return getMsgQueue().poll();
	}
	
	public synchronized void put(String msg){
		
		System.out.println("Msg to add: " + msg);
		getMsgQueue().add(msg);
		empty = false;
		notify();
	}
	
	public boolean isEmpty(){
		return getMsgQueue().isEmpty();
	}
	//Generated getters and setters
	public Queue<String> getMsgQueue() {
		return msgQueue;
	}

	public void setMsgQueue(Queue<String> msgQueue) {
		this.msgQueue = msgQueue;
	}
}
