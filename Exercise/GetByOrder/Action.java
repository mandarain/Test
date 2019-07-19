package GetByOrder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Action {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException, ExecutionException {
        //建立计数：
        long a= System.currentTimeMillis();//获取当前系统时间(毫秒)
        int n = 0;
        Future<Integer> future = null;
        //建立数据库：
        Database newDatabase = new Database();
        newDatabase.getLink();
        //清除原数据库(可选)：
        newDatabase.clearMainTable();
        newDatabase.clearSubTable();
        //构造线程池：
        ExecutorService pool = Executors.newSingleThreadExecutor();
        //建立WebDriver:
        WebDriver webDriver = new ChromeDriver();
        for (int pn = 0; pn < 300; pn = pn + 50) {
            //获取主页信息：
            webDriver.get("https://tieba.baidu.com/f?kw=%E7%81%AB%E5%BD%B1%E5%BF%8D%E8%80%85&ie=utf-8&pn=" + pn);
            webDriver.manage().window().setSize(new Dimension(20, 20));
            Thread.sleep(1000);
            //解析主页：
            Document mainDocument = Jsoup.parse(webDriver.getPageSource());
            Elements mainElements = mainDocument.select("div[class=col2_right j_threadlist_li_right ]").select("div[class=threadlist_lz clearfix]");
            n += mainElements.size();
            //单线程处理主页面元素，并处理子页面/写入数据库：
            for (Element mainElement : mainElements) {
                future = pool.submit(new Parse(mainElement, newDatabase));
            }
        }
        pool.shutdown();
        //判断线程是否结束，并输出数量结果：
        while (true) {
            if (pool.isTerminated()) {
                System.out.println("***************************************************************************\n" + "输出结束,共爬取了" + n + "条帖子," + future.get() + "层楼");
                System.out.print("程序执行时间为：");
                System.out.println(System.currentTimeMillis()-a+"毫秒");
                webDriver.quit();
                break;
            }
            Thread.sleep(5000);
        }
    }
}



