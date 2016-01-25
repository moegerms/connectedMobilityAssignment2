package input;

/**
 * Created by Matthias on 25.01.2016.
 */
public class WebPage {
    private int webPageNumber = 0;
    private int webPageSize = 0;

    public WebPage(int number, int size){
        setWebPageSize(size);
        setWebPageNumber(number);
    }

    public int getWebPageNumber() {
        return webPageNumber;
    }

    public void setWebPageNumber(int webPageNumber) {
        this.webPageNumber = webPageNumber;
    }


    public int getWebPageSize() {
        return webPageSize;
    }

    public void setWebPageSize(int webPageSize) {
        this.webPageSize = webPageSize;
    }

}
