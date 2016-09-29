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
		Message m;
		
        String[] split = s.split("[~]");
        if(split[0] != null){
        	messageText = split[0];
        }
        if(split.length > 1 && split[1] != null){
        	messageType = split[1];
        }
        if(split.length > 2 && split[2] != null){
        	pid = Integer.parseInt(split[2]);
        }
        if(split.length > 3 && split[3] != null){
            hopCount = Integer.parseInt(split[3]);
        }
        if(split.length > 4 && split[4] != null){
        	direction = split[4];
        }
	}
	public String toString(){
		String line = "";
		line += messageText + "~" + messageType + "~" + pid + "~" + hopCount + "~" + direction;
		return line;
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
    public volatile String message = "";
	public  void putMessage(String s){
		 message = s; 
	}
	public  String getMessage(){
		return message;
	}
	public boolean hasMessage(){
		return (message.equals("")) ? false : true;
	}
	public  void flush(){
		message = "";
	}
}


class Process implements Runnable {
    public static volatile int counter = 0;
    public static volatile int count;
	public volatile static ArrayList<Channel> fromMaster = new ArrayList<Channel>();
	public volatile static ArrayList<Channel> toMaster = new ArrayList<Channel>();
	public volatile static ArrayList<ArrayList<Channel>> processChannels = new ArrayList<ArrayList<Channel>>();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (type.equals("master")) {// master starts the rounds
			while (true) {
				// initiate rounds
				int ra = r + 1;
			//	System.out.println("Starting round" + ra);
				for (int i = 0; i < n; i++) {
					    Message m = new Message("START");
						fromMaster.get(i).putMessage(m.toString() + "~" + ra);		
			            
//					System.out.println("master> Sending message to slave " + i);
				}
				while (!isRoundDone()) {
					
				}
				
				r++;
				
			//	System.out.println("done with round! " + counter);
			}
		} else {// Participant in HS Algorithm
			while (true) {
				Channel master = fromMaster.get(index);
				int ra = r + 1;
			//	System.out.println(index + ">Waiting to enter round " + (ra));
				while(!master.hasMessage()){
					
				}
				String text = master.getMessage();
			//	System.out.println(text + "round: " + ra);
				master.flush(); 
				Message message = new Message(text);
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
	public boolean isLeader = true;
	public static boolean leaderElected = false;
	public static int currentRound = 0;
	public int currentPhase = 0;
    public int phase = -1;
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
           // System.out.println(id + ">STUCK");
        }
        
        String sLeft = channelLeft.getMessage();
    	Message left = new Message(sLeft);
    	channelLeft.flush();
    	int ra = r + 1;
    	counter++;
    	boolean bCapturedLeft = false;
    	boolean bSendingLeft = false;
    	boolean bSendingRight = false;
        //boolean bPossiblyLeader = false;    
    	if(left.getMessageType().equals("OUT")){
    		
    		if(left.getPID() == id){
    			System.out.println(id + "> I am the leader!");
    			System.exit(0);
    		}
    		   
    		    if(left.getPID() >= currentMaxPID){
    		    	   currentMaxPID = left.getPID(); 
    		    	   isLeader = false;
    		    
    		    
    		     if(left.getHopCount() - 1 == 0){
    		    	 //Turn it around
    		    	 Message m = new Message("HS","IN",currentMaxPID,-1,"LEFT");
    		    	 sendQueue.add(m);
    		    	 bSendingLeft = true;
    		     }
    		     else{
    		    	 Message m = new Message("HS","OUT",currentMaxPID,left.getHopCount()-1,"RIGHT");
    		    	 sendQueue.add(m);
    		    	 bSendingRight = true;
    		     }
    		    }
    	  
    	}
    	
    	
    	if(left.getMessageType().equals("IN")){
    		
    		  if(left.getPID() == id){
    			 bCapturedLeft = true;
    		  }
    		  else{
    			  Message r = new Message("HS","IN",currentMaxPID,-1,"RIGHT");
    			  sendQueue.add(r);
    			  bSendingRight = true;
    		  }
    	}
        	
        
    	String sRight = channelRight.getMessage();
    	Message right = new Message(sRight);
    	channelRight.flush();
    	counter++;
    	if(right.getMessageType().equals("OUT")){
    		if(right.getPID() == id){
    			System.out.println(id + "> I am the leader!");
    			System.exit(0);
    		}
		    if(right.getPID() >= currentMaxPID){
		    	   currentMaxPID = right.getPID(); 
		    	   isLeader = false;
		    
		    
		     if(right.getHopCount() - 1 == 0){
		    	 //Turn it around
		    	 Message m = new Message("HS","IN",currentMaxPID,-1,"RIGHT");
		    	 sendQueue.add(m);
		    	 bSendingRight = true;
		     }
		     else{
		    	 Message m = new Message("HS","OUT",currentMaxPID,right.getHopCount()-1,"LEFT");
		    	 sendQueue.add(m);
		    	 bSendingLeft = true;
		     }
		    }
	  
	}
    	if(right.getMessageType().equals("IN")){
    		
  		  if(right.getPID() == id && bCapturedLeft){
  			  phase++;
  			  Message l = new Message("HS","OUT",id,(int)Math.pow(2, phase),"LEFT");
  			  Message r = new Message("HS","OUT",id,(int)Math.pow(2, phase),"RIGHT");
  			  //System.out.println(id + ">Entering phase " + phase);
  			  sendQueue.add(l);
  			  sendQueue.add(r);
  			  bSendingLeft = true;
  			  bSendingRight = true;
  		  }
  		  else{
  			  Message r = new Message("HS","IN",currentMaxPID,-1,"LEFT");
  			  bSendingLeft = true;
  			  sendQueue.add(r);
  		  }
  	}
        	
    	
        
        
   		 Message dummyLeft = new Message("DUMMY","DUMMY",-1,-1,"LEFT");
   		 Message dummyRight = new Message("DUMMY","DUMMY",-1,-1,"RIGHT");
   		 if(!bSendingLeft){
   		 sendQueue.add(dummyLeft);
   		 }
   		 if(!bSendingRight){
   		 sendQueue.add(dummyRight);
   		 }
   	
    	
    
    }
	public void round() {
	
		if(phase == -1){
			  phase++;
			  //System.out.println(id  +">Enter phase 0");
        	  Message left = new Message("HS","OUT",currentMaxPID,(int)Math.pow(2, phase),"LEFT");
        	  Message right = new Message("HS","OUT",currentMaxPID,(int)Math.pow(2, phase),"RIGHT");
        	  sendQueue.add(left);
        	  sendQueue.add(right);
        	  
        }		
       
		sendMessages();
		
		receiveMessages();
		
		Channel master = toMaster.get(index);
		while(master.hasMessage()){
			
		}
		Message m = new Message("DONE");
		int ra = r + 1;
     	master.putMessage(m.toString() + "~" + ra ) ;
	}

	public boolean isRoundDone() {
       Process.count = 0;
		for (int i = 0; i < n; i++) {
			Channel c  = toMaster.get(i);
			String sText  = c.getMessage(); 
		    Message message = new Message(sText);
		  
			if (!message.getMessageText().equals("DONE")) {
				 return false; 
			}
			else{
				Process.count++;
			}

		}
		//System.out.println("Count: " + Process.count);
		for(int i = 0; i < n; i++){
			toMaster.get(i).flush();
		}
		return true;
	 
	}
	
    public static void logChannels(){
    	for(int i = 0; i < n; i++){
    		System.out.println(i +">Left: " + processChannels.get(i).get(0).message + ",Right: " + processChannels.get(i).get(1).message);
    	}
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
	          // System.out.println("STUCK123");
		  }
		  int ra = r + 1;
		  targetChannel.putMessage(m.toString() + "~" + ra + "~" + index);
	}

	public Process(int id, int index) {
		this.id = id;
		this.currentMaxPID = id;
		this.index = index;
	}

	public static void spawnThreads() {
		ids = new int[n];
		Random random = new Random();
		HashSet<Integer> set = new HashSet<>();
		ArrayList<Process> list = new ArrayList<Process>();
        int max = Integer.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			ids[i] = random.nextInt(Integer.MAX_VALUE);
			while (set.contains(ids[i])) {
				ids[i] = random.nextInt(Integer.MAX_VALUE);
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
		int n = Integer.parseInt(args[0]);
		int[] id = new int[n];
		Process.n = n;
		Process.spawnThreads();
		Process master = new Process(-1, -1);
		master.type = "master";
		Thread masterThread = new Thread(master);
	    masterThread.start();
		
		
	}

}
