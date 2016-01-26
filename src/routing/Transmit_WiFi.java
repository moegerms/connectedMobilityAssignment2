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
    public void transmitNewPageRequest() {
        for (DTNHost hotSpot : host.getHotSpotConnections()) {
            transmitMessageTo(hotSpot);
        }
    }


    @Override
    public void newConnectionHotSpot(Connection con){
        transmitMessageTo(con.getOtherNode(host));
    }

    @Override
    public void newConnectionPedestrian(Connection con){
    }




}
