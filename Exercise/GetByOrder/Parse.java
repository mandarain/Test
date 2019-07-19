package GetByOrder;

import org.apache.http.client.config.RequestConfig;
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
import java.util.LinkedList;
import java.util.concurrent.Callable;

public class Parse implements Callable {
    private static int l = 0;
    private static int count = 1;
    private final int t = count++;
    private Element mainElement;
    private Database newDatabase;

    Parse(Element mainElement, Database newDatabase) {
        this.mainElement = mainElement;
        this.newDatabase = newDatabase;
    }

    public Integer call() {
        //查找主网页元素：
        Elements title = mainElement.select("a[rel=noreferrer]a[class=j_th_tit ]");
        Elements mainId = mainElement.select("span[class=frs-author-name-wrap]");
        String url = "https://tieba.baidu.com" + title.attr("href");
        if (t >= 2) {
            System.out.println("----------------------------------------------------------------------------------------------------------");
        }
        //打印主网页元素：
        System.out.println("第" + t + "贴题目：" + title.text());
        System.out.println("链接： " + url);
        System.out.println("楼主id： " + mainId.text() + "\n");
        //主页面信息存入数据库：
        try {
            newDatabase.putInMainTable(title.attr("href"), title.text(), url, mainId.text());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //获取子网页：
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .build();
        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);
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
            entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //解析子网页：
        Document tieDocument = Jsoup.parse(entity);
        Elements cenElements = tieDocument.select("div[class=l_post l_post_bright j_l_post clearfix  ]");
        //对子网页信息进行多线程处理；
        int c = 0;
        for (Element cenElement : cenElements) {
            c += 1;
            Elements cenId = cenElement.select("li[class=d_name]");
            Elements cenContents = cenElement.select("div[class=d_post_content j_d_post_content ]");
            System.out.println("第" + c + "楼：");
            System.out.println("层主id: " + cenId.text());
            System.out.println("内容: " + cenContents.text());
            //筛选并爬取、下载图片：
            Elements imgs = cenContents.select("img[class=BDE_Image]");
            LinkedList<String> imgList = new LinkedList<String>();
            for (Element img : imgs) {
                String imgUrl = img.attr("abs:src");
                try {
                    new Img().getPictures(imgUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgList.add(imgUrl);
            }
            //子页面信息存入子数据库：
            try {
                newDatabase.putInSubTable(title.attr("href"), c, cenId.text(), cenContents.text(), imgList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        l += cenElements.size();
        return l;
    }
}


