import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class RUBTClient {

	private final static String KEY_INFO_HASH = "info_hash";
	private final static String KEY_PEER_ID = "peer_id";
	private final static String KEY_PORT = "port";
	private final static String KEY_UPLOADED = "uploaded";
	private final static String KEY_DOWNLOADED = "downloaded";
	private final static String KEY_LEFT = "left";
	private final static String KEY_EVENT = "event";
	private final static String KEY_EVENT_STARTED = "started";
	private final static String KEY_EVENT_STOPPED = "stopped";
	private final static String KEY_EVENT_COMPLETELED = "completed";
	private final static String KEY_IP = "ip"; // optional!

	/*
	 * Open the .torrent file and parse the data inside. You may use the
	 * Bencoder2.java class to decode the data.
	 */

	public static void main(String[] args) {
		/*
		 * Simulated arguments
		 */

		String[] simargs = new String[2];

		simargs[0] = "cs352.png.torrent";
		simargs[1] = "cs352.png";

		TorrentInfo ti;

		/*
		 * Step 1
		 */

		try {
			ti = getTorrentInfoFrom(simargs[0]);
		} catch (Exception e) {
			System.out
					.println("There's something wrong with your torrent info file.");
			return;
		}

		/*
		 * Step 2
		 */

		try {
			getTrackerResponse(ti);
		} catch (Exception e) {
			System.out.println("There was a problem with a GET request.");
			e.printStackTrace();
			return;
		}

		/*
		 * Step 3
		 */

		decodeTrackerResponse();

		/*
		 * Steps 5-7 and 8
		 */
		downloadFile();

		/*
		 * Step 9
		 */

		completeConnection();

		/*
		 * Step 10
		 */

		saveFile();

	}

	/**
	 * Convinience wrapper for the first step of this assignment, accepting the
	 * torrent info filename and returning the torrent info object.
	 * 
	 * @param file
	 *            the path to the file
	 * @return
	 * @throws IOException
	 *             if the file doesn't exist
	 * @throws BencodingException
	 *             if the file isn't b-encoded properly
	 */
	private static TorrentInfo getTorrentInfoFrom(String file)
			throws IOException, BencodingException {
		byte[] torrentFileBytes;
		TorrentInfo ti;

		try {
			torrentFileBytes = readFile(file);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw ioe;
		}

		try {
			ti = new TorrentInfo(torrentFileBytes);
		} catch (BencodingException be) {
			be.printStackTrace();
			throw be;
		}

		return ti;
	}

	/**
	 * Send an HTTP GET request to the tracker at the IP address and port
	 * specified by the TorrentFile object. The java.net.URL class is very
	 * useful for this.
	 * 
	 * @author Paul Jones
	 * 
	 * @param ti
	 *            any torrent info object
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void getTrackerResponse(TorrentInfo ti)
			throws UnknownHostException, IOException {
		URLEncoder ue;

		String info_hash = URLEncoder.encode(byteBufferToString(ti.info_hash),
				"utf-8"); // info_hash
		String peer_id = URLEncoder.encode(
				"" + "paukevinsrichschmidt".length(), "utf-8"); // peer_id
		String ip = URLEncoder.encode("" + InetAddress.getLocalHost(), "utf-8"); // my
																					// ip
		String port = URLEncoder.encode("" + 6883, "utf-8"); // port
		String downloaded = URLEncoder.encode("" + 0, "utf-8");
		String uploaded = URLEncoder.encode("" + 0, "utf-8");
		String left = URLEncoder.encode("" + ti.file_length, "utf-8");

		HttpURLConnection connection = (HttpURLConnection) ti.announce_url
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "text/plain");
		connection.setRequestProperty("charset", "utf-8");

		connection.addRequestProperty(KEY_INFO_HASH, info_hash);
		connection.addRequestProperty(KEY_PEER_ID, peer_id);
		connection.addRequestProperty(KEY_IP, ip);
		connection.addRequestProperty(KEY_PORT, port);
		connection.addRequestProperty(KEY_DOWNLOADED, downloaded);
		connection.addRequestProperty(KEY_UPLOADED, uploaded);
		connection.addRequestProperty(KEY_LEFT, left);

		connection.connect();
		print("" + connection.getResponseCode());
	}

	private static void addByteBufferKeyValueProperty(URLConnection conn,
			ByteBuffer a, ByteBuffer b) {
		conn.addRequestProperty(byteBufferToString(a), byteBufferToString(b));
	}

	private static void addByteBufferKeyValueProperty(URLConnection conn,
			ByteBuffer a, String b) {
		conn.addRequestProperty(byteBufferToString(a), b);
	}

	/*
	 * Capture the response from the tracker and decode it in order to get the
	 * list of peers. From this list of peers, use only the peer at IP address
	 * 128.6.171.3. You must extract this IP from the list, hard-coding it is
	 * not acceptable.
	 */

	public static void decodeTrackerResponse() {

	}

	/*
	 * Open a TCP socket on the local machine and contact the peer using the BT
	 * peer protocol and request a piece of the file.
	 */

	/*
	 * Download the piece of the file and verify its SHA-1 hash against the hash
	 * stored in the metadata file. The first time you begin the download, you
	 * need to contact the tracker and let it know you are starting to download.
	 */

	/*
	 * After a piece is downloaded and verified, the peer is notified that you
	 * have completed the piece.
	 */

	/*
	 * Repeat steps 5-7 (using the same TCP connection) for the rest of the
	 * file.
	 */

	public static void downloadFile() {

	}

	/*
	 * When the file is finished, you must contact the tracker and send it the
	 * completed event and properly close all TCP connections
	 */

	public static void completeConnection() {

	}

	/*
	 * Save the file to the hard disk according to the second command-line
	 * argument.
	 */

	public static void saveFile() {

	}

	/**
	 * Convinience method for getting byte array adapted from
	 * 
	 * @see http://stackoverflow.com/a/7591216/1489522
	 * 
	 * @param file
	 *            is a filename
	 * @return a byte array
	 * @throws IOException
	 *             if the file doesn't exist or is too big
	 */
	private static byte[] readFile(String file) throws IOException {
		RandomAccessFile f = new RandomAccessFile(new File(file), "r");
		int length = (int) f.length();
		byte[] data = new byte[length];
		f.readFully(data);
		f.close();
		return data;
	}

	/**
	 * This returns a string from a byte array, where every byte is cast to a
	 * char. This is used for debugging and curiosity purposes.
	 * 
	 * @author Paul Jones
	 * 
	 * @param b
	 * @return
	 */
	private static String byteBufferToString(ByteBuffer b) {
		String s = new String();

		for (int i = 0; i < b.array().length; i++) {
			s += b.array()[i];
		}

		return s;
	}

	/**
	 * I'm lazy.
	 */
	private static void print(String s) {
		System.out.println(s);
	}

}