package GetFast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionFast {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        //建立计数：
        long a = System.currentTimeMillis();//获取当前系统时间(毫秒)
        int n = 0;
        //建立数据库：
        DatabaseFast newDatabaseFast = new DatabaseFast();
        newDatabaseFast.getLink();
        //清除原数据库(可选)：
        newDatabaseFast.clearMainTable();
        newDatabaseFast.clearSubTable();
        //构造线程池：
        ExecutorService pool = Executors.newCachedThreadPool();
        //建立WebDriver:
        WebDriver webDriver = new ChromeDriver();
        System.out.println("开始爬取...............");
        for (int pn = 0; pn < 300; pn = pn + 50) {
            //获取主页信息：
            webDriver.get("https://tieba.baidu.com/f?kw=%E4%B8%80%E5%87%BB%E7%94%B7&ie=utf-8&pn=" + pn);
            webDriver.manage().window().setSize(new Dimension(20, 20));
            Thread.sleep(1000);
            //解析主页：
            Document mainDocument = Jsoup.parse(webDriver.getPageSource());
            Elements mainElements = mainDocument.select("div[class=col2_right j_threadlist_li_right ]").select("div[class=threadlist_lz clearfix]");
            //记录已爬贴数：
            n += mainElements.size();
            //多线程处理主页面元素，并处理子页面/写入数据库：
            for (Element mainElement : mainElements) {
                pool.execute(new ParseFast(mainElement, newDatabaseFast));
            }
        }
        pool.shutdown();
        //判断线程是否结束，并输出数量结果：
        while (true) {
            if (pool.isTerminated()) {
                System.out.println("***************************************************************************\n" + "输出结束,共爬取了" + n + "条帖子");
                System.out.print("程序执行时间为：");
                System.out.println(System.currentTimeMillis() - a + "毫秒");
                webDriver.quit();
                break;
            }
            Thread.sleep(1000);
        }
    }
}

