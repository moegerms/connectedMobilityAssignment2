package routing;

import core.Connection;
import core.DTNHost;

/**
 * Created by Matthias on 26.01.2016.
 */
public class Transmit_Pedestrian extends Transmit {

    public Transmit_Pedestrian(DTNHost host, int pingSize, int webPageNumber, String APP_ID){
        super(host, pingSize, webPageNumber, APP_ID);
    }

    @Override
    public int transmitNewPageRequest() {
        int returnValue = 0;

        for (DTNHost pedestrian : host.getPedestrianConnections()) {
            transmitMessageTo(pedestrian);
            returnValue++;
        }

        return returnValue;
    }


    @Override
    public void newConnectionHotSpot(Connection con){
    }

    @Override
    public void newConnectionPedestrian(Connection con){
        transmitMessageTo(con.getOtherNode(host));
    }




}
