package routing;

import core.*;

/**
 * Created by Matthias on 26.01.2016.
 */
public abstract class Transmit {

    protected DTNHost host;
    protected int pingSize;
    protected int webPageNumber;
    protected double pageRequestCreationTime;
    protected String APP_ID;

    public Transmit(DTNHost host, int pingSize, int webPageNumber, String APP_ID, double pageRequestCreationTime){
        this.host = host;
        this.pingSize = pingSize;
        this.webPageNumber = webPageNumber;
        this.pageRequestCreationTime = pageRequestCreationTime;
        this.APP_ID = APP_ID;
    }


    public abstract int transmitNewPageRequest();

    protected void transmitMessageTo(DTNHost transmitTo){
        //System.out.println("Transmit: Send Message from "+host.getName()+" to: "+transmitTo.getName());
        Message m = new Message(host, transmitTo, "ping" +
                SimClock.getIntTime() + "-" + host.getAddress(),
                pingSize);
        m.addProperty("type", "ping");
        m.addProperty("webpageNumber", webPageNumber);
        //m.addProperty("pageRequestCreationTime", pageRequestCreationTime);
        m.setPageRequestCreationTime(pageRequestCreationTime);
        m.setAppID(APP_ID);
        host.createNewMessage(m);
    }
    public abstract void newConnectionHotSpot(Connection con);
    public abstract void newConnectionPedestrian(Connection con);
}
