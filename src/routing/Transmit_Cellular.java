package routing;

import core.*;

import java.util.concurrent.TransferQueue;

/**
 * Created by Matthias on 26.01.2016.
 */
public class Transmit_Cellular extends Transmit {

    public Transmit_Cellular(DTNHost host, int pingSize, int webPageNumber, String APP_ID){
        super(host, pingSize, webPageNumber, APP_ID);
    }

    @Override
    public void transmitNewPageRequest() {
        transmitMessageTo(getCellularHost());
    }

    @Override
    public void newConnectionHotSpot(Connection con){
    }

    @Override
    public void newConnectionPedestrian(Connection con){
    }


    private int cellularHost = 0;
    private DTNHost getCellularHost(){
        World w = SimScenario.getInstance().getWorld();
        return w.getNodeByAddress(cellularHost);
    }

}
