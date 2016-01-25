package input;

import java.util.*;

/**
 * Created by Matthias on 04.01.2016.
 */

    //10.000 Web pages - rank-ordered
    //300KB - 3MB size (evenly distributed)
    //Zipf distribution-based popularity
    //- Pick Zipf parameters from a recent study

public class WebPages {
    private static WebPages singletonWebPages = null;
    private HashMap<Integer,Double> webPages = new HashMap();       //Webpage Size
    private HashMap<Integer,Double> webPagesZipf = new HashMap();   //Webpage Distribution
    Random rand = new Random();

    public WebPages(){

        rand.setSeed(42);

        webPages = evenlyDistributedSizes(webPages);
        webPages = mixWebPages(webPages);
        webPagesZipf = calculateZipfValues(webPagesZipf);

        /*double randValue = 0;
        for(int i = 1; i <= 10000; i++){
            //randValue = rand.nextDouble();
            System.out.println(getRandomWebPageSize());
        }*/
    }
    public static WebPages getInstance(){
        if(singletonWebPages == null){
            singletonWebPages = new WebPages();
        }
        return singletonWebPages;
    }

    private int numberOfWebpages = 10000;
    private int minSize = 300000;   //300KB
    private int maxSize = 3000000;  //3MB
    private HashMap<Integer,Double> evenlyDistributedSizes(HashMap<Integer,Double> webPages){

        //difference per Webpage
        double diff1 = maxSize - minSize;
        double diff2 = numberOfWebpages-1;
        double diff = diff1/diff2;

        for(int i = 0; i < numberOfWebpages; i++){
            webPages.put(i,(i*diff+minSize));
        }
        return webPages;
    }

    private HashMap<Integer,Double> mixWebPages(HashMap<Integer,Double> webPages){
        HashMap<Integer,Double> newWebPages = new HashMap();

        ArrayList<Double> cd = new ArrayList<Double>(webPages.values());
        long seed = 2949292;//System.nanoTime();
        Collections.shuffle(cd, new Random(seed));

        for(int i = 0; i<webPages.size();i++) {
            newWebPages.put(i,cd.get(i));
        }
        webPages = newWebPages;
        return webPages;
    }

    //private int N = numberOfWebpages
    private int s = 1; //Value of exponent characterizing the distr
    private double sum1toN = 0;
    private double sumToNormalize;
    private HashMap <Integer,Double> calculateZipfValues(HashMap <Integer,Double> webPagesZipf){
        //Calculate absolute values
        for(int i = 1; i <= numberOfWebpages; i++){
            sumToNormalize += zipf(i);
        }

        //Distribution Function
        double sumOfPriorValues = 0;
        for(int i = 1; i <= numberOfWebpages; i++){
            sumOfPriorValues += zipf(i)/sumToNormalize;
            webPagesZipf.put(i,sumOfPriorValues);
        }
        return webPagesZipf;
    }


    private double zipf(int rank_k){
        //f(k;s,N) = (1/k^s)/(Sum1toN(1/n^s))
        if(sum1toN == 0) {
            for (int i = 1; i <= numberOfWebpages; i++) {
                sum1toN = (1 / i ^ s);
            }
        }
        double result = 1/Math.pow(rank_k,s)/sum1toN;
        return result;
    }

    /*public double getRandomWebPageSize (){
        double randValue = 0;
        int j = 0;
        randValue = rand.nextDouble();
        for(j = 1; j<= numberOfWebpages; j++){
            if(randValue < webPagesZipf.get(j)){
                break;
            }
        }
        return webPages.get(j);
    }*/
    public int getRandomWebPageNumber (){

        double randValue = 0;
        int j = 0;
        randValue = rand.nextDouble();
        for(j = 1; j<= numberOfWebpages; j++){
            if(randValue < webPagesZipf.get(j)){
                break;
            }
        }
        return j;
    }

    public WebPage getRandomWebPage (){

        double randValue = 0;
        int j = 0;
        randValue = rand.nextDouble();
        for(j = 1; j<= numberOfWebpages; j++){
            if(randValue < webPagesZipf.get(j)){
                break;
            }
        }
        double size = webPages.get(j);
        WebPage webPage = new WebPage(j, (int) size );
        return webPage;
    }

    public int getWebPage(int webpageNumber) {
        double size = webPages.get(webpageNumber);
        return (int) size;
    }
}
