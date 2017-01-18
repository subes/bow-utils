package be.bagofwords.http;

import be.bagofwords.ui.UI;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by koen on 3/08/15.
 */
public class URLDownloaderTest {

    @Test
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