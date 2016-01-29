/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import applications.PingApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class PingAppReporter extends Report implements ApplicationListener {

	private ArrayList<Double> responseTimes;

	private int pingsSent=0, pingsReceived=0;
	private int pongsSent=0, pongsReceived=0;

    private int pongsReceivedWiFi = 0;
    private int pongsReceivedCellular = 0;
    private int pongsReceivedPedestrian = 0;

    private int pingsSentWiFi = 0;
    private int bytesSentWiFi = 0;
    private int pingsSentPedestrian = 0;
    private int bytesSentPedestrian = 0;


	public PingAppReporter() {
		super();

		responseTimes = new ArrayList<Double>();
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host, double responseTime, DTNHost.TypeOfHost typeOfOtherHost) {
		// Check that the event is sent by correct application type
		if (!(app instanceof PingApplication)) return;

		// Increment the counters based on the event type
		if (event.equalsIgnoreCase("GotPing")) {
			pingsReceived++;
		}
		if (event.equalsIgnoreCase("SentPong")) {
			pongsSent++;
		}
		if (event.equalsIgnoreCase("GotPong")) {
			pongsReceived++;
            responseTimes.add(responseTime);

            switch(typeOfOtherHost) {
                case CELLULAR_BASE:
                    pongsReceivedCellular++;
                    break;
                case WIFI_HOTSPOT:
                    pongsReceivedWiFi++;
                    break;
                case REGULAR_HOST:
                    pongsReceivedPedestrian++;
                    break;
                default:
                    break;
            }
		}
		if (event.equalsIgnoreCase("SentPing")) {
			pingsSent++;

            if(typeOfOtherHost != null) {
                switch (typeOfOtherHost) {
                    case WIFI_HOTSPOT:
                        pingsSentWiFi++;
                        break;
                    case REGULAR_HOST:
                        pingsSentPedestrian++;
                        break;
                    default:
                        //System.out.println("Not Null");
                        break;
                }
            }
            //else
            //    System.out.println("NULL");
        }

	}

	private double getMeanResponseTime() {
        double sum = 0.0d;

        for(Double d : responseTimes){
            sum += d;
        }

        return sum /((double) responseTimes.size());
	}

	private double getMedianResponseTime() {
        //Collections.sort(responseTimes);

        int medianIndex = responseTimes.size()/2;

        return responseTimes.get(medianIndex);
    }

	private double get95TileResponseTime() {
        //Collections.sort(responseTimes);

        int _95PercentileIndex = (int)(responseTimes.size() * 0.95);

        return responseTimes.get(_95PercentileIndex);
	}

	private double getMinResponseTime() {
        return responseTimes.get(0);
	}

	private double getMaxResponseTime() {
        return responseTimes.get(responseTimes.size() - 1);
	}


    private String getWiFiOffloadingShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedWiFi)/((double) pongsReceived) * 100.0d) + " %";
    }

    private String getWiFiOffloadingRatio() {
        return pongsReceivedWiFi + " / " + pongsReceived;
    }


    private String getPedestrianOffloadingShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedPedestrian)/((double) pongsReceived) * 100.0d) + " %";
    }

    private String getPedestrianOffloadingRatio() {
        return pongsReceivedPedestrian + " / " + pongsReceived;
    }

    private String getPedestrianToWiFiOffloadingShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedPedestrian)/((double) pongsReceivedWiFi) * 100.0d) + " %";
    }

    private String getPedestrianToWiFiOffloadingRatio() {
        return pongsReceivedPedestrian + " / " + pongsReceivedWiFi;
    }


	@Override
	public void done() {
		write("Ping stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		double pingProb = 0; // ping probability
		double pongProb = 0; // pong probability
		double successProb = 0;	// success probability

		if (this.pingsSent > 0) {
			pingProb = (1.0 * this.pingsReceived) / this.pingsSent;
		}
		if (this.pongsSent > 0) {
			pongProb = (1.0 * this.pongsReceived) / this.pongsSent;
		}
		if (this.pingsSent > 0) {
			successProb = (1.0 * this.pongsReceived) / this.pingsSent;
		}

		String statsText = "pings sent: " + this.pingsSent +
			"\npings received: " + this.pingsReceived +
			"\npongs sent: " + this.pongsSent +
			"\npongs received: " + this.pongsReceived +
			"\nping delivery prob: " + format(pingProb) +
			"\npong delivery prob: " + format(pongProb) +
			"\nping/pong success prob: " + format(successProb)
			;

		write(statsText);

        if(responseTimes.size() > 0) {

            /* Response Times stats */
            write("\nResponse Times Statistics\n");

            Collections.sort(responseTimes);

            statsText = "mean response time: " + this.getMeanResponseTime() +
                    "\nmedian response time: " + this.getMedianResponseTime() +
                    "\n95%-tile response time: " + this.get95TileResponseTime() +
                    "\nmin response time: " + this.getMinResponseTime() +
                    "\nmax response time: " + this.getMaxResponseTime()
                    ;

            write(statsText);

        }

        /* Offloading stats */
        write("\nOffloading Statistics\n");

        statsText = "#responses Cellular: " + this.pongsReceivedCellular +
                "\n#responses WiFi: " + this.pongsReceivedWiFi +
                "\n#responses Pedestrian: " + this.pongsReceivedPedestrian +
                "\n#offloading share WiFi: " + this.getWiFiOffloadingShare() +
                "\n#offloading ratio WiFi: " + this.getWiFiOffloadingRatio() +
                "\n#offloading share Pedestrian: " + this.getPedestrianOffloadingShare() +
                "\n#offloading ratio Pedestrian: " + this.getPedestrianOffloadingRatio() +
                "\n#offloading share Pedestrian: " + this.getPedestrianToWiFiOffloadingShare() +
                "\n#offloading ratio Pedestrian: " + this.getPedestrianToWiFiOffloadingRatio() +
                "\n#requests WiFi: " + this.pingsSentWiFi +
                "\n#requests Pedestrian: " + this.pingsSentPedestrian
        ;

        write(statsText);

		super.done();
	}
}
