package routing;

import core.*;

import java.util.concurrent.TransferQueue;

/**
 * Created by Matthias on 26.01.2016.
 */
public class Transmit_Cellular extends Transmit {

    public Transmit_Cellular(DTNHost host, int pingSize, int webPageNumber, String APP_ID, double pageRequestCreationTime){
        super(host, pingSize, webPageNumber, APP_ID, pageRequestCreationTime);
    }

    @Override
    public int transmitNewPageRequest() {
        transmitMessageTo(getCellularHost());

        return 1;
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
