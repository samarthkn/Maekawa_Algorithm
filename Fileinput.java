package firstTry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class Fileinput{

	private int numNodes, numReq, interReq, critSecTime;
	private HashMap<Integer, String> ipAddressList;
	private HashMap<Integer, Integer> portList;
	String inputFilePath = "//home//004//s//sx//sxk133830//firstTry//inputFile.txt";
	String ipAddressFilePath = "//home//004//s//sx//sxk133830//firstTry//ipAddresses.txt";
	String portFilePath = "//home//004//s//sx//sxk133830//firstTry//ports.txt";
	
	public Fileinput() throws IOException, FileNotFoundException{
		String sCurrentLine;
		
		ipAddressList = new HashMap<Integer, String>(); 
		BufferedReader br1 = new BufferedReader(new FileReader(ipAddressFilePath));
		while ((sCurrentLine = br1.readLine()) != null) {
			String words[] = sCurrentLine.split(" ");
			getIpAddressList().put(Integer.parseInt(words[0]), words[1]);
		}
		br1.close();
		
		portList = new HashMap<Integer, Integer>();
		BufferedReader br2 = new BufferedReader(new FileReader(portFilePath)); //./secondTry/
		while ((sCurrentLine = br2.readLine()) != null) {
			String words[] = sCurrentLine.split(" ");
			getPortList().put(Integer.parseInt(words[0]), Integer.parseInt(words[1]));
		}
		br2.close();
		
		
	}
	//Constructor ends here
	
	//Function to scan the input file and populate the quorum list for this node
	public void scanInputFile(int myNodeId, Quorumclients quorum) throws NumberFormatException, IOException{
		String sCurrentLine;
		int count = 0;
		
		BufferedReader br1 = new BufferedReader(new FileReader(inputFilePath));
		
		while ((sCurrentLine = br1.readLine()) != null) {
			String words[] = sCurrentLine.split("=");
			
			if(count<4){
				if(words[0].equals("N")){
					setNumNodes(Integer.parseInt(words[1]));
					System.out.println("No. of nodes: " + getNumNodes());
				}
				else if(words[0].equals("M")){
					setNumReq(Integer.parseInt(words[1]));
					System.out.println("No. of requests: " + getNumReq());
				}
				else if(words[0].equals("IA")){
					setInterReq(Integer.parseInt(words[1]));
					System.out.println("Inter Request Time: " + getInterReq());
				}				
				else if(words[0].equals("CST")){
					setCritSecTime(Integer.parseInt(words[1]));
					System.out.println("Critical Section Time: " + getCritSecTime());
				}
			}
			else{
				if(words[0].contains(myNodeId + "")){
					String quorums[] = words[1].split(",");
					
					for(int i=0 ; i<quorums.length ; i++)
						quorum.addQuorum(Integer.parseInt(quorums[i]));
				}
			}
			
			count++;
		}
		br1.close();
		
		System.out.println("\nList of nodes in " + myNodeId + "'s quorum: ");
		quorum.printQuorumList();
		System.out.println("\nList of pending tokens in " + myNodeId + "'s quorum: ");
		quorum.printPermissionsPendingList();
	}
	
	//Function to scan the input file and populate the quorum list for this node
	public void scanInputFile(int myNodeId) throws NumberFormatException, IOException{
		String sCurrentLine;
		int count = 0;
		
		BufferedReader br1 = new BufferedReader(new FileReader(inputFilePath));
		
		while ((sCurrentLine = br1.readLine()) != null) {
			String words[] = sCurrentLine.split("=");
			
			if(count<4){
				if(words[0].equals("N")){
					setNumNodes(Integer.parseInt(words[1]));
					System.out.println("No. of nodes: " + getNumNodes());
				}
				else if(words[0].equals("M")){
					setNumReq(Integer.parseInt(words[1]));
					System.out.println("No. of requests: " + getNumReq());
			}
				else if(words[0].equals("IA")){
						setInterReq(Integer.parseInt(words[1]));
				}				
				else if(words[0].equals("CST")){
				setCritSecTime(Integer.parseInt(words[1]));
					System.out.println("Critical Section Time: " + getCritSecTime());
				}
			}
			
			count++;
		}
		br1.close();
		
	}
	
	public int getPortNo(int nodeId){
		int portNo = portList.get(nodeId);
		return portNo;
	}
	
	
	
	

	//Generated getters and setters
	public int getNumNodes() {
		return numNodes;
	}

	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}

	public int getNumReq() {
		return numReq;
	}

	public void setNumReq(int numReq) {
		this.numReq = numReq;
	}

	public int getInterReq() {
		return interReq;
	}

	public void setInterReq(int interReq) {
		this.interReq = interReq;
	}

	public int getCritSecTime() {
		return critSecTime;
	}

	public void setCritSecTime(int critSecTime) {
		this.critSecTime = critSecTime;
	}

	public HashMap<Integer, String> getIpAddressList() {
		return ipAddressList;
	}

	public void setIpAddressList(HashMap<Integer, String> ipAddressList) {
		this.ipAddressList = ipAddressList;
	}

	
	
	public HashMap<Integer, Integer> getPortList() {
		return portList;
	}

	public void setPortList(HashMap<Integer, Integer> portList) {
		this.portList = portList;
	}
	
}
