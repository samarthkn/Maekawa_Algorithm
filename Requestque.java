package firstTry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Requestque {

	private PriorityQueue<String> queue;
	Comparator<String> comparator;
	boolean empty;
	
	public Requestque(){
		comparator = new PriorityComparator();
		queue = new PriorityQueue<String>(100, comparator);
		empty = true;
	}	
	
	public synchronized void printQueue(){
		Iterator<String> it = queue.iterator();
				while(it.hasNext())
			System.out.println("\t\t" + it.next());
		System.out.println("\n");
	}
	
	public synchronized String take(){
		if(empty)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		System.out.println("Returning head of the queue: " + getQueue().peek());
		empty = false;
		notify();
		return getQueue().poll();
	}
	
	public synchronized void put(String msg){
		
		//System.out.println("##Msg to add to the QUEUE: " + msg);
		getQueue().add(msg);
		empty = false;
		notify();
	}

	public synchronized boolean isEmpty(){
		return getQueue().isEmpty();
	}
	
	//Generated getters and setters
	public PriorityQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(PriorityQueue<String> queue) {
		this.queue = queue;
	}
}

class PriorityComparator implements Comparator<String>{

	@Override
	public int compare(String x, String y) {
		// Check the timestamp in the msg of the string and insert
		//Format of the string "REQ nodeId clockTime"
		String string1[] = x.split(" ");
		String string2[] = y.split(" ");
		int time1 = Integer.parseInt(string1[2]);
		int time2 = Integer.parseInt(string2[2]);
		
		if(time1 < time2){
			return -1;
		}
		else if(time1 > time2){
			return 1;
		}
		else if(time1 == time2){
			int procId1 = Integer.parseInt(string1[1]);
			int procId2 = Integer.parseInt(string2[1]);
			if(procId1 < procId2)
				return -1;
			else
				return 1;
		}
		else
			return 0;
	}
}