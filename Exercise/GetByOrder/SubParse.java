package GetByOrder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

class SubParse implements Runnable {
    private int c;
    private Element cenElement;
    private Database newDatabase;
    private Elements title;

    SubParse(Element cenElement, Database newDatabase, Elements title, int c) {
        this.cenElement = cenElement;
        this.newDatabase = newDatabase;
        this.title = title;
        this.c = c;
    }

    public void run() {
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
}
