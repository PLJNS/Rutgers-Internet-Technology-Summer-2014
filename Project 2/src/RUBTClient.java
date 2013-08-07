import java.net.MalformedURLException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.io.File;

public class RUBTClient {
	
	public static ByteBuffer[] pieces = null;
	public static boolean[] requests = null;
	public static boolean[] have = null;
	public static int downloaded = 0;
	public static int uploaded = 0;
	public static TorrentInfo torrentInfo = null;
	public static TrackerResponse trackerResponse = null;
	public static String announce_url = "";

	/**
	 * @param args
	 * @throws InterruptedException
	 */

	public static void main(String[] args) throws InterruptedException {
		 
		FileOutputStream fileoutput;
		ArrayList<Peer> peers;
		TrackerThread t;
		String ipString;
		FrontDoor f;
		Scanner sc;
		DPeer p;
		int i=0, choice=0;
		
		//parse torrent file
		torrentInfo = TorrentInfo.getTorrentInfoFrom(args[0]);
		
		if (RUBTClientConstants.DEVELOP)
			System.out.println(torrentInfo);

		//send HTTP Request to tracker
		trackerResponse = new TrackerResponse(torrentInfo);

		if (RUBTClientConstants.DEVELOP) 
			System.out.println(trackerResponse);
		
		announce_url = trackerResponse.announceURL;

		RUBTClientUtils.initializeFields();
		
		//spawn download threads
		for (i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).ip = trackerResponse.peers.get(i).ip.replaceAll(":",".");
			new Thread(trackerResponse.peers.get(i)).start();
		}
		
		/*
		//sets up front door object to listen for peers and spawn upload threads
		f = new FrontDoor();
		new Thread(f).start();

		//spawn tracker thread to send updates during time interval
		t = new TrackerThread();
		new Thread(t).start();*/

		//small interface to allow them to exit the program gracefully?
		sc = new Scanner(System.in);
		while (true) {
			System.out.println("1) quit program");						//just quits program, saving state not implemented yet
			choice = sc.nextInt();

			if (choice == 1) {
				System.out.println("exiting and saving state");
			
				if(RUBTClientUtils.check()) {
					System.out.println("We have finished downloading all pieces, attempting to save file");
						try {
					// save file
					fileoutput = new FileOutputStream(new File(torrentInfo.file_name));
						
					for (i = 0; i < pieces.length; i++) {
						byte[] array = pieces[i].array();
						fileoutput.write(pieces[i].array());
					}
					
					fileoutput.close();
						
					//send stopped event to tracker
					trackerResponse.sendTrackerFinishedStopped(announce_url,torrentInfo.info_hash.array(), downloaded, uploaded, 0);
					}catch(Exception e){
						e.printStackTrace();
					}
					break;
				}else{
					System.out.println("wait were still dowloading (at this point we would save state and resume)");
				}
			}else {
				System.out.println("Invalid option, please enter 1 to quit");
			}
		}
		System.exit(0);
	}
}