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
	private Random	rng;
	private int cellularDelay = 300;
	private int requestedWebPageNumber = 0;

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
		if (s.contains(PING_DEST_RANGE)){
			int[] destination = s.getCsvInts(PING_DEST_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}

		rng = new Random(this.seed);
		this.interval = drawNextHomepageRequest();
		super.setAppID(APP_ID);
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
		String type = (String)msg.getProperty("type");
		if (type==null) return msg; // Not a ping/pong message

		// Respond with pong if we're the recipient
		if (msg.getTo()==host && type.equalsIgnoreCase("ping")) {
			int webpageNumber = (int)msg.getProperty("webpageNumber");
			int webpageSize = webPages.getWebPage(webpageNumber);

			if(host.getName().startsWith("p")){
				//Check pedestrians cache
				if(host.useCache()) {
					if(host.findWebPageInCache(webpageNumber) == webpageSize){
						//continue to send back the chached site
					}else{
						return msg;
					}
				}else{
					return msg;
				}
			}



			//String id = "pong" + SimClock.getIntTime() + "-" +
			String id = "pong" + (int) (SimClock.getTime()*10) + "-" +
				host.getAddress();
			Message m = new Message(host, msg.getFrom(), id, webpageSize);
			m.addProperty("type", "pong");
			m.setAppID(APP_ID);
			m.addProperty("webpageNumber", webpageNumber);
			host.createNewMessage(m);

		//	System.out.println("send pong from:"+m.getFrom().getName()+" \tto:  "+m.getTo().getName() +" \tsize: "+m.getSize()+ " \tttl "+m.getTtl()+" \tid "+m.getId()+" \thop count "+m.getHopCount());
			// Send event to listeners
			super.sendEventToListeners("GotPing", null, host);
			super.sendEventToListeners("SentPong", null, host);
		}

		// Received a pong reply
		if (msg.getTo()==host && type.equalsIgnoreCase("pong")) {
			receivePong(host,msg);
		}

		return msg;
	}
	private void receivePong(DTNHost host, Message msg){
		//if(msg.getFrom().getName().startsWith("p") && msg.getTo().getName().startsWith("p"))
		//System.out.println("receive pong from:"+msg.getFrom().getName()+" to: "+msg.getTo().getName() +" size: "+msg.getSize()+"\tid "+msg.getId());
		//System.out.println(""+((int) msg.getProperty("webpageNumber"))+","+msg.getSize());
		if(host.useCache()) {
			host.addToCache((int) msg.getProperty("webpageNumber"), msg.getSize());
		}
		host.setWaitForReply(false);
		// Send event to listeners
		super.sendEventToListeners("GotPong", null, host);
	}

	/**
	 * Draws a random host from the destination range
	 *
	 * @return host
	 */
	/*private DTNHost randomHost() {
		int destaddr = 0;
		if (destMax == destMin) {
			destaddr = destMin;
		}
		destaddr = destMin + rng.nextInt(destMax - destMin);
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(destaddr);
	}*/


	@Override
	public Application replicate() {
		return new PingApplication(this);
	}

	private void createNewRequest(){
		requestedWebPageNumber = webPages.getRandomWebPageNumber();


	}
	/**
	 * Sends a ping packet if this is an active application instance.
	 *
	 * @param host to which the application instance is attached
	 */
	//@Override
	public void update(DTNHost host) {
		//System.out.println(this);
		if (this.passive) return;
		double curTime = SimClock.getTime();
		if (curTime - this.lastPing >= this.interval) {
			createNewRequest();

			if(host.useCache()) {
				//Local cache
				int webPageSize = host.findWebPageInCache(requestedWebPageNumber);
				if (webPageSize > 0) {
					//Webpage still in cache
					String id = "pong" + (int) (SimClock.getTime() * 10) + "-" +
							host.getAddress();
					Message m = new Message(host, host, id, webPageSize);
					m.addProperty("type", "pong");
					m.setAppID(APP_ID);
					m.addProperty("webpageNumber", requestedWebPageNumber);
					receivePong(host, m);        //Use a message from,to itself
				} else {
					//Check on all available nodes
					host.sendWebPageRequests(pingSize, curTime, requestedWebPageNumber, APP_ID);
				}
			}else{

				//Check on all available nodes
				host.sendWebPageRequests(pingSize, curTime, requestedWebPageNumber, APP_ID);
			}

			// Call listeners
			super.sendEventToListeners("SentPing", null, host);
			this.interval = drawNextHomepageRequest();
			this.lastPing = curTime;

		}
	}


	private int cellularHost = 0;
	private DTNHost getCellularHost(){
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(cellularHost);
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

	private static WebPages webPages = new WebPages();


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
