/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

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

	private ArrayList<Double> roundTripTimes;
    private ArrayList<Double> responseTimes;
    private TreeMap<Integer,Long> individualNumOfRequestsWiFi;
    private TreeMap<Integer,Double> individualShareRequestsWiFi;
    private TreeMap<Integer,Long> individualNumOfRequestsCellular;
    private TreeMap<Integer,Double> individualShareRequestsCellular;
    private TreeMap<Integer,Long> individualNumOfRequestsPedestrian;
    private TreeMap<Integer,Double> individualShareRequestsPedestrian;
    private TreeMap<Integer,Long> individualNumOfRequestsAggregate;
    private TreeMap<Integer,Long> individualNumOfResponsesWiFi;
    private TreeMap<Integer,Double> individualShareResponsesWiFi;
    private TreeMap<Integer,Long> individualNumOfResponsesPedestrian;
    private TreeMap<Integer,Double> individualShareResponsesPedestrian;
    private TreeMap<Integer,Long> individualNumOfResponsesCellular;
    private TreeMap<Integer,Double> individualShareResponsesCellular;
    private TreeMap<Integer,Long> individualNumOfResponsesAggregate;
    private TreeMap<Integer,Long> individualNumOfRequestBytesWiFi;
    private TreeMap<Integer,Double> individualShareRequestBytesWiFi;
    private TreeMap<Integer,Long> individualNumOfRequestBytesPedestrian;
    private TreeMap<Integer,Double> individualShareRequestBytesPedestrian;
    private TreeMap<Integer,Long> individualNumOfRequestBytesCellular;
    private TreeMap<Integer,Double> individualShareRequestBytesCellular;
    private TreeMap<Integer,Long> individualNumOfRequestBytesAggregate;
    private TreeMap<Integer,Long> individualNumOfResponseBytesWiFi;
    private TreeMap<Integer,Double> individualShareResponseBytesWiFi;
    private TreeMap<Integer,Long> individualNumOfResponseBytesPedestrian;
    private TreeMap<Integer,Double> individualShareResponseBytesPedestrian;
    private TreeMap<Integer,Long> individualNumOfResponseBytesCellular;
    private TreeMap<Integer,Double> individualShareResponseBytesCellular;
    private TreeMap<Integer,Long> individualNumOfResponseBytesAggregate;

	private int pingsSent=0, pingsReceived=0;
	private int pongsSent=0, pongsReceived=0;
    private int foundInLocalCache=0;
    private long totalBytesReceived = 0;
    private long totalBytesSent = 0;

    private int pongsReceivedWiFi = 0;
    private long bytesReceivedWiFi = 0;
    private int pongsReceivedCellular = 0;
    private long bytesReceivedCellular = 0;
    private int pongsReceivedPedestrian = 0;
    private long bytesReceivedPedestrian = 0;

    private int pingsSentWiFi = 0;
    private long bytesSentWiFi = 0;
    private int pingsSentCellular = 0;
    private long bytesSentCellular = 0;
    private int pingsSentPedestrian = 0;
    private long bytesSentPedestrian = 0;


	public PingAppReporter() {
		super();

		roundTripTimes = new ArrayList<Double>();
        responseTimes = new ArrayList<Double>();
        individualNumOfRequestsWiFi = new TreeMap<Integer,Long>();
        individualShareRequestsWiFi = new TreeMap<Integer,Double>();
        individualNumOfRequestsPedestrian = new TreeMap<Integer,Long>();
        individualShareRequestsPedestrian = new TreeMap<Integer,Double>();
        individualNumOfRequestsCellular = new TreeMap<Integer,Long>();
        individualShareRequestsCellular = new TreeMap<Integer,Double>();

        individualNumOfResponsesWiFi = new TreeMap<Integer,Long>();
        individualShareResponsesWiFi = new TreeMap<Integer,Double>();
        individualNumOfResponsesPedestrian = new TreeMap<Integer,Long>();
        individualShareResponsesPedestrian = new TreeMap<Integer,Double>();
        individualNumOfResponsesCellular = new TreeMap<Integer,Long>();
        individualShareResponsesCellular = new TreeMap<Integer,Double>();

        individualNumOfRequestBytesWiFi = new TreeMap<Integer,Long>();
        individualShareRequestBytesWiFi = new TreeMap<Integer,Double>();
        individualNumOfRequestBytesPedestrian = new TreeMap<Integer,Long>();
        individualShareRequestBytesPedestrian = new TreeMap<Integer,Double>();
        individualNumOfRequestBytesCellular = new TreeMap<Integer,Long>();
        individualShareRequestBytesCellular = new TreeMap<Integer,Double>();

        individualNumOfResponseBytesWiFi = new TreeMap<Integer,Long>();
        individualShareResponseBytesWiFi = new TreeMap<Integer,Double>();
        individualNumOfResponseBytesPedestrian = new TreeMap<Integer,Long>();
        individualShareResponseBytesPedestrian = new TreeMap<Integer,Double>();
        individualNumOfResponseBytesCellular = new TreeMap<Integer,Long>();
        individualShareResponseBytesCellular = new TreeMap<Integer,Double>();
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host, double roundTripTime, DTNHost.TypeOfHost typeOfOtherHost, int messageSize, double responseTime) {
		// Check that the event is sent by correct application type
		if (!(app instanceof PingApplication)) return;

        if (isWarmup())
            return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("InLocalCache")) {
            foundInLocalCache++;
        }
		if (event.equalsIgnoreCase("GotPing")) {
			pingsReceived++;
		}
		if (event.equalsIgnoreCase("SentPong")) {
			pongsSent++;
		}
		if (event.equalsIgnoreCase("GotPong")) {
			pongsReceived++;
            totalBytesReceived += messageSize;
            roundTripTimes.add(roundTripTime);
            responseTimes.add(responseTime);

            switch(typeOfOtherHost) {
                case CELLULAR_BASE:
                    pongsReceivedCellular++;
                    bytesReceivedCellular += messageSize;

                    if(individualNumOfResponsesCellular.containsKey(host.getAddress()))
                        individualNumOfResponsesCellular.put(host.getAddress(), individualNumOfResponsesCellular.get(host.getAddress()) + 1);
                    else
                        individualNumOfResponsesCellular.put(host.getAddress(), 1L);

                    if(individualNumOfResponseBytesCellular.containsKey(host.getAddress()))
                        individualNumOfResponseBytesCellular.put(host.getAddress(), individualNumOfResponseBytesCellular.get(host.getAddress()) + messageSize);
                    else
                        individualNumOfResponseBytesCellular.put(host.getAddress(), (long) messageSize);

                    break;
                case WIFI_HOTSPOT:
                    pongsReceivedWiFi++;
                    bytesReceivedWiFi += messageSize;

                    if(individualNumOfResponsesWiFi.containsKey(host.getAddress()))
                        individualNumOfResponsesWiFi.put(host.getAddress(), individualNumOfResponsesWiFi.get(host.getAddress()) + 1);
                    else
                        individualNumOfResponsesWiFi.put(host.getAddress(), 1L);

                    if(individualNumOfResponseBytesWiFi.containsKey(host.getAddress()))
                        individualNumOfResponseBytesWiFi.put(host.getAddress(), individualNumOfResponseBytesWiFi.get(host.getAddress()) + messageSize);
                    else
                        individualNumOfResponseBytesWiFi.put(host.getAddress(), (long) messageSize);

                    break;
                case REGULAR_HOST:
                    pongsReceivedPedestrian++;
                    bytesReceivedPedestrian += messageSize;

                    if(individualNumOfResponsesPedestrian.containsKey(host.getAddress()))
                        individualNumOfResponsesPedestrian.put(host.getAddress(), individualNumOfResponsesPedestrian.get(host.getAddress()) + 1);
                    else
                        individualNumOfResponsesPedestrian.put(host.getAddress(), 1L);

                    if(individualNumOfResponseBytesPedestrian.containsKey(host.getAddress()))
                        individualNumOfResponseBytesPedestrian.put(host.getAddress(), individualNumOfResponseBytesPedestrian.get(host.getAddress()) + messageSize);
                    else
                        individualNumOfResponseBytesPedestrian.put(host.getAddress(), (long) messageSize);

                    break;
                default:
                    break;
            }
		}
		if (event.equalsIgnoreCase("SentPing")) {
			pingsSent++;
            totalBytesSent += messageSize;

            if(typeOfOtherHost != null) {
                switch (typeOfOtherHost) {
                    case CELLULAR_BASE:
                        pingsSentCellular++;
                        bytesSentCellular += messageSize;

                        if(individualNumOfRequestsCellular.containsKey(host.getAddress()))
                            individualNumOfRequestsCellular.put(host.getAddress(), individualNumOfRequestsCellular.get(host.getAddress()) + 1);
                        else
                            individualNumOfRequestsCellular.put(host.getAddress(), 1L);

                        if(individualNumOfRequestBytesCellular.containsKey(host.getAddress()))
                            individualNumOfRequestBytesCellular.put(host.getAddress(), individualNumOfRequestBytesCellular.get(host.getAddress()) + messageSize);
                        else
                            individualNumOfRequestBytesCellular.put(host.getAddress(), (long) messageSize);

                        break;
                    case WIFI_HOTSPOT:
                        pingsSentWiFi++;
                        bytesSentWiFi += messageSize;

                        if(individualNumOfRequestsWiFi.containsKey(host.getAddress()))
                            individualNumOfRequestsWiFi.put(host.getAddress(), individualNumOfRequestsWiFi.get(host.getAddress()) + 1);
                        else
                            individualNumOfRequestsWiFi.put(host.getAddress(), 1L);

                        if(individualNumOfRequestBytesWiFi.containsKey(host.getAddress()))
                            individualNumOfRequestBytesWiFi.put(host.getAddress(), individualNumOfRequestBytesWiFi.get(host.getAddress()) + messageSize);
                        else
                            individualNumOfRequestBytesWiFi.put(host.getAddress(), (long) messageSize);

                        break;
                    case REGULAR_HOST:
                        pingsSentPedestrian++;
                        bytesSentPedestrian += messageSize;

                        if(individualNumOfRequestsPedestrian.containsKey(host.getAddress()))
                            individualNumOfRequestsPedestrian.put(host.getAddress(), individualNumOfRequestsPedestrian.get(host.getAddress()) + 1);
                        else
                            individualNumOfRequestsPedestrian.put(host.getAddress(), 1L);

                        if(individualNumOfRequestBytesPedestrian.containsKey(host.getAddress()))
                            individualNumOfRequestBytesPedestrian.put(host.getAddress(), individualNumOfRequestBytesPedestrian.get(host.getAddress()) + messageSize);
                        else
                            individualNumOfRequestBytesPedestrian.put(host.getAddress(), (long) messageSize);

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

	private double getMeanDouble(ArrayList<Double> values) {
        double sum = 0.0d;

        for(Double d : values){
            sum += d;
        }

        return sum /((double) values.size());
	}

    private double getMean(ArrayList<Long> values) {
        long sum = 0L;

        for(Long l : values){
            sum += l;
        }

        return sum /((double) values.size());
    }

	private double getMedianDouble(ArrayList<Double> values) {
        //Collections.sort(roundTripTimes);

        int medianIndex = values.size()/2;

        return values.get(medianIndex);
    }

    private long getMedian(ArrayList<Long> values) {
        //Collections.sort(roundTripTimes);

        int medianIndex = values.size()/2;

        return values.get(medianIndex);
    }

	private double get95TileDouble(ArrayList<Double> values) {
        //Collections.sort(roundTripTimes);

        int _95PercentileIndex = (int)(values.size() * 0.95);

        return values.get(_95PercentileIndex);
	}

    private long get95Tile(ArrayList<Long> values) {
        //Collections.sort(roundTripTimes);

        int _95PercentileIndex = (int)(values.size() * 0.95);

        return values.get(_95PercentileIndex);
    }

	private double getMinDouble(ArrayList<Double> values) {
        return values.get(0);
	}

    private long getMin(ArrayList<Long> values) {
        return values.get(0);
    }

	private double getMaxDouble(ArrayList<Double> values) {
        return values.get(values.size() - 1);
	}

    private long getMax(ArrayList<Long> values) {
        return values.get(values.size() - 1);
    }



    private String getWiFiOffloadingRequestsShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pingsSentWiFi)/((double) pingsSent) * 100.0d) + " %";
    }

    private String getWiFiOffloadingResponsesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedWiFi)/((double) pongsReceived) * 100.0d) + " %";
    }

    private String getWiFiOffloadingRequestBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesSentWiFi)/((double) totalBytesSent) * 100.0d) + " %";
    }

    private String getWiFiOffloadingResponseBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesReceivedWiFi)/((double) totalBytesReceived) * 100.0d) + " %";
    }

    private String getWiFiOffloadingRequestsRatio() {
        return pingsSentWiFi + " / " + pingsSent;
    }

    private String getWiFiOffloadingResponsesRatio() {
        return pongsReceivedWiFi + " / " + pongsReceived;
    }

    private String getWiFiOffloadingRequestBytesRatio() {
        return bytesSentWiFi + " / " + totalBytesSent;
    }

    private String getWiFiOffloadingResponseBytesRatio() {
        return bytesReceivedWiFi + " / " + totalBytesReceived;
    }



    private String getPedestrianOffloadingRequestsShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pingsSentPedestrian)/((double) pingsSent) * 100.0d) + " %";
    }

    private String getPedestrianOffloadingResponsesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedPedestrian)/((double) pongsReceived) * 100.0d) + " %";
    }

    private String getPedestrianOffloadingRequestBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesSentPedestrian)/((double) totalBytesSent) * 100.0d) + " %";
    }

    private String getPedestrianOffloadingResponseBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesReceivedPedestrian)/((double) totalBytesReceived) * 100.0d) + " %";
    }

    private String getPedestrianOffloadingRequestsRatio() {
        return pingsSentPedestrian + " / " + pingsSent;
    }

    private String getPedestrianOffloadingResponsesRatio() {
        return pongsReceivedPedestrian + " / " + pongsReceived;
    }

    private String getPedestrianOffloadingRequestBytesRatio() {return bytesSentPedestrian + " / " + totalBytesSent;}

    private String getPedestrianOffloadingResponseBytesRatio() {return bytesReceivedPedestrian + " / " + totalBytesReceived;}



    private String getPedestrianToWiFiOffloadingRequestsShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pingsSentPedestrian)/((double) pingsSentWiFi) * 100.0d) + " %";
    }

    private String getPedestrianToWiFiOffloadingResponsesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) pongsReceivedPedestrian)/((double) pongsReceivedWiFi) * 100.0d) + " %";
    }

    private String getPedestrianToWiFiOffloadingRequestBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesSentPedestrian)/((double) bytesSentWiFi) * 100.0d) + " %";
    }

    private String getPedestrianToWiFiOffloadingResponseBytesShare() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(((double) bytesReceivedPedestrian)/((double) bytesReceivedWiFi) * 100.0d) + " %";
    }

    private String getPedestrianToWiFiOffloadingRequestsRatio() {return pingsSentPedestrian + " / " + pingsSentWiFi;}

    private String getPedestrianToWiFiOffloadingResponsesRatio() {return pongsReceivedPedestrian + " / " + pongsReceivedWiFi;}

    private String getPedestrianToWiFiOffloadingRequestBytesRatio() {return bytesSentPedestrian + " / " + bytesSentWiFi;}

    private String getPedestrianToWiFiOffloadingResponseBytesRatio() {return bytesReceivedPedestrian + " / " + bytesReceivedWiFi;}


    private TreeMap<Integer,Double> calculateShares(TreeMap<Integer,Long> offloadValues, TreeMap<Integer,Long> aggregateValues) {
        TreeMap<Integer,Double> results = new TreeMap<Integer,Double>();

        if(offloadValues.keySet().size() != aggregateValues.keySet().size())
            System.out.println("ERROR: Not equal sizes.");
        else {
            for(Integer i : offloadValues.keySet()) {
                results.put(i, ((double) offloadValues.get(i))/((double) aggregateValues.get(i)) * 100.0d);
            }
        }

        return results;
    }







	@Override
	public void done() {
		write("Ping stats for scenario " + getScenarioName() + "\n--------------------------------\n" +
				"\nsimulation time: " + format(getSimTime()));
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
			"\nping delivery probability: " + format(pingProb) +
			"\npong delivery probability: " + format(pongProb) +
			"\nping/pong success probability: " + format(successProb) + "\n" +
            "\nbytes sent: " + this.totalBytesSent +
            "\nbytes received: " + this.totalBytesReceived +
            "\npages found in local cache: " + this.foundInLocalCache + "\n"
			;

		write(statsText);

        if(roundTripTimes.size() > 0) {

            /* Round Trip Times stats */
            write("\nRound Trip Times Statistics\n" + "--------------------------------\n");

            Collections.sort(roundTripTimes);

            statsText = "mean round trip time: " + this.getMeanDouble(roundTripTimes) + " secs" +
                      "\nmedian round trip time: " + this.getMedianDouble(roundTripTimes) + " secs" +
                      "\n95%-tile round trip time: " + this.get95TileDouble(roundTripTimes) + " secs" +
                      "\nmin round trip time: " + this.getMinDouble(roundTripTimes) + " secs" +
                      "\nmax round trip time: " + this.getMaxDouble(roundTripTimes) + " secs\n"
                    ;

            write(statsText);

        }


        if(responseTimes.size() > 0) {

            /* Response Times stats */
            write("\nResponse Times Statistics\n" + "--------------------------------\n");

            Collections.sort(responseTimes);

            statsText = "mean response time: " + this.getMeanDouble(responseTimes) + " secs" +
                    "\nmedian response time: " + this.getMedianDouble(responseTimes) + " secs" +
                    "\n95%-tile response time: " + this.get95TileDouble(responseTimes) + " secs" +
                    "\nmin response time: " + this.getMinDouble(responseTimes) + " secs" +
                    "\nmax response time: " + this.getMaxDouble(responseTimes) + " secs\n"
            ;

            write(statsText);
        }




        /* Offloading stats - Overall - Requests*/
        write("\nOffloading Statistics (Overall) - Requests\n" + "--------------------------------\n");

        statsText = "#requests Cellular: " + this.pingsSentCellular +
                  "\n#requests WiFi: " + this.pingsSentWiFi +
                  "\n#requests Pedestrian: " + this.pingsSentPedestrian +

                  "\n#offloading request share WiFi: " + this.getWiFiOffloadingRequestsShare() +
                  "\n#offloading request ratio WiFi: " + this.getWiFiOffloadingRequestsRatio() +
                  "\n#offloading request share Pedestrian: " + this.getPedestrianOffloadingRequestsShare() +
                  "\n#offloading request ratio Pedestrian: " + this.getPedestrianOffloadingRequestsRatio() +
                  "\n#offloading request share Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingRequestsShare() +
                  "\n#offloading request ratio Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingRequestsRatio() + "\n"
        ;

        write(statsText);



        /* Offloading stats - Overall - Request Bytes*/
        write("\nOffloading Statistics (Overall) - Request Bytes\n" + "--------------------------------\n");

        statsText = "#request bytes Cellular: " + this.bytesSentCellular +
                  "\n#request bytes WiFi: " + this.bytesSentWiFi +
                  "\n#request bytes Pedestrian: " + this.bytesSentPedestrian +

                  "\n#offloading request bytes share WiFi: " + this.getWiFiOffloadingRequestBytesShare() +
                  "\n#offloading request bytes ratio WiFi: " + this.getWiFiOffloadingRequestBytesRatio() +
                  "\n#offloading request bytes share Pedestrian: " + this.getPedestrianOffloadingRequestBytesShare() +
                  "\n#offloading request bytes ratio Pedestrian: " + this.getPedestrianOffloadingRequestBytesRatio() +
                  "\n#offloading request bytes share Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingRequestBytesShare() +
                  "\n#offloading request bytes ratio Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingRequestBytesRatio() + "\n"
        ;

        write(statsText);



        /* Offloading stats - Overall - Responses*/
        write("\nOffloading Statistics (Overall) - Responses\n" + "--------------------------------\n");

        statsText = "#responses Cellular: " + this.pongsReceivedCellular +
                "\n#responses WiFi: " + this.pongsReceivedWiFi +
                "\n#responses Pedestrian: " + this.pongsReceivedPedestrian +

                "\n#offloading response share WiFi: " + this.getWiFiOffloadingResponsesShare() +
                "\n#offloading response ratio WiFi: " + this.getWiFiOffloadingResponsesRatio() +
                "\n#offloading response share Pedestrian: " + this.getPedestrianOffloadingResponsesShare() +
                "\n#offloading response ratio Pedestrian: " + this.getPedestrianOffloadingResponsesRatio() +
                "\n#offloading response share Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingResponsesShare() +
                "\n#offloading response ratio Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingResponsesRatio() + "\n"
        ;

        write(statsText);


        /* Offloading stats - Overall - Response Bytes*/
        write("\nOffloading Statistics (Overall) - Response Bytes\n" + "--------------------------------\n");

        statsText = "#response bytes Cellular: " + this.bytesReceivedCellular +
                "\n#response bytes WiFi: " + this.bytesReceivedWiFi +
                "\n#response bytes Pedestrian: " + this.bytesReceivedPedestrian + "\n" +

                "\n#offloading response bytes share WiFi: " + this.getWiFiOffloadingResponseBytesShare() +
                "\n#offloading response bytes ratio WiFi: " + this.getWiFiOffloadingResponseBytesRatio() +
                "\n#offloading response bytes share Pedestrian: " + this.getPedestrianOffloadingResponseBytesShare() +
                "\n#offloading response bytes ratio Pedestrian: " + this.getPedestrianOffloadingResponseBytesRatio() +
                "\n#offloading response bytes share Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingResponseBytesShare() +
                "\n#offloading response bytes ratio Pedestrian to WiFi: " + this.getPedestrianToWiFiOffloadingResponseBytesRatio() + "\n"
        ;

        write(statsText);


        /* Offloading stats - Per Node - Requests*/
        //requests WiFi,Ped,Cell,Aggregate
        write("\nOffloading Statistics (Per Node) - Requests\n" + "--------------------------------\n");

        //Calculate Aggregate Numbers
        individualNumOfRequestsAggregate = new TreeMap<Integer,Long>(individualNumOfRequestsCellular);

        for(Integer i : individualNumOfRequestsWiFi.keySet()) {
            if(individualNumOfRequestsAggregate.containsKey(i))
                individualNumOfRequestsAggregate.put(i, individualNumOfRequestsAggregate.get(i) + individualNumOfRequestsWiFi.get(i));
            else
                individualNumOfRequestsAggregate.put(i, individualNumOfRequestsWiFi.get(i));
        }

        for(Integer i : individualNumOfRequestsPedestrian.keySet()) {
            if(individualNumOfRequestsAggregate.containsKey(i))
                individualNumOfRequestsAggregate.put(i, individualNumOfRequestsAggregate.get(i) + individualNumOfRequestsPedestrian.get(i));
            else
                individualNumOfRequestsAggregate.put(i, individualNumOfRequestsPedestrian.get(i));
        }

        //Fill any missing info for nodes with 0.
        for(Integer i : individualNumOfRequestsAggregate.keySet()) {
            if(!individualNumOfRequestsPedestrian.containsKey(i)){
                individualNumOfRequestsPedestrian.put(i, 0L);
            }
            if(!individualNumOfRequestsWiFi.containsKey(i)){
                individualNumOfRequestsWiFi.put(i, 0L);
            }
            if(!individualNumOfRequestsCellular.containsKey(i)){
                individualNumOfRequestsCellular.put(i, 0L);
            }
        }

        //Calculate Shares
        individualShareRequestsWiFi = calculateShares(individualNumOfRequestsWiFi, individualNumOfRequestsAggregate);
        individualShareRequestsCellular = calculateShares(individualNumOfRequestsCellular, individualNumOfRequestsAggregate);
        individualShareRequestsPedestrian = calculateShares(individualNumOfRequestsPedestrian, individualNumOfRequestsAggregate);


        //Sort Lists of Values
        ArrayList<Long> valuesRequestsWiFi = new ArrayList<Long>(individualNumOfRequestsWiFi.values());
        Collections.sort(valuesRequestsWiFi);
        ArrayList<Double> valuesSharesRequestsWiFi = new ArrayList<Double>(individualShareRequestsWiFi.values());
        Collections.sort(valuesSharesRequestsWiFi);
        ArrayList<Long> valuesRequestsCellular = new ArrayList<Long>(individualNumOfRequestsCellular.values());
        Collections.sort(valuesRequestsCellular);
        ArrayList<Double> valuesSharesRequestsCellular = new ArrayList<Double>(individualShareRequestsCellular.values());
        Collections.sort(valuesSharesRequestsCellular);
        ArrayList<Long> valuesRequestsPedestrian = new ArrayList<Long>(individualNumOfRequestsPedestrian.values());
        Collections.sort(valuesRequestsPedestrian);
        ArrayList<Double> valuesSharesRequestsPedestrian = new ArrayList<Double>(individualShareRequestsPedestrian.values());
        Collections.sort(valuesSharesRequestsPedestrian);
        ArrayList<Long> valuesRequestsAggregate = new ArrayList<Long>(individualNumOfRequestsAggregate.values());
        Collections.sort(valuesRequestsAggregate);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        //System.out.println(individualNumOfRequestsWiFi);
        //System.out.println(individualNumOfRequestsAggregate);
        statsText = "\nmean requests Cellular: " + this.getMean(valuesRequestsCellular) + "  requests per node" +
                    "\nmean request share Cellular: " + df.format(this.getMeanDouble(valuesSharesRequestsCellular)) + " %  (mean of individual shares)" +
                    "\nmedian requests Cellular: " + this.getMedian(valuesRequestsCellular) + "  requests per node" +
                    "\nmedian request share Cellular: " + df.format(this.getMedianDouble(valuesSharesRequestsCellular)) + " %" +
                    "\n95%-tile requests Cellular: " + this.get95Tile(valuesRequestsCellular) +
                    "\n95%-tile request share Cellular: " + df.format(this.get95TileDouble(valuesSharesRequestsCellular)) + " %" +
                    "\nmin requests Cellular: " + this.getMin(valuesRequestsCellular) +
                    "\nmin request share Cellular: " + df.format(this.getMinDouble(valuesSharesRequestsCellular)) + " %" +
                    "\nmax requests Cellular: " + this.getMax(valuesRequestsCellular) +
                    "\nmax request share Cellular: " + df.format(this.getMaxDouble(valuesSharesRequestsCellular)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading requests WiFi: " + this.getMean(valuesRequestsWiFi) +
                "\nmean offloading request share WiFi: " + df.format(this.getMeanDouble(valuesSharesRequestsWiFi)) + " %  (mean of individual shares)" +
                "\nmedian offloading requests WiFi: " + this.getMedian(valuesRequestsWiFi) +
                "\nmedian offloading request share WiFi: " + df.format(this.getMedianDouble(valuesSharesRequestsWiFi)) + " %" +
                "\n95%-tile offloading requests WiFi: " + this.get95Tile(valuesRequestsWiFi) +
                "\n95%-tile offloading request share WiFi: " + df.format(this.get95TileDouble(valuesSharesRequestsWiFi)) + " %" +
                "\nmin offloading requests WiFi: " + this.getMin(valuesRequestsWiFi) +
                "\nmin offloading request share WiFi: " + df.format(this.getMinDouble(valuesSharesRequestsWiFi)) + " %" +
                "\nmax offloading requests WiFi: " + this.getMax(valuesRequestsWiFi) +
                "\nmax offloading request share WiFi: " + df.format(this.getMaxDouble(valuesSharesRequestsWiFi)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading requests Pedestrian: " + this.getMean(valuesRequestsPedestrian) +
                "\nmean offloading request share Pedestrian: " + df.format(this.getMeanDouble(valuesSharesRequestsPedestrian)) + " %" +
                "\nmedian offloading requests Pedestrian: " + this.getMedian(valuesRequestsPedestrian) +
                "\nmedian offloading request share Pedestrian: " + df.format(this.getMedianDouble(valuesSharesRequestsPedestrian)) + " %" +
                "\n95%-tile offloading requests Pedestrian: " + this.get95Tile(valuesRequestsPedestrian) +
                "\n95%-tile offloading request share Pedestrian: " + df.format(this.get95TileDouble(valuesSharesRequestsPedestrian)) + " %" +
                "\nmin offloading requests Pedestrian: " + this.getMin(valuesRequestsPedestrian) +
                "\nmin offloading request share Pedestrian: " + df.format(this.getMinDouble(valuesSharesRequestsPedestrian)) + " %" +
                "\nmax offloading requests Pedestrian: " + this.getMax(valuesRequestsPedestrian) +
                "\nmax offloading request share Pedestrian: " + df.format(this.getMaxDouble(valuesSharesRequestsPedestrian)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean requests Aggregate: " + this.getMean(valuesRequestsAggregate) + "   aggregate = cell + wifi + pedestrian      requests per node" +
                "\nmedian requests Aggregate: " + this.getMedian(valuesRequestsAggregate) +
                "\n95%-tile requests Aggregate: " + this.get95Tile(valuesRequestsAggregate) +
                "\nmin requests Aggregate: " + this.getMin(valuesRequestsAggregate) +
                "\nmax requests Aggregate: " + this.getMax(valuesRequestsAggregate) + "\n\n"
        ;

        write(statsText);








        /* Offloading stats - Per Node - Request Bytes*/
        write("\nOffloading Statistics (Per Node) - Request Bytes\n" + "--------------------------------\n");

        //Calculate Aggregate Numbers
        individualNumOfRequestBytesAggregate = new TreeMap<Integer,Long>(individualNumOfRequestBytesCellular);

        for(Integer i : individualNumOfRequestBytesWiFi.keySet()) {
            if(individualNumOfRequestBytesAggregate.containsKey(i))
                individualNumOfRequestBytesAggregate.put(i, individualNumOfRequestBytesAggregate.get(i) + individualNumOfRequestBytesWiFi.get(i));
            else
                individualNumOfRequestBytesAggregate.put(i, individualNumOfRequestBytesWiFi.get(i));
        }

        for(Integer i : individualNumOfRequestBytesPedestrian.keySet()) {
            if(individualNumOfRequestBytesAggregate.containsKey(i))
                individualNumOfRequestBytesAggregate.put(i, individualNumOfRequestBytesAggregate.get(i) + individualNumOfRequestBytesPedestrian.get(i));
            else
                individualNumOfRequestBytesAggregate.put(i, individualNumOfRequestBytesPedestrian.get(i));
        }

        //Fill any missing info for nodes with 0.
        for(Integer i : individualNumOfRequestBytesAggregate.keySet()) {
            if(!individualNumOfRequestBytesPedestrian.containsKey(i)){
                individualNumOfRequestBytesPedestrian.put(i, 0L);
            }
            if(!individualNumOfRequestBytesWiFi.containsKey(i)){
                individualNumOfRequestBytesWiFi.put(i, 0L);
            }
            if(!individualNumOfRequestBytesCellular.containsKey(i)){
                individualNumOfRequestBytesCellular.put(i, 0L);
            }
        }

        //Calculate Shares
        individualShareRequestBytesWiFi = calculateShares(individualNumOfRequestBytesWiFi, individualNumOfRequestBytesAggregate);
        individualShareRequestBytesCellular = calculateShares(individualNumOfRequestBytesCellular, individualNumOfRequestBytesAggregate);
        individualShareRequestBytesPedestrian = calculateShares(individualNumOfRequestBytesPedestrian, individualNumOfRequestBytesAggregate);

        //Sort Lists of Values
        ArrayList<Long> valuesRequestBytesWiFi = new ArrayList<Long>(individualNumOfRequestBytesWiFi.values());
        Collections.sort(valuesRequestBytesWiFi);
        ArrayList<Double> valuesSharesRequestBytesWiFi = new ArrayList<Double>(individualShareRequestBytesWiFi.values());
        Collections.sort(valuesSharesRequestBytesWiFi);
        ArrayList<Long> valuesRequestBytesCellular = new ArrayList<Long>(individualNumOfRequestBytesCellular.values());
        Collections.sort(valuesRequestBytesCellular);
        ArrayList<Double> valuesSharesRequestBytesCellular = new ArrayList<Double>(individualShareRequestBytesCellular.values());
        Collections.sort(valuesSharesRequestBytesCellular);
        ArrayList<Long> valuesRequestBytesPedestrian = new ArrayList<Long>(individualNumOfRequestBytesPedestrian.values());
        Collections.sort(valuesRequestBytesPedestrian);
        ArrayList<Double> valuesSharesRequestBytesPedestrian = new ArrayList<Double>(individualShareRequestBytesPedestrian.values());
        Collections.sort(valuesSharesRequestBytesPedestrian);
        ArrayList<Long> valuesRequestBytesAggregate = new ArrayList<Long>(individualNumOfRequestBytesAggregate.values());
        Collections.sort(valuesRequestBytesAggregate);


        statsText = "\nmean request bytes Cellular: " + this.getMean(valuesRequestBytesCellular) +
                "\nmean request bytes share Cellular: " + df.format(this.getMeanDouble(valuesSharesRequestBytesCellular)) + " %" +
                "\nmedian request bytes Cellular: " + this.getMedian(valuesRequestBytesCellular) +
                "\nmedian request bytes share Cellular: " + df.format(this.getMedianDouble(valuesSharesRequestBytesCellular)) + " %" +
                "\n95%-tile request bytes Cellular: " + this.get95Tile(valuesRequestBytesCellular) +
                "\n95%-tile request bytes share Cellular: " + df.format(this.get95TileDouble(valuesSharesRequestBytesCellular)) + " %" +
                "\nmin request bytes Cellular: " + this.getMin(valuesRequestBytesCellular) +
                "\nmin request bytes share Cellular: " + df.format(this.getMinDouble(valuesSharesRequestBytesCellular)) + " %" +
                "\nmax request bytes Cellular: " + this.getMax(valuesRequestBytesCellular) +
                "\nmax request bytes share Cellular: " + df.format(this.getMaxDouble(valuesSharesRequestBytesCellular)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading request bytes WiFi: " + this.getMean(valuesRequestBytesWiFi) +
                "\nmean offloading request bytes share WiFi: " + df.format(this.getMeanDouble(valuesSharesRequestBytesWiFi)) + " %" +
                "\nmedian offloading request bytes WiFi: " + this.getMedian(valuesRequestBytesWiFi) +
                "\nmedian offloading request bytes share WiFi: " + df.format(this.getMedianDouble(valuesSharesRequestBytesWiFi)) + " %" +
                "\n95%-tile offloading request bytes WiFi: " + this.get95Tile(valuesRequestBytesWiFi) +
                "\n95%-tile offloading request bytes share WiFi: " + df.format(this.get95TileDouble(valuesSharesRequestBytesWiFi)) + " %" +
                "\nmin offloading request bytes WiFi: " + this.getMin(valuesRequestBytesWiFi) +
                "\nmin offloading request bytes share WiFi: " + df.format(this.getMinDouble(valuesSharesRequestBytesWiFi)) + " %" +
                "\nmax offloading request bytes WiFi: " + this.getMax(valuesRequestBytesWiFi) +
                "\nmax offloading request bytes share WiFi: " + df.format(this.getMaxDouble(valuesSharesRequestBytesWiFi)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading request bytes Pedestrian: " + this.getMean(valuesRequestBytesPedestrian) +
                "\nmean offloading request bytes share Pedestrian: " + df.format(this.getMeanDouble(valuesSharesRequestBytesPedestrian)) + " %" +
                "\nmedian offloading request bytes Pedestrian: " + this.getMedian(valuesRequestBytesPedestrian) +
                "\nmedian offloading request bytes share Pedestrian: " + df.format(this.getMedianDouble(valuesSharesRequestBytesPedestrian)) + " %" +
                "\n95%-tile offloading request bytes Pedestrian: " + this.get95Tile(valuesRequestBytesPedestrian) +
                "\n95%-tile offloading request bytes share Pedestrian: " + df.format(this.get95TileDouble(valuesSharesRequestBytesPedestrian)) + " %" +
                "\nmin offloading request bytes Pedestrian: " + this.getMin(valuesRequestBytesPedestrian) +
                "\nmin offloading request bytes share Pedestrian: " + df.format(this.getMinDouble(valuesSharesRequestBytesPedestrian)) + " %" +
                "\nmax offloading request bytes Pedestrian: " + this.getMax(valuesRequestBytesPedestrian) +
                "\nmax offloading request bytes share Pedestrian: " + df.format(this.getMaxDouble(valuesSharesRequestBytesPedestrian)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean request bytes Aggregate: " + this.getMean(valuesRequestBytesAggregate) +
                "\nmedian request bytes Aggregate: " + this.getMedian(valuesRequestBytesAggregate) +
                "\n95%-tile request bytes Aggregate: " + this.get95Tile(valuesRequestBytesAggregate) +
                "\nmin request bytes Aggregate: " + this.getMin(valuesRequestBytesAggregate) +
                "\nmax request bytes Aggregate: " + this.getMax(valuesRequestBytesAggregate) + "\n\n"
        ;

        write(statsText);










        /* Offloading stats - Per Node - Responses*/
        write("\nOffloading Statistics (Per Node) - Responses\n" + "--------------------------------\n");

        //Calculate Aggregate Numbers
        individualNumOfResponsesAggregate = new TreeMap<Integer,Long>(individualNumOfResponsesCellular);

        for(Integer i : individualNumOfResponsesWiFi.keySet()) {
            if(individualNumOfResponsesAggregate.containsKey(i))
                individualNumOfResponsesAggregate.put(i, individualNumOfResponsesAggregate.get(i) + individualNumOfResponsesWiFi.get(i));
            else
                individualNumOfResponsesAggregate.put(i, individualNumOfResponsesWiFi.get(i));
        }

        for(Integer i : individualNumOfResponsesPedestrian.keySet()) {
            if(individualNumOfResponsesAggregate.containsKey(i))
                individualNumOfResponsesAggregate.put(i, individualNumOfResponsesAggregate.get(i) + individualNumOfResponsesPedestrian.get(i));
            else
                individualNumOfResponsesAggregate.put(i, individualNumOfResponsesPedestrian.get(i));
        }

        //Fill any missing info for nodes with 0.
        for(Integer i : individualNumOfResponsesAggregate.keySet()) {
            if(!individualNumOfResponsesPedestrian.containsKey(i)){
                individualNumOfResponsesPedestrian.put(i, 0L);
            }
            if(!individualNumOfResponsesWiFi.containsKey(i)){
                individualNumOfResponsesWiFi.put(i, 0L);
            }
            if(!individualNumOfResponsesCellular.containsKey(i)){
                individualNumOfResponsesCellular.put(i, 0L);
            }
        }

        //Calculate Shares
        individualShareResponsesWiFi = calculateShares(individualNumOfResponsesWiFi, individualNumOfResponsesAggregate);
        individualShareResponsesCellular = calculateShares(individualNumOfResponsesCellular, individualNumOfResponsesAggregate);
        individualShareResponsesPedestrian = calculateShares(individualNumOfResponsesPedestrian, individualNumOfResponsesAggregate);


        //Sort Lists of Values
        ArrayList<Long> valuesResponsesWiFi = new ArrayList<Long>(individualNumOfResponsesWiFi.values());
        Collections.sort(valuesResponsesWiFi);
        ArrayList<Double> valuesSharesResponsesWiFi = new ArrayList<Double>(individualShareResponsesWiFi.values());
        Collections.sort(valuesSharesResponsesWiFi);
        ArrayList<Long> valuesResponsesCellular = new ArrayList<Long>(individualNumOfResponsesCellular.values());
        Collections.sort(valuesResponsesCellular);
        ArrayList<Double> valuesSharesResponsesCellular = new ArrayList<Double>(individualShareResponsesCellular.values());
        Collections.sort(valuesSharesResponsesCellular);
        ArrayList<Long> valuesResponsesPedestrian = new ArrayList<Long>(individualNumOfResponsesPedestrian.values());
        Collections.sort(valuesResponsesPedestrian);
        ArrayList<Double> valuesSharesResponsesPedestrian = new ArrayList<Double>(individualShareResponsesPedestrian.values());
        Collections.sort(valuesSharesResponsesPedestrian);
        ArrayList<Long> valuesResponsesAggregate = new ArrayList<Long>(individualNumOfResponsesAggregate.values());
        Collections.sort(valuesResponsesAggregate);


        statsText = "\nmean responses Cellular: " + this.getMean(valuesResponsesCellular) +
                "\nmean response share Cellular: " + df.format(this.getMeanDouble(valuesSharesResponsesCellular)) + " %" +
                "\nmedian responses Cellular: " + this.getMedian(valuesResponsesCellular) +
                "\nmedian response share Cellular: " + df.format(this.getMedianDouble(valuesSharesResponsesCellular)) + " %" +
                "\n95%-tile responses Cellular: " + this.get95Tile(valuesResponsesCellular) +
                "\n95%-tile response share Cellular: " + df.format(this.get95TileDouble(valuesSharesResponsesCellular)) + " %" +
                "\nmin responses Cellular: " + this.getMin(valuesResponsesCellular) +
                "\nmin response share Cellular: " + df.format(this.getMinDouble(valuesSharesResponsesCellular)) + " %" +
                "\nmax responses Cellular: " + this.getMax(valuesResponsesCellular) +
                "\nmax response share Cellular: " + df.format(this.getMaxDouble(valuesSharesResponsesCellular)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading responses WiFi: " + this.getMean(valuesResponsesWiFi) +
                "\nmean offloading response share WiFi: " + df.format(this.getMeanDouble(valuesSharesResponsesWiFi)) + " %" +
                "\nmedian offloading responses WiFi: " + this.getMedian(valuesResponsesWiFi) +
                "\nmedian offloading response share WiFi: " + df.format(this.getMedianDouble(valuesSharesResponsesWiFi)) + " %" +
                "\n95%-tile offloading responses WiFi: " + this.get95Tile(valuesResponsesWiFi) +
                "\n95%-tile offloading response share WiFi: " + df.format(this.get95TileDouble(valuesSharesResponsesWiFi)) + " %" +
                "\nmin offloading responses WiFi: " + this.getMin(valuesResponsesWiFi) +
                "\nmin offloading response share WiFi: " + df.format(this.getMinDouble(valuesSharesResponsesWiFi)) + " %" +
                "\nmax offloading responses WiFi: " + this.getMax(valuesResponsesWiFi) +
                "\nmax offloading response share WiFi: " + df.format(this.getMaxDouble(valuesSharesResponsesWiFi)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading responses Pedestrian: " + this.getMean(valuesResponsesPedestrian) +
                "\nmean offloading response share Pedestrian: " + df.format(this.getMeanDouble(valuesSharesResponsesPedestrian)) + " %" +
                "\nmedian offloading responses Pedestrian: " + this.getMedian(valuesResponsesPedestrian) +
                "\nmedian offloading response share Pedestrian: " + df.format(this.getMedianDouble(valuesSharesResponsesPedestrian)) + " %" +
                "\n95%-tile offloading responses Pedestrian: " + this.get95Tile(valuesResponsesPedestrian) +
                "\n95%-tile offloading response share Pedestrian: " + df.format(this.get95TileDouble(valuesSharesResponsesPedestrian)) + " %" +
                "\nmin offloading responses Pedestrian: " + this.getMin(valuesResponsesPedestrian) +
                "\nmin offloading response share Pedestrian: " + df.format(this.getMinDouble(valuesSharesResponsesPedestrian)) + " %" +
                "\nmax offloading responses Pedestrian: " + this.getMax(valuesResponsesPedestrian) +
                "\nmax offloading response share Pedestrian: " + df.format(this.getMaxDouble(valuesSharesResponsesPedestrian)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean responses Aggregate: " + this.getMean(valuesResponsesAggregate) +
                "\nmedian responses Aggregate: " + this.getMedian(valuesResponsesAggregate) +
                "\n95%-tile responses Aggregate: " + this.get95Tile(valuesResponsesAggregate) +
                "\nmin responses Aggregate: " + this.getMin(valuesResponsesAggregate) +
                "\nmax responses Aggregate: " + this.getMax(valuesResponsesAggregate) + "\n\n"
        ;

        write(statsText);






        /* Offloading stats - Per Node - Response Bytes*/
        write("\nOffloading Statistics (Per Node) - Response Bytes\n" + "--------------------------------\n");

        //Calculate Aggregate Numbers
        individualNumOfResponseBytesAggregate = new TreeMap<Integer,Long>(individualNumOfResponseBytesCellular);

        for(Integer i : individualNumOfResponseBytesWiFi.keySet()) {
            if(individualNumOfResponseBytesAggregate.containsKey(i))
                individualNumOfResponseBytesAggregate.put(i, individualNumOfResponseBytesAggregate.get(i) + individualNumOfResponseBytesWiFi.get(i));
            else
                individualNumOfResponseBytesAggregate.put(i, individualNumOfResponseBytesWiFi.get(i));
        }

        for(Integer i : individualNumOfResponseBytesPedestrian.keySet()) {
            if(individualNumOfResponseBytesAggregate.containsKey(i))
                individualNumOfResponseBytesAggregate.put(i, individualNumOfResponseBytesAggregate.get(i) + individualNumOfResponseBytesPedestrian.get(i));
            else
                individualNumOfResponseBytesAggregate.put(i, individualNumOfResponseBytesPedestrian.get(i));
        }

        //Fill any missing info for nodes with 0.
        for(Integer i : individualNumOfResponseBytesAggregate.keySet()) {
            if(!individualNumOfResponseBytesPedestrian.containsKey(i)){
                individualNumOfResponseBytesPedestrian.put(i, 0L);
            }
            if(!individualNumOfResponseBytesWiFi.containsKey(i)){
                individualNumOfResponseBytesWiFi.put(i, 0L);
            }
            if(!individualNumOfResponseBytesCellular.containsKey(i)){
                individualNumOfResponseBytesCellular.put(i, 0L);
            }
        }

        //Calculate Shares
        individualShareResponseBytesWiFi = calculateShares(individualNumOfResponseBytesWiFi, individualNumOfResponseBytesAggregate);
        individualShareResponseBytesCellular = calculateShares(individualNumOfResponseBytesCellular, individualNumOfResponseBytesAggregate);
        individualShareResponseBytesPedestrian = calculateShares(individualNumOfResponseBytesPedestrian, individualNumOfResponseBytesAggregate);

        //Sort Lists of Values
        ArrayList<Long> valuesResponseBytesWiFi = new ArrayList<Long>(individualNumOfResponseBytesWiFi.values());
        Collections.sort(valuesResponseBytesWiFi);
        ArrayList<Double> valuesSharesResponseBytesWiFi = new ArrayList<Double>(individualShareResponseBytesWiFi.values());
        Collections.sort(valuesSharesResponseBytesWiFi);
        ArrayList<Long> valuesResponseBytesCellular = new ArrayList<Long>(individualNumOfResponseBytesCellular.values());
        Collections.sort(valuesResponseBytesCellular);
        ArrayList<Double> valuesSharesResponseBytesCellular = new ArrayList<Double>(individualShareResponseBytesCellular.values());
        Collections.sort(valuesSharesResponseBytesCellular);
        ArrayList<Long> valuesResponseBytesPedestrian = new ArrayList<Long>(individualNumOfResponseBytesPedestrian.values());
        Collections.sort(valuesResponseBytesPedestrian);
        ArrayList<Double> valuesSharesResponseBytesPedestrian = new ArrayList<Double>(individualShareResponseBytesPedestrian.values());
        Collections.sort(valuesSharesResponseBytesPedestrian);
        ArrayList<Long> valuesResponseBytesAggregate = new ArrayList<Long>(individualNumOfResponseBytesAggregate.values());
        Collections.sort(valuesResponseBytesAggregate);


        statsText = "\nmean response bytes Cellular: " + this.getMean(valuesResponseBytesCellular) +
                "\nmean response bytes share Cellular: " + df.format(this.getMeanDouble(valuesSharesResponseBytesCellular)) + " %" +
                "\nmedian response bytes Cellular: " + this.getMedian(valuesResponseBytesCellular) +
                "\nmedian response bytes share Cellular: " + df.format(this.getMedianDouble(valuesSharesResponseBytesCellular)) + " %" +
                "\n95%-tile response bytes Cellular: " + this.get95Tile(valuesResponseBytesCellular) +
                "\n95%-tile response bytes share Cellular: " + df.format(this.get95TileDouble(valuesSharesResponseBytesCellular)) + " %" +
                "\nmin response bytes Cellular: " + this.getMin(valuesResponseBytesCellular) +
                "\nmin response bytes share Cellular: " + df.format(this.getMinDouble(valuesSharesResponseBytesCellular)) + " %" +
                "\nmax response bytes Cellular: " + this.getMax(valuesResponseBytesCellular) +
                "\nmax response bytes share Cellular: " + df.format(this.getMaxDouble(valuesSharesResponseBytesCellular)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading response bytes WiFi: " + this.getMean(valuesResponseBytesWiFi) +
                "\nmean offloading response bytes share WiFi: " + df.format(this.getMeanDouble(valuesSharesResponseBytesWiFi)) + " %" +
                "\nmedian offloading response bytes WiFi: " + this.getMedian(valuesResponseBytesWiFi) +
                "\nmedian offloading response bytes share WiFi: " + df.format(this.getMedianDouble(valuesSharesResponseBytesWiFi)) + " %" +
                "\n95%-tile offloading response bytes WiFi: " + this.get95Tile(valuesResponseBytesWiFi) +
                "\n95%-tile offloading response bytes share WiFi: " + df.format(this.get95TileDouble(valuesSharesResponseBytesWiFi)) + " %" +
                "\nmin offloading response bytes WiFi: " + this.getMin(valuesResponseBytesWiFi) +
                "\nmin offloading response bytes share WiFi: " + df.format(this.getMinDouble(valuesSharesResponseBytesWiFi)) + " %" +
                "\nmax offloading response bytes WiFi: " + this.getMax(valuesResponseBytesWiFi) +
                "\nmax offloading response bytes share WiFi: " + df.format(this.getMaxDouble(valuesSharesResponseBytesWiFi)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean offloading response bytes Pedestrian: " + this.getMean(valuesResponseBytesPedestrian) +
                "\nmean offloading response bytes share Pedestrian: " + df.format(this.getMeanDouble(valuesSharesResponseBytesPedestrian)) + " %" +
                "\nmedian offloading response bytes Pedestrian: " + this.getMedian(valuesResponseBytesPedestrian) +
                "\nmedian offloading response bytes share Pedestrian: " + df.format(this.getMedianDouble(valuesSharesResponseBytesPedestrian)) + " %" +
                "\n95%-tile offloading response bytes Pedestrian: " + this.get95Tile(valuesResponseBytesPedestrian) +
                "\n95%-tile offloading response bytes share Pedestrian: " + df.format(this.get95TileDouble(valuesSharesResponseBytesPedestrian)) + " %" +
                "\nmin offloading response bytes Pedestrian: " + this.getMin(valuesResponseBytesPedestrian) +
                "\nmin offloading response bytes share Pedestrian: " + df.format(this.getMinDouble(valuesSharesResponseBytesPedestrian)) + " %" +
                "\nmax offloading response bytes Pedestrian: " + this.getMax(valuesResponseBytesPedestrian) +
                "\nmax offloading response bytes share Pedestrian: " + df.format(this.getMaxDouble(valuesSharesResponseBytesPedestrian)) + " %" + "\n"
        ;

        write(statsText);


        statsText = "\nmean response bytes Aggregate: " + this.getMean(valuesResponseBytesAggregate) +
                "\nmedian response bytes Aggregate: " + this.getMedian(valuesResponseBytesAggregate) +
                "\n95%-tile response bytes Aggregate: " + this.get95Tile(valuesResponseBytesAggregate) +
                "\nmin response bytes Aggregate: " + this.getMin(valuesResponseBytesAggregate) +
                "\nmax response bytes Aggregate: " + this.getMax(valuesResponseBytesAggregate) + "\n"
        ;

        write(statsText);



		super.done();
	}
}
