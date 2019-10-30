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

    @Test
    @Ignore
    public void testDeMorgenWithCookies() throws Exception {
        URLDownloader urlDownloader = new URLDownloader("https://www.demorgen.be/nieuws");
        urlDownloader.getSavedCookies().put("pwv", "1");
        urlDownloader.getSavedCookies().put("pws", "functional|analytics|content_recommendation|targeted_advertising|social_media");
        DownloadResult result = urlDownloader.download();
        Assert.assertTrue(result.isSuccess());
        String body = urlDownloader.getContent();
        Assert.assertTrue("Body should contain content", body.contains("teaser__title"));
    }

}