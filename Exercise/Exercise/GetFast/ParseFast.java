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
    private static int t = 0;
    private static int c = 0;
    private Element mainElement;
    private DatabaseFast newDatabaseFast;
    private String forumName;

    ParseFast(Element mainElement, DatabaseFast newDatabaseFast, String forumName) {
        this.mainElement = mainElement;
        this.newDatabaseFast = newDatabaseFast;
        this.forumName = forumName;
    }

    public void run() {
        //查找主网页元素：
        //贴主题：
        Elements title = mainElement.select("a[rel=noreferrer]a[class=j_th_tit ]");
        //发帖人名：
        Elements mainId = mainElement.select("span[class=tb_icon_author ]");
        if (mainId.isEmpty()) {
            mainId = mainElement.select("span[class=tb_icon_author no_icon_author]");
        }
        String mainIdName = mainId.attr("title").replace("主题作者: ", "");
        String url = "https://tieba.baidu.com" + title.attr("href");
        //主页面信息存入数据库：
        try {
            newDatabaseFast.putInMainTable(title.attr("href"), title.text(), url, mainIdName, forumName);
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
        Elements subPage = tieDocument.select("li[class = l_pager pager_theme_5 pb_list_pager]").select("a:contains(尾页)");
        //判断为单页还是多页：
        if (subPage.isEmpty()) {//单页处理：
            Elements cenElements = tieDocument.select("div[class=l_post l_post_bright j_l_post clearfix  ]");
            //记录层数：
            c += cenElements.size();
            //建立子线程：
            ExecutorService subPool = Executors.newFixedThreadPool(30);
            //ExecutorService subPool = Executors.newCachedThreadPool();
            //对子网页信息进行多线程处理；
            for (Element cenElement : cenElements) {
                subPool.execute(new SubParseFast(cenElement, newDatabaseFast, title, forumName));
            }
            subPool.shutdown();
        } else { //多页处理：
            int max = Integer.parseInt(subPage.attr("href").substring(17));//String转为int,得到总子页数;
            //建立子线程：
            ExecutorService subPool = Executors.newFixedThreadPool(120);
            //ExecutorService subPool = Executors.newCachedThreadPool();
            for (int spn = 1; spn <= max; spn++) {
                try {
                    tieDocument = Jsoup.connect(url + "?pn=" + spn).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements cenElements = tieDocument.select("div[class=l_post l_post_bright j_l_post clearfix  ]");
                //记录层数：
                c += cenElements.size();
                //对子网页信息进行多线程处理；
                for (Element cenElement : cenElements) {
                    subPool.execute(new SubParseFast(cenElement, newDatabaseFast, title, forumName));
                }
            }
            subPool.shutdown();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t += 1;
        System.out.println("已爬取" + t + "个贴子," + c + "层楼");
    }
}



