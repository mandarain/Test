package GetFast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionFast {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException, UnsupportedEncodingException {
        //输入贴吧网站：
        System.out.println("请输入要爬取的贴吧主页域名(输入后按空格再按回车):");
        Scanner scanner = new Scanner(System.in);
        String totalUrl = scanner.nextLine();
        //输入爬取页数：
        System.out.println("请输入想要爬取的页数");
        int page = scanner.nextInt();
        int top = (page - 1) * 50;
        scanner.close();
        System.out.println("开始爬取...............");
        //建立WebDriver:
        WebDriver webDriver = new ChromeDriver();
        webDriver.get(totalUrl);
        webDriver.findElement(By.id("forum-card-head")).click();
        String mainUrl = webDriver.getCurrentUrl();
        //获取站名：
        String forumName = mainUrl.substring(mainUrl.indexOf("%"), mainUrl.lastIndexOf("i") - 1);
        forumName = URLDecoder.decode(forumName, "UTF-8");
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
        ExecutorService pool = Executors.newFixedThreadPool(50);//大规模爬取时
        //ExecutorService pool = Executors.newCachedThreadPool();//小规模爬取时
        for (int pn = 0; pn <= top; pn = pn + 50) {
            //获取主页信息：
            webDriver.get(mainUrl + "&pn=" + pn);
            webDriver.manage().window().setSize(new Dimension(20, 20));
            Thread.sleep(1000);
            //解析主页：
            Document mainDocument = Jsoup.parse(webDriver.getPageSource());
            Elements mainElements = mainDocument.select("div[class=col2_right j_threadlist_li_right ]").select("div[class=threadlist_lz clearfix]");
            //记录已爬贴数：
            n += mainElements.size();
            //多线程处理主页面元素，并处理子页面/写入数据库：
            for (Element mainElement : mainElements) {
                pool.execute(new ParseFast(mainElement, newDatabaseFast, forumName));
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
            Thread.sleep(5000);
        }
    }
}

