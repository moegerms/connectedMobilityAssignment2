package routing;

import core.*;

/**
 * Created by Matthias on 26.01.2016.
 */
public class Transmit_WiFi extends Transmit {

    public Transmit_WiFi(DTNHost host, int pingSize, int webPageNumber, String APP_ID){
        super(host, pingSize, webPageNumber, APP_ID);
    }

    @Override
    public int transmitNewPageRequest() {
        int returnValue = 0;

        for (DTNHost hotSpot : host.getHotSpotConnections()) {
            transmitMessageTo(hotSpot);
            //System.out.println("Transmit: Send Message from "+host.getName()+" to: "+hotSpot.getName());
            returnValue++;
        }

        return returnValue;
    }


    @Override
    public void newConnectionHotSpot(Connection con){
        transmitMessageTo(con.getOtherNode(host));
    }

    @Override
    public void newConnectionPedestrian(Connection con){
    }




}
