import java.util.ArrayList;

import java.util.HashSet;
import java.util.Random;

class Channel{
	public volatile String message = "";
	public void putMessage(String s){
		 message = s;
	}
	public String getMessage(){
		return message;
	}
}

class Process implements Runnable {

	public static ArrayList<Channel> fromMaster = new ArrayList<Channel>();
	public static ArrayList<Channel> toMaster = new ArrayList<Channel>();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (type.equals("master")) {// master starts the rounds
			ids = new int[n];
			spawnThreads();
			while (!leaderElected) {
				// initiate rounds
				
				for (int i = 0; i < n; i++) {
					fromMaster.get(i).putMessage("START");
					System.out.println("master> Sending message to slave " + i);
				}
				while (!isRoundDone()) {
					
					//System.out.println("master> Waiting for DONE messages from slaves!" );
				}
				leaderElected = true;
				System.out.println("Done with round");
			}
			System.exit(0);
		} else {// Participant in HS Algorithm
			while (true) {
				if (fromMaster.get(index).getMessage().equals("START")) {
					fromMaster.get(index).putMessage("");
					// round
					round();
				}
			}
		}

	}

	public static int n;

	public int index;
	public static boolean leaderElected = false;
	public static int currentRound = 0;
	public int currentPhase = 0;

	public void round() {
		toMaster.get(index).putMessage( "DONE") ;
		System.out.println("slave " + index + "> Sending DONE message back to master");
	}

	public boolean isRoundDone() {
       
		for (int i = 0; i < n; i++) {
			if (!toMaster.get(i).getMessage().equals("DONE")) {
				 return false; 
			}

		}
		return true;
	 
	}
	
       
	

	public Process(int id, int index) {
		this.id = id;
		this.index = index;
	}

	public void spawnThreads() {
		Random random = new Random();
		HashSet<Integer> set = new HashSet<>();
		ArrayList<Process> list = new ArrayList<Process>();

		for (int i = 0; i < n; i++) {
			ids[i] = random.nextInt(Integer.MAX_VALUE);
			while (set.contains(ids[i])) {
				ids[i] = random.nextInt(Integer.MAX_VALUE);
			}
			Channel inbound = new Channel();
			Channel outbound = new Channel();
			toMaster.add(inbound);
			fromMaster.add(outbound);
			set.add(ids[i]);
			Process p = new Process(ids[i], i);
			p.type = "slave";
			list.add(p);
			Thread t = new Thread(p);
			t.start();
		}
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
		Process master = new Process(-1, -1);
		master.type = "master";
		Thread masterThread = new Thread(master);
		masterThread.start();

	}

}
