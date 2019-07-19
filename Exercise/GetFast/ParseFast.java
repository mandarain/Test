package GetFast;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParseFast implements Runnable {
    private Element mainElement;
    private DatabaseFast newDatabaseFast;

    ParseFast(Element mainElement, DatabaseFast newDatabaseFast) {
        this.mainElement = mainElement;
        this.newDatabaseFast = newDatabaseFast;
    }

    public void run() {
        //查找主网页元素：
        Elements title = mainElement.select("a[rel=noreferrer]a[class=j_th_tit ]");
        Elements mainId = mainElement.select("span[class=frs-author-name-wrap]");
        String url = "https://tieba.baidu.com" + title.attr("href");
        //主页面信息存入数据库：
        try {
            newDatabaseFast.putInMainTable(title.attr("href"), title.text(), url, mainId.text());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //获取子网页：
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        httpGet.setHeader("Cache-Control", "max-age=0");
        httpGet.setHeader("Connection", "keep-alive");
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String entity = null;
        try {
            assert httpResponse != null;
            entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //解析子网页：
        assert entity != null;
        Document tieDocument = Jsoup.parse(entity);
        Elements cenElements = tieDocument.select("div[class=l_post l_post_bright j_l_post clearfix  ]");
        //建立子线程：
        ExecutorService subPool = Executors.newCachedThreadPool();
        //对子网页信息进行多线程处理；
        int c = 0;
        for (Element cenElement : cenElements) {
            c += 1;
            subPool.execute(new SubParseFast(cenElement, newDatabaseFast, title, c));
        }
        subPool.shutdown();
    }
}



