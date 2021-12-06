package org.rx.test;

import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.rx.crawler.*;
import org.rx.crawler.common.AppConfig;
import org.rx.crawler.service.BrowserPool;
import org.rx.crawler.service.impl.WebBrowser;
import org.rx.core.Cache;
import org.rx.core.NQuery;
import org.rx.core.Tasks;
import org.rx.io.Files;
import org.rx.io.IOStream;
import org.rx.net.http.HttpClient;
import org.rx.net.rpc.Remoting;
import org.rx.net.rpc.RpcClientConfig;
import org.rx.spring.SpringContext;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.rx.core.App.*;

@SpringBootTest(classes = Application.class)
public class BrowserTests {
    @Resource
    AppConfig appConfig;
    @Resource
    BrowserAsyncTopic asyncTopic;
//    @Resource
//    BrowserService browserService;

    @SneakyThrows
    @Test
    public void poolListener() {
        Remoting.listen(new BrowserPool(appConfig, asyncTopic), 1210);

        Tasks.schedule(() -> {
            BrowserPoolListener listener = Remoting.create(BrowserPoolListener.class, RpcClientConfig.statefulMode("127.0.0.1:1210", 0));
            System.out.println(listener.nextIdleId(BrowserType.IE));
            tryClose(listener);
        }, 1000);

        System.in.read();
    }

    @SneakyThrows
    @Test
    public void urlGenerator() {
        System.out.println(SpringContext.getBean(AppConfig.class));

//        UrlGenerator generator = new UrlGenerator("http://free-proxy.cz/zh/proxylist/country/CN/socks5/uptime/level1/[1-5]");
//        for (String url : generator) {
//            System.out.println(url);
//        }
    }

    @SneakyThrows
    @Test
    public void fiddler() {
        String path = "D:\\app_rebate\\fiddler\\VipGoods_1584849184132.txt";
        String p = NQuery.of(Files.readLines(path)).last();
        String u = HttpClient.decodeUrl(toJsonObject(p).getString("page_url"));
        Map<String, String> queryString = HttpClient.decodeQueryString(u);
        u = queryString.get("$route");
        queryString = HttpClient.decodeQueryString(u);
        System.out.println(queryString);
        String goodsId = queryString.get("brandId") + "-" + queryString.get("goodsId");
        System.out.println(goodsId);
    }

    @SneakyThrows
    @Test
    public void download() {
        String refUrl = "https://pub.alimama.com/myunion.htm?spm=a219t.7900221/1.a214tr8.2.2a8f75a5HmjmiY#!/report/detail/taoke";
        String rawCookie = "isg=BCEhEApn2ciykHU8ek0CJg5-OO07zpXAjSi_woP2HSiH6kG8yx6lkE-4StxJOS34; t=70824e08423cb52e5173c58b0dee1a93; cna=6N84E+HCOwcCAXngjNKxcUwW; l=aB7DTtLdyUaWZyQpDMaPsVhISxrxygBPpkTZBMaLzTqGdP8vhtS1fjno-VwkQ_qC5f9L_XtiI; cookie2=1391a802ada07c947d4f6dc4f332bfaa; v=0; _tb_token_=fe5b3865573ee; alimamapwag=TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV09XNjQ7IFRyaWRlbnQvNy4wOyBydjoxMS4wKSBsaWtlIEdlY2tv; cookie32=da263646f8b7310d20a6241569cb21ca; alimamapw=TgoJAwYAAgoEBmsJUVQABQMFDglSAwAIBVpQBQEKUwMHUVMFCF8EB1NUVQ%3D%3D; cookie31=MzcxNTE0NDcsdzM5NTExNTMyMyxyb2NreXdvbmcuY2huQGdtYWlsLmNvbSxUQg%3D%3D; login=Vq8l%2BKCLz3%2F65A%3D%3D";
        HttpClient.COOKIE_CONTAINER.saveFromResponse(HttpUrl.get(refUrl), HttpClient.decodeCookie(HttpUrl.get(refUrl), rawCookie));
        String url = "https://pub.alimama.com/report/getTbkPaymentDetails.json?spm=a219t.7664554.1998457203.10.19ef35d9uFsIOb&queryType=1&payStatus=&DownloadID=DOWNLOAD_REPORT_INCOME_NEW&startTime=2019-01-05&endTime=2019-01-11";
        HttpClient caller = new HttpClient();
//        caller.setHeaders(HttpCaller.parseOriginalHeader("Accept: text/html, application/xhtml+xml, image/jxr, */*\n" +
////                "Referer: https://pub.alimama.com/myunion.htm?spm=a219t.7900221/1.a214tr8.2.2a8f75a5khPITz\n" +
////                "Accept-Language: en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3\n" +
////                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko\n" +
////                "Accept-Encoding: gzip, deflate\n" +
////                "Host: pub.alimama.com\n" +
////                "Connection: Keep-Alive\n" +
//                "Cookie: "+rawCookie));
        caller.get(url).toFile("D:\\a.xls");
    }

    @SneakyThrows
    @Test
    public void rpcInvoke() {
        RemoteBrowser.invoke(browser -> {
            browser.navigateUrl("http://f-li.cn");
        });
        RemoteBrowser.invoke(browser -> {
            browser.navigateUrl("http://cloud.f-li.cn");
        });
//        System.in.read();
    }

    @Test
    public void innerScript() {
        String baseScript = Cache.getOrSet("WebBrowser.baseScript", k -> {
            InputStream stream = WebBrowser.class.getResourceAsStream("/bot/base.js");
            if (stream == null) {
                System.out.println("resource is null");
                return "";
            }
            return IOStream.readString(stream, StandardCharsets.UTF_8) + "\n";
        });
        System.out.println(baseScript);
    }

    @SneakyThrows
    @Test
    public void webLogin() {
        System.setProperty("webdriver.chrome.driver", readSetting("app.chrome.driver"));
        System.setProperty("webdriver.ie.driver", readSetting("app.ie.driver"));
        String url = "https://login.taobao.com/member/login.jhtml?style=mini&newMini2=true&from=alimama&redirectURL=http:%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=false";
        InternetExplorerOptions opt = new InternetExplorerOptions();
        opt.withInitialBrowserUrl("about:blank");
        InternetExplorerDriver driver = new InternetExplorerDriver(opt);
        driver.get(url);
        Thread.sleep(3000);
        By locator = By.id("J_SubmitQuick");
        while (!driver.getCurrentUrl().contains("alimama.com")) {
            driver.findElement(locator).click();
            System.out.println("click...");
            Thread.sleep(1000);
        }

        System.out.println("url: " + driver.getCurrentUrl());
        for (org.openqa.selenium.Cookie cookie : driver.manage().getCookies()) {
            System.out.println(cookie.getName());
        }
        System.in.read();
    }

    @SneakyThrows
    @Test
    public void changeTab() {
        WebBrowser caller = new WebBrowser(appConfig, BrowserType.CHROME);
        String currentHandle = caller.getCurrentHandle();
        System.out.println(currentHandle);

        String handle = caller.openTab();
        System.out.println(handle);
        Thread.sleep(2000);

        caller.openTab();
        System.out.println(handle);
        Thread.sleep(2000);

        caller.switchTab(handle);
        System.out.println("switch");
        Thread.sleep(2000);

        caller.closeTab(handle);
        System.out.println("close");
    }
}