package firstTry;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ExecuteCS implements Runnable{
	int myNodeId, myPortNo, cst;
	Requests requestQueue;
	Fileinput file;
	Thread t;
	HashMap<Integer, Socket> csNodeSockets;
	SimpleDateFormat format;
	File result;
	FileWriter fw;
	BufferedWriter bw=null;
	String writeToFile, currentTime;
	int reqNo;
	
	public ExecuteCS(int myNodeId, int myPortNo, Requests msgQueue, Fileinput file, HashMap<Integer, Socket> csNodeSockets){
		this.myNodeId = myNodeId;
		this.myPortNo = myPortNo;
		this.requestQueue = msgQueue;
		this.file = file;
		
		this.csNodeSockets = csNodeSockets;
		format = new SimpleDateFormat("HH:mm:ss");
		
		String portFilePath = "//home//004//s//sx//sxk133830//firstTry//outputfile.txt";
		result = new File(portFilePath);
		if (!result.exists())
			try {
				result.createNewFile();
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		try {
			fw = new FileWriter(result.getAbsoluteFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
		
		reqNo = file.getNumNodes() * file.getNumReq();
		
		//Thread creation
		t = new Thread(this, "ReadCSNodeThread");
		t.start();
	}

	@Override
	public void run() {
		// Read messages from the msgQueue and do the critical section execution until it is empty
		reqNo = file.getNumReq() * file.getNumNodes();
		System.out.println("Total requests expected: "+reqNo);
		
		while(true){
			if(!requestQueue.isEmpty()){
				Socket client = null;
			    DataOutputStream outToClient;
			    String msgToSend;
			    
				String headOfQueue = requestQueue.take();
				System.out.println("\n\n"+t.getName()+"\tReceived head of the queue: " + headOfQueue);
				
				//String format "MSG nodeId"
				String words[] = headOfQueue.split(" ");
				int nodeId = Integer.parseInt(words[0]);
				int reqId = Integer.parseInt(words[1]);
				int cst = Integer.parseInt(words[2]);
				
				currentTime = format.format(new Date());
				System.out.println(t.getName()+"\tNode "+nodeId+" enters the CS at "+ currentTime);
				
				try {
					writeToFile = "<" + nodeId + ", " + reqId + ", " + getHostName() + ">";
				} catch (UnknownHostException e2) {
					e2.printStackTrace();
				}
				
				try {
					bw.write(writeToFile+"\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					Thread.sleep(cst);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//currentTime = format.format(new Date());
				//System.out.println(t.getName()+"\tNode "+nodeId+" exits the CS at "+ currentTime);
				
				try {
					writeToFile = "<" + nodeId + ", " + reqId + ", " + getHostName() + ">";
				} catch (UnknownHostException e2) {
					e2.printStackTrace();
				}
				try {
					bw.write(writeToFile+"\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//Sending ACK to the node the requested CS
				String destIpAdd = file.getIpAddressList().get(nodeId);
				int destPort = file.getPortList().get(nodeId);
				
				if(csNodeSockets.containsKey(nodeId))
					client = csNodeSockets.get(nodeId);
				else{
					try {
						client = new Socket(destIpAdd, destPort);
						csNodeSockets.put(nodeId, client);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					outToClient = new DataOutputStream(client.getOutputStream());
					
					msgToSend = "ACK "+nodeId;
					outToClient.writeBytes(msgToSend+"\n");
					System.out.println(t.getName()+"\tMsg sent: " + msgToSend);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Checking if all CS requests have been processed
				reqNo--;
				System.out.println(t.getName() + "\tNO. OF CS LEFT: " + reqNo);
				
				if(reqNo == 0){
					try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println(t.getName() + "\t =============== FINISHED EXECUTION ===============");
					break;
				}
				
			}else{
				//Do nothing
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public String getHostName() throws UnknownHostException{
		String hostname;
		hostname = java.net.InetAddress.getLocalHost().getHostName();
		return hostname;
	}
}