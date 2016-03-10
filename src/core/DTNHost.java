/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.sun.org.apache.xpath.internal.SourceTree;
import input.WebPage;
import movement.MovementModel;
import movement.Path;
import routing.*;
import routing.util.RoutingInfo;

import static core.Constants.DEBUG;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {

    public static int requestTimeOut = 60; //seconds
    public static int temp = 0;

	private static int nextAddress = 0;
	private int address;

	private Coord location; 	// where is the host
	private Coord destination;	// where is it going

	private MessageRouter router;
	private MovementModel movement;
	private Path path;
	private double speed;
	private double nextTimeToMove;
	private String name;
	private List<MessageListener> msgListeners;
	private List<MovementListener> movListeners;
	private List<NetworkInterface> net;
	private ModuleCommunicationBus comBus;

	public boolean useCache() {
		return caching;
	}


	//private boolean waitForReply = false;
	//public void setWaitForReply(boolean waitForReply){
	//	this.waitForReply = waitForReply;
	//}


	//public boolean getWaitForReply(){
	//	return waitForReply;
	//}

    public boolean existPendingRequests() {
        return (requestBuffer.size() > 0 ? true : false);
    }


	private Transmit_Cellular transmit_cellular;
	private Transmit_WiFi transmit_hotspot;
	private Transmit_Pedestrian transmit_pedestrian;
	private int pingSize;
	private double curTime;
	private int requestedWebPageNumber;
    private double pageRequestCreationTime;
	private String APP_ID;

	private int demo_case = 1;
	private boolean caching = false;
	private int cacheEntries = 0;
	private LinkedList cache = new LinkedList();
    private ArrayList<RequestBufferEntry> requestBuffer = new ArrayList<RequestBufferEntry>();

	public void sendWebPageRequests(int pingSize, double curTime, int requestedWebPageNumber, String APP_ID, double pageRequestCreationTime) {
		this.pingSize = pingSize;
		this.curTime = curTime;
		this.requestedWebPageNumber = requestedWebPageNumber;
        this.pageRequestCreationTime = pageRequestCreationTime;
		this.APP_ID = APP_ID;
		//setWaitForReply(true);
		transmit_cellular = null;
		transmit_hotspot = null;
		transmit_pedestrian = null;

        //typesOfDestionations.clear();

		updateTransmit(true);
	}
	public void updateTransmit(boolean newRequest){
		//if(!getWaitForReply()) return;		//Not linked to a message, just an open need for a website

        int result;

		switch(demo_case){
			case 0:
                /* Empty dull case. */
				/*if(transmit_cellular == null) {
					transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID);
					int result = transmit_cellular.transmitNewPageRequest();

                    if(result > 0) {
                        for(int i=0;i < result;i++)
                            typesOfDestionations.add(TypeOfHost.CELLULAR_BASE);
                    }
				}
				if(transmit_hotspot == null) {
					transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID);
                    int result = transmit_hotspot.transmitNewPageRequest();

                    if(result > 0) {
                        for (int i = 0; i < result; i++)
                            typesOfDestionations.add(TypeOfHost.WIFI_HOTSPOT);
                    }
				}*/
				break;
			case 1:
				//50% cellular network only, together with case 2
				//if(transmit_cellular == null) {
                if(newRequest){
					transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_cellular.transmitNewPageRequest();

                    if(result > 0) {
                        entry.stateIndicator = 1;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));
                    }

                    requestBuffer.add(entry);
				}
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Act like the "newRequest" case.
                        if(entry.stateIndicator == 0 || (entry.stateIndicator == 1 && ((SimClock.getTime() - entry.curTime) > requestTimeOut) )) {
                            transmit_cellular = new Transmit_Cellular(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_cellular.transmitNewPageRequest();

                            if(result > 0) {
                                entry.stateIndicator = 1;

                                for (int i = 0; i < result; i++)
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));  //TODO: Should we count the resent requests?
                            }
                        }
                    }
                }
				break;
			case 2:
				// 50% wifi only users, together with case 1
				//if(transmit_hotspot == null) {
                if(newRequest){
					transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_hotspot.transmitNewPageRequest();

                    if(result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));
                    }

                    requestBuffer.add(entry);
				}
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Act like the "newRequest" case.
                        if(entry.stateIndicator == 0 || (entry.stateIndicator == 2 && ((SimClock.getTime() - entry.curTime) > requestTimeOut) )) {
                            transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_hotspot.transmitNewPageRequest();

                            if(result > 0) {
                                entry.stateIndicator = 2;

                                for (int i = 0; i < result; i++)
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));  //TODO: Should we count the resent requests?
                            }
                        }
                    }
                }
				break;
			case 3:
				// All cellular with WiFi offloading - Instant
				//if(transmit_hotspot == null) {
                if(newRequest) {
                    transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_hotspot.transmitNewPageRequest();

                    if (result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));
                    }


                    //Use cellular network if not on wifi
                    if (/*transmit_cellular == null &&*/ result == 0) { //meaning, if the request was not sent to any hotspot.
                        transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                        result = transmit_cellular.transmitNewPageRequest();

                        if (result > 0) {
                            entry.stateIndicator = 1;

                            for (int i = 0; i < result; i++)
                                typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));
                        }
                    }

                    requestBuffer.add(entry);
                }
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Act like the "newRequest" case.
                        if(entry.stateIndicator == 0 || ((entry.stateIndicator == 1 || entry.stateIndicator == 2) && ((SimClock.getTime() - entry.curTime) > requestTimeOut) )) {
                            transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_hotspot.transmitNewPageRequest();

                            if (result > 0) {
                                entry.stateIndicator = 2;

                                for (int i = 0; i < result; i++)
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT)); //TODO: Should we count the resent requests?
                            }


                            //Use cellular network if not on wifi
                            if (/*transmit_cellular == null &&*/ result == 0) { //meaning, if the request was not sent to any hotspot.
                                transmit_cellular = new Transmit_Cellular(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                                entry.curTime = SimClock.getTime();

                                result = transmit_cellular.transmitNewPageRequest();

                                if (result > 0) {
                                    entry.stateIndicator = 1;

                                    for (int i = 0; i < result; i++)
                                        typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE)); //TODO: Should we count the resent requests?
                                }
                            }
                        }
                    }
                }
                break;
			case 4:
				// All cellular with WiFi offloading - 60 sec wait time
				//if(transmit_hotspot == null) {
                if(newRequest) {
                    transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_hotspot.transmitNewPageRequest();

                    if (result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));
                    }
                    //}

                    requestBuffer.add(entry);

                    //TODO: The below should not apply on new requests.
                    //Wait 60 sec for wifi, then use cellular network
                    //if (/*transmit_cellular == null &&*/  ((SimClock.getTime() - curTime) > 60)) {
                        /*transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID);
                        result = transmit_cellular.transmitNewPageRequest();

                        if (result > 0) {
                            for (int i = 0; i < result; i++)
                                typesOfDestionations.add(TypeOfHost.CELLULAR_BASE);
                        }
                    }*/
                }
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Several cases, mainly for the cellular interface.
                        if((SimClock.getTime() - entry.curTime) > requestTimeOut) { //requestTimeout and offloading time limit, have the same value
                            transmit_cellular = new Transmit_Cellular(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_cellular.transmitNewPageRequest();

                            if (result > 0) {
                                entry.stateIndicator = 1;

                                for (int i = 0; i < result; i++) {
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));  //TODO: Should we count the resent requests?
                                    //System.out.println(++temp);
                                }
                            }

                            if (result == 0) { //meaning, if the request was not sent to Cell.
                                //Try the procedure from the beginning.
                                transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                                entry.curTime = SimClock.getTime();

                                result = transmit_hotspot.transmitNewPageRequest();

                                if (result > 0) {
                                    entry.stateIndicator = 2;

                                    for (int i = 0; i < result; i++)
                                        typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT)); //TODO: Should we count the resent requests?
                                }
                            }

                        }
                    }
                }
                break;
			case 5:
				// All cellular with WiFi offloading - 300 sec wait time
				//if(transmit_hotspot == null) {
                if(newRequest) {
                    transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_hotspot.transmitNewPageRequest();

                    if (result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));
                    }
                    //}

                    requestBuffer.add(entry);

                    //TODO: The below should not apply on new requests.
                    //Wait 300 sec for wifi, then use cellular network
                    //if (/*transmit_cellular == null &&*/  ((SimClock.getTime() - curTime) > 300)) {
                        /*transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID);
                        result = transmit_cellular.transmitNewPageRequest();

                        if (result > 0) {
                            for (int i = 0; i < result; i++)
                                typesOfDestionations.add(TypeOfHost.CELLULAR_BASE);
                        }
                    }*/
                }
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Several cases, mainly for the cellular interface.
                        if((entry.stateIndicator == 0 && ((SimClock.getTime() - entry.curTime) > 300)) || (entry.stateIndicator != 0 && ((SimClock.getTime() - entry.curTime) > requestTimeOut))) {
                            transmit_cellular = new Transmit_Cellular(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_cellular.transmitNewPageRequest();

                            if (result > 0) {
                                entry.stateIndicator = 1;

                                for (int i = 0; i < result; i++) {
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));  //TODO: Should we count the resent requests?
                                    //System.out.println(++temp);
                                }
                            }

                            if (result == 0) { //meaning, if the request was not sent to Cell.
                                //Try the procedure from the beginning.
                                transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                                entry.curTime = SimClock.getTime();

                                result = transmit_hotspot.transmitNewPageRequest();

                                if (result > 0) {
                                    entry.stateIndicator = 2;

                                    for (int i = 0; i < result; i++)
                                        typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT)); //TODO: Should we count the resent requests?
                                }
                            }

                        }
                    }
                }
                break;
			case 6:
				// Cellular with WiFi offloading and peer-to-peer caching (wait 300sec)
				//if(transmit_hotspot == null) {
                if(newRequest) {
                    //First, try to find the page within the caches of nearby pedestrians.
                    transmit_pedestrian = new Transmit_Pedestrian(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);
                    RequestBufferEntry entry = new RequestBufferEntry(requestedWebPageNumber, 0, curTime, pingSize, APP_ID, pageRequestCreationTime);

                    result = transmit_pedestrian.transmitNewPageRequest();

                    if (result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.REGULAR_HOST));
                    }

                    //At the same time, try to find Hotspots in range.
                    transmit_hotspot = new Transmit_WiFi(this, pingSize, requestedWebPageNumber, APP_ID, pageRequestCreationTime);

                    result = transmit_hotspot.transmitNewPageRequest();

                    if (result > 0) {
                        entry.stateIndicator = 2;

                        for (int i = 0; i < result; i++)
                            typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT));
                    }

                    requestBuffer.add(entry);

                    //TODO: The below should not apply on new requests.
                    //Wait 300 sec for wifi, then use cellular network
                    //if (/*transmit_cellular == null && */ ((SimClock.getTime() - curTime) > 300)) {
                        /*transmit_cellular = new Transmit_Cellular(this, pingSize, requestedWebPageNumber, APP_ID);
                        result = transmit_cellular.transmitNewPageRequest();

                        if (result > 0) {
                            for (int i = 0; i < result; i++)
                                typesOfDestionations.add(TypeOfHost.CELLULAR_BASE);
                        }
                    }*/
                }
                else {
                    //Parse all the requests that havent been sent yet, or a response for them has not been received yet.
                    for(RequestBufferEntry entry : requestBuffer) {
                        //Several cases, mainly for the cellular interface.
                        if((entry.stateIndicator == 0 && ((SimClock.getTime() - entry.curTime) > 300)) || (entry.stateIndicator != 0 && ((SimClock.getTime() - entry.curTime) > requestTimeOut))) {
                            transmit_cellular = new Transmit_Cellular(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                            entry.stateIndicator = 0;
                            entry.curTime = SimClock.getTime();

                            result = transmit_cellular.transmitNewPageRequest();

                            if (result > 0) {
                                entry.stateIndicator = 1;

                                for (int i = 0; i < result; i++)
                                    typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.CELLULAR_BASE));  //TODO: Should we count the resent requests?
                            }

                            if (result == 0) { //meaning, if the request was not sent to Cell.
                                //Try the procedure from the beginning.
                                transmit_pedestrian = new Transmit_Pedestrian(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                                entry.curTime = SimClock.getTime();

                                result = transmit_pedestrian.transmitNewPageRequest();

                                if (result > 0) {
                                    entry.stateIndicator = 2;

                                    for (int i = 0; i < result; i++)
                                        typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.REGULAR_HOST));  //TODO: Should we count the resent requests?
                                }

                                transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);

                                result = transmit_hotspot.transmitNewPageRequest();

                                if (result > 0) {
                                    entry.stateIndicator = 2;

                                    for (int i = 0; i < result; i++)
                                        typesOfDestinations.add(new TypeOfDestinationEntry(TypeOfHost.WIFI_HOTSPOT)); //TODO: Should we count the resent requests?
                                }
                            }

                        }
                    }
                }
                break;
            default:
                break;
		}
	}


	public void setDemoCase(int demoCase) {
		this.demo_case = demoCase;
		//System.out.println("demo_case "+demo_case);
	}

	public void setCaching(boolean caching) {
		this.caching = caching;
		//System.out.println("Caching "+caching);
	}

	public void setCacheEntries(int cacheEntries) {
		this.cacheEntries = cacheEntries;
		//System.out.println("cacheEntries "+cacheEntries);
	}

	public enum TypeOfHost{
		REGULAR_HOST, WIFI_HOTSPOT, CELLULAR_BASE
	}
	private TypeOfHost typeOfHost;

    public TypeOfHost getTypeOfHost() {
        return this.typeOfHost;
    }

    private ArrayList<TypeOfDestinationEntry> typesOfDestinations = new ArrayList<TypeOfDestinationEntry>();

    public ArrayList<TypeOfDestinationEntry> getTypesOfDestinations() {
        return this.typesOfDestinations;
    }

    public ArrayList<RequestBufferEntry> getRequestBuffer() {
        return this.requestBuffer;
    }


    public class TypeOfDestinationEntry {
        TypeOfHost typeOfDestination;
        boolean counted;

        public TypeOfDestinationEntry(TypeOfHost typeOfDestionation) {
            this.typeOfDestination = typeOfDestionation;
            this.counted = false;
        }

        public TypeOfHost getTypeOfDestionation() {
            return this.typeOfDestination;
        }

        public void setCounted(boolean counted) {
            this.counted = counted;
        }

        public boolean isCounted() {
            return this.counted;
        }
    }

    public class RequestBufferEntry {
        int pingSize;
        int requestedWebPageNumber;
        double pageRequestCreationTime;
        int stateIndicator; //0 = not sent, 1 = sent by Cell but no response yet, 2 = sent by WiFi or Ped, but no response yet.
        double curTime;
        String APP_ID;

        public RequestBufferEntry(int requestedWebPageNumber, int stateIndicator, double curTime, int pingSize, String APP_ID, double pageRequestCreationTime) {
            this.pingSize = pingSize;
            this.requestedWebPageNumber = requestedWebPageNumber;
            this.pageRequestCreationTime = pageRequestCreationTime;
            this.stateIndicator = stateIndicator;
            this.curTime = curTime;
            this.APP_ID = APP_ID;
        }

        public int getRequestedWebPageNumber() {
            return this.requestedWebPageNumber;
        }

        public double getPageRequestCreationTime() {
            return this.pageRequestCreationTime;
        }
    }


	static {
		DTNSim.registerForReset(DTNHost.class.getCanonicalName());
		reset();
	}
	/**
	 * Creates a new DTNHost.
	 * @param msgLs Message listeners
	 * @param movLs Movement listeners
	 * @param groupId GroupID of this host
	 * @param interf List of NetworkInterfaces for the class
	 * @param comBus Module communication bus object
	 * @param mmProto Prototype of the movement model of this host
	 * @param mRouterProto Prototype of the message router of this host
	 */
	public DTNHost(List<MessageListener> msgLs,
			List<MovementListener> movLs,
			String groupId, List<NetworkInterface> interf,
			ModuleCommunicationBus comBus,
			MovementModel mmProto, MessageRouter mRouterProto) {


		this.comBus = comBus;
		this.location = new Coord(0,0);
		this.address = getNextAddress();
		this.name = groupId+address;
		this.net = new ArrayList<NetworkInterface>();

		Settings s = new Settings(SimScenario.GROUP_NS+address);
		s.setSecondaryNamespace(SimScenario.GROUP_NS);
		//String gid = s.getSetting(SimScenario.GROUP_ID_S);


		for (NetworkInterface i : interf) {
			NetworkInterface ni = i.replicate();
			ni.setHost(this);
			net.add(ni);
		}

		// TODO - think about the names of the interfaces and the nodes
		//this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();

		this.msgListeners = msgLs;
		this.movListeners = movLs;

		// create instances by replicating the prototypes
		this.movement = mmProto.replicate();
		this.movement.setComBus(comBus);
		this.movement.setHost(this);
		setRouter(mRouterProto.replicate());

		this.location = movement.getInitialLocation();

		this.nextTimeToMove = movement.nextPathAvailable();
		this.path = null;

		if (movLs != null) { // inform movement listeners about the location
			for (MovementListener l : movLs) {
				l.initialLocation(this, this.location);
			}
		}
		if(groupId.startsWith("z"))
			typeOfHost = TypeOfHost.CELLULAR_BASE;
		else if(groupId.startsWith("p"))
			typeOfHost = TypeOfHost.REGULAR_HOST;
		else
			typeOfHost = TypeOfHost.WIFI_HOTSPOT;
	}

	/**
	 * Returns a new network interface address and increments the address for
	 * subsequent calls.
	 * @return The next address.
	 */
	private synchronized static int getNextAddress() {
		return nextAddress++;
	}

	/**
	 * Reset the host and its interfaces
	 */
	public static void reset() {
		nextAddress = 0;
	}

	/**
	 * Returns true if this node is actively moving (false if not)
	 * @return true if this node is actively moving (false if not)
	 */
	public boolean isMovementActive() {
		return this.movement.isActive();
	}

	/**
	 * Returns true if this node's radio is active (false if not)
	 * @return true if this node's radio is active (false if not)
	 */
	public boolean isRadioActive() {
		// Radio is active if any of the network interfaces are active.
		for (final NetworkInterface i : this.net) {
			if (i.isActive()) return true;
		}
		return false;
	}

	/**
	 * Set a router for this host
	 * @param router The router to set
	 */
	private void setRouter(MessageRouter router) {
		router.init(this, msgListeners);
		this.router = router;
	}

	/**
	 * Returns the router of this host
	 * @return the router of this host
	 */
	public MessageRouter getRouter() {
		return this.router;
	}

	/**
	 * Returns the network-layer address of this host.
	 */
	public int getAddress() {
		return this.address;
	}

	/**
	 * Returns this hosts's ModuleCommunicationBus
	 * @return this hosts's ModuleCommunicationBus
	 */
	public ModuleCommunicationBus getComBus() {
		return this.comBus;
	}

    /**
	 * Informs the router of this host about state change in a connection
	 * object.
	 * @param con  The connection object whose state changed
	 */
	public void connectionUp(Connection con) {
		DTNHost otherNode = con.getOtherNode(this);
		//System.out.println("Up Conntection from "+this.getName()+" to: "+otherNode.getName());
		if(this.getName().startsWith("p")){	//Only required for pedestrians
			if(otherNode.getTypeOfHost() == DTNHost.TypeOfHost.REGULAR_HOST){
				//System.out.println("C "+this.getName() +" to pedestrian");
				//connection between pedestrians can be asked for cache
				addPedestrianConnection(otherNode, con);
			}
			if(otherNode.getTypeOfHost() == TypeOfHost.WIFI_HOTSPOT){
				//System.out.println("C "+this.getName() +" to HotSpot");
				//connection to HotSpot
				addHotSpotConnection(otherNode, con);
			}
			if(otherNode.getTypeOfHost() == TypeOfHost.CELLULAR_BASE){
				//System.out.println("C "+this.getName() +" to cellular");
				//connection to Cellular Network
				connectedCellular = otherNode;
			}
			//printConnections();
		}
		this.router.changedConnection(con);
	}
	private ArrayList<DTNHost> connectedToPedestrians = new ArrayList<>();
	private ArrayList<DTNHost> connectedToHotSpots = new ArrayList<>();
	private DTNHost connectedCellular;


	private void addPedestrianConnection(DTNHost otherHost, Connection con){
		connectedToPedestrians.add(otherHost);
		//if(getWaitForReply() && transmit_pedestrian != null) transmit_pedestrian.newConnectionPedestrian(con);

        if(demo_case == 6 && existPendingRequests()) { //if there is a meaning into sending requests to other pedestrians.
            for(RequestBufferEntry entry : requestBuffer) {
                if(entry.stateIndicator != 1) {
                    transmit_pedestrian = new Transmit_Pedestrian(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                    if(entry.stateIndicator == 0)
                        entry.curTime = SimClock.getTime();

                    transmit_pedestrian.newConnectionPedestrian(con);
                    entry.stateIndicator = 2;

                    for(Application app :router.getApplications(APP_ID)){
                        app.sendEventToListeners("SentPing", null, this, -1.0, TypeOfHost.REGULAR_HOST, 1000, -1.0);
                    }
                }
            }
        }
	}

	private void removePedestrianConnection(DTNHost otherHost){
		connectedToPedestrians.remove(otherHost);
	}




	private void addHotSpotConnection(DTNHost otherHost, Connection con){
		connectedToHotSpots.add(otherHost);

		/*if(getWaitForReply() && transmit_hotspot != null) {
			transmit_hotspot.newConnectionHotSpot(con);

			for(Application app :router.getApplications(APP_ID)){
                app.sendEventToListeners("SentPing", null, this, -1.0, TypeOfHost.WIFI_HOTSPOT, 1000);
			}
		}*/


        if(demo_case >= 2 && existPendingRequests()) { //if there is a meaning into sending requests to new hotspots.
            for(RequestBufferEntry entry : requestBuffer) {
                if(entry.stateIndicator != 1) {
                    transmit_hotspot = new Transmit_WiFi(this, entry.pingSize, entry.requestedWebPageNumber, entry.APP_ID, entry.pageRequestCreationTime);
                    if(entry.stateIndicator == 0)
                        entry.curTime = SimClock.getTime();

                    transmit_hotspot.newConnectionHotSpot(con);
                    entry.stateIndicator = 2;

                    for(Application app :router.getApplications(APP_ID)){
                        app.sendEventToListeners("SentPing", null, this, -1.0, TypeOfHost.WIFI_HOTSPOT, 1000, -1.0);
                    }
                }
            }
        }




	}

	private void removeHotSpotConnection(DTNHost otherHost){
		connectedToHotSpots.remove(otherHost);
	}
	public ArrayList<DTNHost> getHotSpotConnections(){
		return connectedToHotSpots;
	}
	public ArrayList<DTNHost> getPedestrianConnections(){
		return connectedToPedestrians;
	}

	public void connectionDown(Connection con) {
		DTNHost otherNode = con.getOtherNode(this);
		//System.out.println("Down Conntection from "+this.getName()+" to: "+otherNode.getName());
		if(this.getName().startsWith("p")){	//Only required for pedestrians
			if(otherNode.getName().startsWith("p")){
				//System.out.println("R "+this.getName() +" to pedestrian");
				//connection between pedestrians can be asked for cache
				removePedestrianConnection(otherNode);
			}
			if(otherNode.getName().startsWith("HotSpot")){
				//System.out.println("R "+this.getName() +" to HotSpot");
				//connection to HotSpot
				removeHotSpotConnection(otherNode);
			}
			if(otherNode.getName().startsWith("z")){
				//System.out.println("R "+this.getName() +" to cellular");
				//connection to Cellular Network
				connectedCellular = null;
			}
			//printConnections();
		}
		this.router.changedConnection(con);
	}

	private void printConnections(){
		System.out.println("");
		System.out.print(this.getName()+" Pedestrians: ");
		for(DTNHost host : connectedToPedestrians ){
			System.out.print(host.getName());
			System.out.print(" ,");
		}
		System.out.print(" HotSpots: ");
		for(DTNHost host : connectedToHotSpots){
			System.out.print(host.getName());
			System.out.print(" ,");
		}
		if(connectedCellular != null)
			System.out.print(" Cellular: "+connectedCellular.getName());
		System.out.println("");
	}



	public void addToCache(int key, int size){
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
	public int findWebPageInCache(int webPageNumber){
		for(int i = 0; i< cache.size(); i++){
			if(((WebPage) cache.get(i)).getWebPageNumber() == webPageNumber){
				return ((WebPage) cache.get(i)).getWebPageSize();		//Return webpage Size
			}
		}
		return -1;		//not found
	}

	/**
	 * Returns a copy of the list of connections this host has with other hosts
	 * @return a copy of the list of connections this host has with other hosts
	 */
	public List<Connection> getConnections() {
		List<Connection> lc = new ArrayList<Connection>();

		for (NetworkInterface i : net) {
			lc.addAll(i.getConnections());
		}

		return lc;
	}

	/**
	 * Returns the current location of this host.
	 * @return The location
	 */
	public Coord getLocation() {
		return this.location;
	}

	/**
	 * Returns the Path this node is currently traveling or null if no
	 * path is in use at the moment.
	 * @return The path this node is traveling
	 */
	public Path getPath() {
		return this.path;
	}


	/**
	 * Sets the Node's location overriding any location set by movement model
	 * @param location The location to set
	 */
	public void setLocation(Coord location) {
		this.location = location.clone();
	}

	/**
	 * Sets the Node's name overriding the default name (groupId + netAddress)
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the messages in a collection.
	 * @return Messages in a collection
	 */
	public Collection<Message> getMessageCollection() {
		return this.router.getMessageCollection();
	}

	/**
	 * Returns the number of messages this node is carrying.
	 * @return How many messages the node is carrying currently.
	 */
	public int getNrofMessages() {
		return this.router.getNrofMessages();
	}

	/**
	 * Returns the buffer occupancy percentage. Occupancy is 0 for empty
	 * buffer but can be over 100 if a created message is bigger than buffer
	 * space that could be freed.
	 * @return Buffer occupancy percentage
	 */
	public double getBufferOccupancy() {
		double bSize = router.getBufferSize();
		double freeBuffer = router.getFreeBufferSize();
		return 100*((bSize-freeBuffer)/bSize);
	}

	/**
	 * Returns routing info of this host's router.
	 * @return The routing info.
	 */
	public RoutingInfo getRoutingInfo() {
		return this.router.getRoutingInfo();
	}

	/**
	 * Returns the interface objects of the node
	 */
	public List<NetworkInterface> getInterfaces() {
		return net;
	}

	/**
	 * Find the network interface based on the index
	 */
	public NetworkInterface getInterface(int interfaceNo) {
		NetworkInterface ni = null;
		try {
			ni = net.get(interfaceNo-1);
		} catch (IndexOutOfBoundsException ex) {
			throw new SimError("No such interface: "+interfaceNo +
					" at " + this);
		}
		return ni;
	}

	/**
	 * Find the network interface based on the interfacetype
	 */
	protected NetworkInterface getInterface(String interfacetype) {
		for (NetworkInterface ni : net) {
			if (ni.getInterfaceType().equals(interfacetype)) {
				return ni;
			}
		}
		return null;
	}

	/**
	 * Force a connection event
	 */
	public void forceConnection(DTNHost anotherHost, String interfaceId,
			boolean up) {
		NetworkInterface ni;
		NetworkInterface no;

		System.out.println("Force Connection: "+anotherHost.name+" connection: "+interfaceId);
		if (interfaceId != null) {
			ni = getInterface(interfaceId);
			no = anotherHost.getInterface(interfaceId);

			assert (ni != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
			assert (no != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
		} else {
			ni = getInterface(1);
			no = anotherHost.getInterface(1);

			assert (ni.getInterfaceType().equals(no.getInterfaceType())) :
				"Interface types do not match.  Please specify interface type explicitly";
		}

		if (up) {
			ni.createConnection(no);
		} else {
			ni.destroyConnection(no);
		}
	}

	/**
	 * for tests only --- do not use!!!
	 */
	public void connect(DTNHost h) {
		if (DEBUG) Debug.p("WARNING: using deprecated DTNHost.connect" +
			"(DTNHost) Use DTNHost.forceConnection(DTNHost,null,true) instead");
		forceConnection(h,null,true);
	}

	/**
	 * Updates node's network layer and router.
	 * @param simulateConnections Should network layer be updated too
	 */
	public void update(boolean simulateConnections) {
		if (!isRadioActive()) {
			// Make sure inactive nodes don't have connections
			tearDownAllConnections();
			return;
		}

		if (simulateConnections) {
			for (NetworkInterface i : net) {
				i.update();
			}
		}
		updateTransmit(false);		//To enable timer

        //ArrayList<DTNHost.TypeOfDestinationEntry> typeOfDestinations = host.getTypesOfDestionations();
        for(TypeOfDestinationEntry dest : typesOfDestinations) {
            if(!dest.isCounted()) {

                for(Application app :router.getApplications(APP_ID)){
                    app.sendEventToListeners("SentPing", null, this, -1.0, dest.getTypeOfDestionation(), 1000, -1.0);
                }

                dest.setCounted(true);
            }
        }



		this.router.update();
	}

	/**
	 * Tears down all connections for this host.
	 */
	private void tearDownAllConnections() {
		for (NetworkInterface i : net) {
			// Get all connections for the interface
			List<Connection> conns = i.getConnections();
			if (conns.size() == 0) continue;

			// Destroy all connections
			List<NetworkInterface> removeList =
				new ArrayList<NetworkInterface>(conns.size());
			for (Connection con : conns) {
				removeList.add(con.getOtherInterface(i));
			}
			for (NetworkInterface inf : removeList) {
				i.destroyConnection(inf);
			}
		}
	}

	/**
	 * Moves the node towards the next waypoint or waits if it is
	 * not time to move yet
	 * @param timeIncrement How long time the node moves
	 */
	public void move(double timeIncrement) {
		double possibleMovement;
		double distance;
		double dx, dy;

		if (!isMovementActive() || SimClock.getTime() < this.nextTimeToMove) {
			return;
		}
		if (this.destination == null) {
			if (!setNextWaypoint()) {
				return;
			}
		}

		possibleMovement = timeIncrement * speed;
		distance = this.location.distance(this.destination);

		while (possibleMovement >= distance) {
			// node can move past its next destination
			this.location.setLocation(this.destination); // snap to destination
			possibleMovement -= distance;
			if (!setNextWaypoint()) { // get a new waypoint
				return; // no more waypoints left
			}
			distance = this.location.distance(this.destination);
		}

		// move towards the point for possibleMovement amount
		dx = (possibleMovement/distance) * (this.destination.getX() -
				this.location.getX());
		dy = (possibleMovement/distance) * (this.destination.getY() -
				this.location.getY());
		this.location.translate(dx, dy);
	}

	/**
	 * Sets the next destination and speed to correspond the next waypoint
	 * on the path.
	 * @return True if there was a next waypoint to set, false if node still
	 * should wait
	 */
	private boolean setNextWaypoint() {
		if (path == null) {
			path = movement.getPath();
		}

		if (path == null || !path.hasNext()) {
			this.nextTimeToMove = movement.nextPathAvailable();
			this.path = null;
			return false;
		}

		this.destination = path.getNextWaypoint();
		this.speed = path.getSpeed();

		if (this.movListeners != null) {
			for (MovementListener l : this.movListeners) {
				l.newDestination(this, this.destination, this.speed);
			}
		}

		return true;
	}

	/**
	 * Sends a message from this host to another host
	 * @param id Identifier of the message
	 * @param to Host the message should be sent to
	 */
	public void sendMessage(String id, DTNHost to) {
		this.router.sendMessage(id, to);
		System.out.println("Send: "+id+" to: "+to.name);
	}

	/**
	 * Start receiving a message from another host
	 * @param m The message
	 * @param from Who the message is from
	 * @return The value returned by
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public int receiveMessage(Message m, DTNHost from) {
		int retVal = this.router.receiveMessage(m, from);

		if (retVal == MessageRouter.RCV_OK) {
			m.addNodeOnPath(this);	// add this node on the messages path
		}

		return retVal;
	}

	/**
	 * Requests for deliverable message from this host to be sent trough a
	 * connection.
	 * @param con The connection to send the messages trough
	 * @return True if this host started a transfer, false if not
	 */
	public boolean requestDeliverableMessages(Connection con) {
		return this.router.requestDeliverableMessages(con);
	}

	/**
	 * Informs the host that a message was successfully transferred.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 */
	public void messageTransferred(String id, DTNHost from) {
		this.router.messageTransferred(id, from);
	}

	/**
	 * Informs the host that a message transfer was aborted.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 * @param bytesRemaining Nrof bytes that were left before the transfer
	 * would have been ready; or -1 if the number of bytes is not known
	 */
	public void messageAborted(String id, DTNHost from, int bytesRemaining) {
		this.router.messageAborted(id, from, bytesRemaining);
	}

	/**
	 * Creates a new message to this host's router
	 * @param m The message to create
	 */
	public void createNewMessage(Message m) {
		this.router.createNewMessage(m);
	}

	/**
	 * Deletes a message from this host
	 * @param id Identifier of the message
	 * @param drop True if the message is deleted because of "dropping"
	 * (e.g. buffer is full) or false if it was deleted for some other reason
	 * (e.g. the message got delivered to final destination). This effects the
	 * way the removing is reported to the message listeners.
	 */
	public void deleteMessage(String id, boolean drop) {
		this.router.deleteMessage(id, drop);
	}

	/**
	 * Returns a string presentation of the host.
	 * @return Host's name
	 */
	public String toString() {
		return name;
	}

	/**
	 * Checks if a host is the same as this host by comparing the object
	 * reference
	 * @param otherHost The other host
	 * @return True if the hosts objects are the same object
	 */
	public boolean equals(DTNHost otherHost) {
		return this == otherHost;
	}

	/**
	 * Compares two DTNHosts by their addresses.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(DTNHost h) {
		return this.getAddress() - h.getAddress();
	}

	public String getName(){
		return name;
	}
}
