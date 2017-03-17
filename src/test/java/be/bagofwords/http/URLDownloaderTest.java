package be.bagofwords.http;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by koen on 3/08/15.
 */
public class URLDownloaderTest {

    @Test
    @Ignore
    public void testHttps() throws Exception {
        URLDownloader urlDownloader = new URLDownloader("https://www.febelfin.be/nl/nieuws");
        DownloadResult download = urlDownloader.download();
        Assert.assertTrue(download.isSuccess());

        urlDownloader = new URLDownloader("https://www.acv-online.be");
        download = urlDownloader.download();
        Assert.assertTrue(download.isSuccess());

        urlDownloader = new URLDownloader("https://www.google.be");
        download = urlDownloader.download();
        Assert.assertTrue(download.isSuccess());

    }

}