/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;

import java.util.ArrayList;
import java.util.List;

public class PageRequestRouter extends ActiveRouter {

	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public PageRequestRouter(Settings s) {
		super(s);
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected PageRequestRouter(PageRequestRouter r) {
		super(r);
	}


	/* TODO: Should we keep this method? */
	@Override
	protected int checkReceiving(Message m, DTNHost from) {
		int recvCheck = super.checkReceiving(m, from);

		if (recvCheck == RCV_OK) {
			/* don't accept a message that has already traversed this node */
			if (m.getHops().contains(getHost())) {
				recvCheck = DENIED_OLD;
			}
		}

		return recvCheck;
	}

	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return;
		}

		//if (exchangeDeliverableMessages() != null) {
		//	return;
		//}

		//tryAllMessagesToAllConnections();


		//here we will have a method that sends the message (request)
		sendAllMessagesToTheirDestinations();
	}


	protected Connection sendAllMessagesToTheirDestinations() {
        List<Connection> connections = getConnections();

        if (connections.size() == 0 || this.getNrofMessages() == 0) {
            return null;
        }

        List<Message> messages = new ArrayList<Message>(this.getMessageCollection());
        this.sortByQueueMode(messages);//TODO: Do we need this????

        for (int i = 0, n = messages.size(); i < n; i++) {
            Message msg = messages.get(i);

            for (int j = 0, m = connections.size(); j < m; j++) {
                Connection con = connections.get(j);

                if(msg.getTo().getAddress() == con.getOtherNode(msg.getFrom()).getAddress()) {
                    int retVal = startTransfer(msg, con);
                    if (retVal == RCV_OK) {
                        // accepted a message, don't try others
                    }
                    else if (retVal > 0) {
                        // should try later -> don't bother trying others
                    }

                    break;
                }
            }
        }

		return null;
	}

	/* TODO: Should we keep this method? */
	@Override
	protected void transferDone(Connection con) {
		/* don't leave a copy for the sender */
		this.deleteMessage(con.getMessage().getId(), false);
	}

	@Override
	public PageRequestRouter replicate() {
		return new PageRequestRouter(this);
	}

}
