package GetByOrder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

class Img {
    void getPictures(String imgUrl) throws IOException {
        //对图片URL进行转码处理：
        String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
        String urlTail = URLEncoder.encode(fileName, "UTF-8");
        imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('/') + 1) + urlTail.replaceAll("\\+", "\\%20");
        //设立写入文件夹：
        File file = new File("D://img//" + fileName);
        //建立连接、IO、缓冲区：
        URL url = new URL(imgUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(60000);
        InputStream inputStream = urlConnection.getInputStream();
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytes = new byte[2048];
        int size;
        //开始写入：
        while ((size = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, size);
        }
        outputStream.close();
        inputStream.close();
        System.out.println(imgUrl);
    }
}
