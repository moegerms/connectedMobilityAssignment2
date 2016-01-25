/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package applications;

import java.util.*;

import input.WebPage;
import input.WebPages;
import report.PingAppReporter;
import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;

/**
 * Simple ping application to demonstrate the application support. The
 * application can be configured to send pings with a fixed interval or to only
 * answer to pings it receives. When the application receives a ping it sends
 * a pong message in response.
 *
 * The corresponding <code>PingAppReporter</code> class can be used to record
 * information about the application behavior.
 *
 * @see PingAppReporter
 * @author teemuk
 */
public class PingApplication extends Application {
	/** Run in passive mode - don't generate pings but respond */
	public static final String PING_PASSIVE = "passive";
	/** Ping generation interval */
	public static final String PING_INTERVAL = "interval";
	/** Ping interval offset - avoids synchronization of ping sending */
	public static final String PING_OFFSET = "offset";
	/** Destination address range - inclusive lower, exclusive upper */
	public static final String PING_DEST_RANGE = "destinationRange";
	/** Seed for the app's random number generator */
	public static final String PING_SEED = "seed";
	/** Size of the ping message */
	public static final String PING_PING_SIZE = "pingSize";
	/** Size of the pong message */
	public static final String PING_PONG_SIZE = "pongSize";

	/** Application ID */
	public static final String APP_ID = "fi.tkk.netlab.PingApplication";

	// Private vars
	private double	lastPing = 0;
	private double	interval = 500;
	private boolean passive = false;
	private int		seed = 0;
	private int		destMin=0;
	private int		destMax=1;
	private int		pingSize=1;
	//private int		pongSize=1;
	private Random	rng;

	private boolean caching = true;
	private	LinkedList cache = new LinkedList();
	private int cacheEntries = 10;

	/**
	 * Creates a new ping application with the given settings.
	 *
	 * @param s	Settings to use for initializing the application.
	 */
	public PingApplication(Settings s) {
		if (s.contains(PING_PASSIVE)){
			this.passive = s.getBoolean(PING_PASSIVE);
		}
		if (s.contains(PING_INTERVAL)){
			this.interval = s.getDouble(PING_INTERVAL);
		}
		if (s.contains(PING_OFFSET)){
			this.lastPing = s.getDouble(PING_OFFSET);
		}
		if (s.contains(PING_SEED)){
			this.seed = s.getInt(PING_SEED);
		}
		if (s.contains(PING_PING_SIZE)) {
			this.pingSize = s.getInt(PING_PING_SIZE);
		}
		//if (s.contains(PING_PONG_SIZE)) {
		//	this.pongSize = s.getInt(PING_PONG_SIZE);
		//}
		if (s.contains(PING_DEST_RANGE)){
			int[] destination = s.getCsvInts(PING_DEST_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}

		rng = new Random(this.seed);
		this.interval = drawNextHomepageRequest();
		super.setAppID(APP_ID);
	}
	private void addToCache(int key, int size){
		WebPage webPage = new WebPage(key,size);
		for(int i = 0; i< cache.size(); i++){
			if(((WebPage) cache.get(i)).getWebPageNumber() == key){
				cache.remove(i);
			}
		}
		cache.addFirst(webPage);
		if(cache.size()> cacheEntries){
			cache.removeLast();
		}
		/*System.out.println("queue "+Arrays.toString(cache.toArray()));
		for(int i = 0; i< cache.size(); i++){
			System.out.println(""+i+" PageNumber: "+((WebPage) cache.get(i)).getWebPageNumber()+" PageSize: "+((WebPage) cache.get(i)).getWebPageSize());
		}*/
	}

	private int drawNextHomepageRequest(){
		//rng.setSeed((int)(SimClock.getTime()*10));
		Random random = new Random();

		//System.out.println(random.nextDouble());
		double rand = random.nextDouble();
		int pauseBetweenRequests = (int) (rand*60*5)*2;
		//System.out.println("pause "+pauseBetweenRequests);
		return pauseBetweenRequests;
	}


	/**
	 * Copy-constructor
	 *
	 * @param a
	 */
	public PingApplication(PingApplication a) {
		super(a);
		this.lastPing = a.getLastPing();
		this.interval = a.getInterval();
		this.passive = a.isPassive();
		this.destMax = a.getDestMax();
		this.destMin = a.getDestMin();
		this.seed = a.getSeed();
		//this.pongSize = a.getPongSize();
		this.pingSize = a.getPingSize();
		this.rng = new Random(this.seed);
		this.interval = drawNextHomepageRequest();
	}

	/**
	 * Handles an incoming message. If the message is a ping message replies
	 * with a pong message. Generates events for ping and pong messages.
	 *
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		//System.out.println("handle");
		String type = (String)msg.getProperty("type");
		if (type==null) return msg; // Not a ping/pong message

		// Respond with pong if we're the recipient
		if (msg.getTo()==host && type.equalsIgnoreCase("ping")) {
			int webpageNumber = (int)msg.getProperty("webpageNumber");
			int webpageSize = webPages.getWebPage(webpageNumber);

			//String id = "pong" + SimClock.getIntTime() + "-" +
			String id = "pong" + (int) (SimClock.getTime()*10) + "-" +
				host.getAddress();
			Message m = new Message(host, msg.getFrom(), id, webpageSize);
			m.addProperty("type", "pong");
			m.setAppID(APP_ID);
			m.addProperty("webpageNumber", webpageNumber);
			host.createNewMessage(m);

			//System.out.println("send pong from:"+m.getFrom().getName()+" \tto:  "+m.getTo().getName() +" \tsize: "+m.getSize()+ " \tttl "+m.getTtl()+" \tid "+m.getId()+" \thop count "+m.getHopCount());
			// Send event to listeners
			super.sendEventToListeners("GotPing", null, host);
			super.sendEventToListeners("SentPong", null, host);
		}

		// Received a pong reply
		if (msg.getTo()==host && type.equalsIgnoreCase("pong")) {
			// Send event to listeners

			//System.out.println("receive pong from:"+msg.getFrom().getName()+" to: "+msg.getTo().getName() +" size: "+msg.getSize()+"\tid "+msg.getId());
			System.out.println(""+((int) msg.getProperty("webpageNumber"))+","+msg.getSize());
			if(caching) {
				addToCache((int) msg.getProperty("webpageNumber"), msg.getSize());
			}
			super.sendEventToListeners("GotPong", null, host);
		}

		return msg;
	}

	/**
	 * Draws a random host from the destination range
	 *
	 * @return host
	 */
	private DTNHost randomHost() {
		int destaddr = 0;
		if (destMax == destMin) {
			destaddr = destMin;
		}
		destaddr = destMin + rng.nextInt(destMax - destMin);
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(destaddr);
	}

	private int cellularHost = 0;
	private DTNHost getCellularHost(){
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(cellularHost);
	}

	@Override
	public Application replicate() {
		return new PingApplication(this);
	}

	/**
	 * Sends a ping packet if this is an active application instance.
	 *
	 * @param host to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {
		//System.out.println(this);
		if (this.passive) return;
		double curTime = SimClock.getTime();
		if (curTime - this.lastPing >= this.interval) {
			// Time to send a new ping
			Message m = new Message(host, getCellularHost(), "ping" +
					SimClock.getIntTime() + "-" + host.getAddress(),
					getPingSize());
			m.addProperty("type", "ping");
			m.addProperty("webpageNumber", webPages.getRandomWebPageNumber());
			m.setAppID(APP_ID);
			host.createNewMessage(m);

			//System.out.println("Send ping");

			// Call listeners
			super.sendEventToListeners("SentPing", null, host);

			this.interval = drawNextHomepageRequest();
			this.lastPing = curTime;
		}
	}

	/**
	 * @return the lastPing
	 */
	public double getLastPing() {
		return lastPing;
	}

	/**
	 * @param lastPing the lastPing to set
	 */
	public void setLastPing(double lastPing) {
		this.lastPing = lastPing;
	}

	/**
	 * @return the interval
	 */
	public double getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(double interval) {
		this.interval = interval;
	}

	/**
	 * @return the passive
	 */
	public boolean isPassive() {
		return passive;
	}

	/**
	 * @param passive the passive to set
	 */
	public void setPassive(boolean passive) {
		this.passive = passive;
	}

	/**
	 * @return the destMin
	 */
	public int getDestMin() {
		return destMin;
	}

	/**
	 * @param destMin the destMin to set
	 */
	public void setDestMin(int destMin) {
		this.destMin = destMin;
	}

	/**
	 * @return the destMax
	 */
	public int getDestMax() {
		return destMax;
	}

	/**
	 * @param destMax the destMax to set
	 */
	public void setDestMax(int destMax) {
		this.destMax = destMax;
	}

	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}

	/**
	 * @return the pongSize
	 */
	private static WebPages webPages = new WebPages();
	/*public int getPongSize() {
		pongSize = (int) webPages.getRandomWebPageSize();
		return pongSize;
	}*/

	/**
	 * @param pongSize the pongSize to set
	 */
	/*public void setPongSize(int pongSize) {
		this.pongSize = pongSize;
	}*/

	/**
	 * @return the pingSize
	 */
	public int getPingSize() {
		return pingSize;
	}

	/**
	 * @param pingSize the pingSize to set
	 */
	public void setPingSize(int pingSize) {
		this.pingSize = pingSize;
	}

}
