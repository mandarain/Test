package GetFast;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.LinkedList;

class SubParseFast implements Runnable {
    private Element cenElement;
    private DatabaseFast newDatabase;
    private Elements title;
    private String forumName;

    SubParseFast(Element cenElement, DatabaseFast newDatabase, Elements title, String forumName) {
        this.cenElement = cenElement;
        this.newDatabase = newDatabase;
        this.title = title;
        this.forumName = forumName;
    }

    public void run() {
        Elements cenId = cenElement.select("li[class=d_name]");
        Elements cenContents = cenElement.select("div[class=d_post_content j_d_post_content ]");
        //读取内容ID号作为主键：
        String contentId = cenContents.attr("id").replace("post_content_", "");
        //读取尾部信息（楼层号、发帖时间）：
        Elements tails = cenElement.select("div[class=post-tail-wrap]");
        Elements cen = tails.select("span:contains(楼)");
        int cenNumber = Integer.parseInt(cen.text().replace("楼", ""));
        Elements time = tails.select("span:contains(-)");
        //筛选并爬取图片：
        Elements imgs = cenContents.select("img[class=BDE_Image]");
        LinkedList<String> imgList = new LinkedList<String>();
        for (Element img : imgs) {
            String imgUrl = img.attr("abs:src");
            String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
            String urlTail = null;
            try {
                urlTail = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            assert urlTail != null;
            imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('/') + 1) + urlTail.replaceAll("\\+", "\\%20");
            imgList.add(imgUrl);
        }
        //子页面信息存入子数据库：
        try {
            newDatabase.putInSubTable(title.attr("href"), cenNumber, cenId.text(), cenContents.text(), imgList, forumName, time.text(), contentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
