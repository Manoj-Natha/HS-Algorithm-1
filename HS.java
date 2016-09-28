import java.util.ArrayList;
import java.util.HashSet;

import java.util.Random;

class Message{
	public volatile String messageText = "";
	public volatile String messageType = "";
	public volatile int pid;
	public volatile int hopCount;
	public volatile String direction = "";
	public  Message(String s, String t, int p, int h, String d){
		 messageText = s;
		 messageType = t;
		 pid = p;
		 hopCount = h;
		 direction = d;
	}
	public Message(String s){
		messageText = s;
	}
	public String getMessageText(){
		return messageText;
	}
	public String getMessageType(){
		return messageType;
	}
	public int getPID(){
		return pid;
	}
	public String getDirection(){
		return direction;
	}
	public int getHopCount(){
		return hopCount;
	}

}
class Channel{
    public volatile Message message = null;
	public void putMessage(Message s){
		 message = s;
	}
	public Message getMessage(){
		return message;
	}
	public boolean hasMessage(){
		return (message == null) ? false : true;
	}
	public void flush(){
		message = null;
	}
}


class Process implements Runnable {

	public volatile static ArrayList<Channel> fromMaster = new ArrayList<Channel>();
	public volatile static ArrayList<Channel> toMaster = new ArrayList<Channel>();
	public volatile static ArrayList<ArrayList<Channel>> processChannels = new ArrayList<ArrayList<Channel>>();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (type.equals("master")) {// master starts the rounds
			ids = new int[n];
			spawnThreads();
			while (true) {
				// initiate rounds
				int ra = r + 1;
				System.out.println("Starting round" + ra);
				for (int i = 0; i < n; i++) {
						fromMaster.get(i).putMessage(new Message("START"));		
			            
//					System.out.println("master> Sending message to slave " + i);
				}
				while (!isRoundDone()) {
					
				}
				r++;
				
				System.out.println("done with round!");
			}
		} else {// Participant in HS Algorithm
			while (true) {
				Channel master = fromMaster.get(index);
				int ra = r + 1;
				System.out.println(index + ">Waiting to enter round " + (ra));
				while(!master.hasMessage()){
				    
				}
				Message message = master.getMessage();
				master.flush(); 
				if (message.getMessageText().equals("START")) {
					// round
					round();
					r++;
				}
			}
		}

	}

	public static int n;
	public int index;
	public int currentMaxPID;
	public boolean isLeader = false;
	public static boolean leaderElected = false;
	public static int currentRound = 0;
	public int currentPhase = 0;
    public int phase = 0;
    public int r =0;
    public ArrayList<Message> sendQueue = new ArrayList<>();
    public void sendMessages(){
    	    
          	while(!sendQueue.isEmpty()){
          		 Message m =  sendQueue.remove(0);
          		 sendMessage(m);
          	}
    }
    public void receiveMessages(){
    	//check both left and right channels
        Channel channelLeft = processChannels.get(index).get(0);
        Channel channelRight = processChannels.get(index).get(1);
        while(!channelLeft.hasMessage() || !channelRight.hasMessage()){
        	
        }
        
    	Message left = channelLeft.getMessage();
    	Message right = channelRight.getMessage();
    	
    	
    	channelLeft.flush();
    	channelRight.flush();
    	if(left.getMessageType().equals("OUT")){
    		
    		if(left.getPID() == id){
    			System.out.println(id + "> I am the leader!");
    			System.exit(0);
    		}
    		   
    		    if(left.getPID() >= currentMaxPID){
    		    	   currentMaxPID = left.getPID(); 
    		    	   isLeader = false;
    		    }
    		    
    		     if(left.getHopCount() - 1 == 0){
    		    	 //Turn it around
    		    	 Message m = new Message("HS","IN",left.getPID(),-1,"RIGHT");
    		    	 sendQueue.add(m);
    		     }
    		     else{
    		    	 Message m = new Message("HS","IN",left.getPID(),left.getHopCount()-1,"LEFT");
    		    	 sendQueue.add(m);
    		     }
    	  
    	}
    	if(right.getMessageType().equals("OUT")){
    		if(right.getPID() == id){
    			System.out.println(id + "> I am the leader!");
    			System.exit(0);
    		}
		    if(right.getPID() >= currentMaxPID){
		    	   currentMaxPID = left.getPID(); 
		    	   isLeader = false;
		    }
		    
		     if(right.getHopCount() - 1 == 0){
		    	 //Turn it around
		    	 Message m = new Message("HS","IN",right.getPID(),-1,"LEFT");
		    	 sendQueue.add(m);
		     }
		     else{
		    	 Message m = new Message("HS","IN",right.getPID(),left.getHopCount()-1,"RIGHT");
		    	 sendQueue.add(m);
		     }
	  
	}
    	
    	if(left.getMessageType().equals("IN")){
    		
    		  if(left.getPID() == id){
    			  phase++;
    			  System.out.println(id + "> Entering new phase!");
    			  Message l = new Message("HS","OUT",id,(int)Math.pow(2, phase),"LEFT");
    			  Message r = new Message("HS","OUT",id,(int)Math.pow(2, phase),"RIGHT");
    			  sendQueue.add(l);
    			  sendQueue.add(r);
    		  }
    		  else{
    			  Message r = new Message("HS","IN",currentMaxPID,-1,"RIGHT");
    			  sendQueue.add(r);
    		  }
    	}
    	if(right.getMessageType().equals("IN")){
    		
  		  if(right.getPID() == id){
  			  phase++;
  			  Message l = new Message("HS","OUT",id,(int)Math.pow(2, phase),"LEFT");
  			  Message r = new Message("HS","OUT",id,(int)Math.pow(2, phase),"RIGHT");
  			  sendQueue.add(l);
  			  sendQueue.add(r);
  		  }
  		  else{
  			  Message r = new Message("HS","IN",currentMaxPID,-1,"LEFT");
  			  sendQueue.add(r);
  		  }
  	}
    	
    	if(sendQueue.isEmpty()){
    		 Message dummyLeft = new Message("DUMMY","DUMMY",-1,-1,"LEFT");
    		 Message dummyRight = new Message("DUMMY","DUMMY",-1,-1,"RIGHT");
    		 sendQueue.add(dummyLeft);
    		 sendQueue.add(dummyRight);
    	}
    	
    }
	public void round() {
	
		if(phase == 0){
        	  Message left = new Message("HS","out",currentMaxPID,(int)Math.pow(2, phase),"LEFT");
        	  Message right = new Message("HS","out",currentMaxPID,(int)Math.pow(2, phase),"RIGHT");
        	  sendQueue.add(left);
        	  sendQueue.add(right);
        	  
        }		
       
		sendMessages();
		
		receiveMessages();
		
		Channel master = toMaster.get(index);
		while(master.hasMessage()){
			
		}
     	master.putMessage(new Message("DONE")) ;
	}

	public boolean isRoundDone() {
       
		for (int i = 0; i < n; i++) {
			Message message = toMaster.get(i).getMessage(); 
			if (message == null || !message.getMessageText().equals("DONE")) {
				 return false; 
			}

		}
		for(int i = 0; i < n; i++){
			toMaster.get(i).flush();
		}
		return true;
	 
	}
	
       
	public void sendMessage(Message m){
		  String direction = m.getDirection();
		  int left;
		  if(index == 0){
			  left = n-1;
		  }
		  else{
			  left = index -1;
		  }
		  int neighbor = (direction.equals("LEFT")) ? left : (index + 1) % n;
		  int target = (direction.equals("LEFT")) ? 1 : 0;
		  Channel targetChannel =  processChannels.get(neighbor).get(target);
		  
		  while(targetChannel.hasMessage()){
			  
		  }
		  targetChannel.putMessage(m);
	}

	public Process(int id, int index) {
		this.id = id;
		this.index = index;
	}

	public void spawnThreads() {
		Random random = new Random();
		HashSet<Integer> set = new HashSet<>();
		ArrayList<Process> list = new ArrayList<Process>();
        int max = Integer.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			ids[i] = random.nextInt(10);
			while (set.contains(ids[i])) {
				ids[i] = random.nextInt(10);
			}
			if(max < ids[i]){
				max = ids[i];
			}
			Channel inboundMaster = new Channel();
			Channel outboundMaster = new Channel();
			Channel inboundLeft = new Channel();
			Channel inboundRight = new Channel();
			toMaster.add(inboundMaster);
			fromMaster.add(outboundMaster);
			ArrayList<Channel> processChannel = new ArrayList<Channel>();
			processChannel.add(inboundLeft);
			processChannel.add(inboundRight);
			processChannels.add(processChannel);
			set.add(ids[i]);
			Process p = new Process(ids[i], i);
			p.type = "slave";
			list.add(p);
			Thread t = new Thread(p);
			t.start();
		}
		
		System.out.println("Max Id: " + max);
	}

	public static int[] ids;
	public static String[] messagesFromMaster;
	public static volatile String[] messagesToMaster;
	public String type;
	public int id;
	public volatile boolean roundDone = false;

}

public class HS {

	public static void main(String args[]) {
		Message m = new Message("dssad");
		System.out.println(m);
		int n = Integer.parseInt(args[0]);
		int[] id = new int[n];
		Process.n = n;
		Process master = new Process(-1, -1);
		master.type = "master";
		Thread masterThread = new Thread(master);
		masterThread.start();

	}

}
