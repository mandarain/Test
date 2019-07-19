package GetFast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

class DatabaseFast {
    private Connection connection;

    //建立连接：
    void getLink() throws ClassNotFoundException, SQLException {
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://localhost:3306/TieBa?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "123456";
        connection = DriverManager.getConnection(url, user, pass);
    }

    //清除原来数据方法：
    void clearMainTable() throws SQLException {
        String sql1 = "truncate table mainTable";
        PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
        preparedStatement1.executeUpdate();
    }

    //存入不重复新数据方法：
    void putInMainTable(String subUrl, String title, String linkUrl, String mainId) throws SQLException {
        String sql2 = "INSERT IGNORE INTO mainTable VALUE (?,?,?,?,?)";
        PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDate = dateFormat.format(date);
        preparedStatement2.setString(1, subUrl.replace("/p/", ""));
        preparedStatement2.setString(2, mainId);
        preparedStatement2.setString(3, linkUrl);
        preparedStatement2.setString(4, title);
        preparedStatement2.setString(5, currentDate);
        preparedStatement2.addBatch();
        preparedStatement2.executeBatch();
    }

    void clearSubTable() throws SQLException {
        String sql1 = "truncate table subTable";
        PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
        preparedStatement1.executeUpdate();
    }

    void putInSubTable(String subURL, int cenNumber, String subId, String subContent, LinkedList<String> imgList) throws SQLException {
        String sql2 = "INSERT IGNORE INTO subTable VALUE (?,?,?,?,?,?)";
        PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDate = dateFormat.format(date);
        preparedStatement2.setString(1, subURL.replace("/p/", ""));
        preparedStatement2.setInt(2, cenNumber);
        preparedStatement2.setString(3, subId);
        preparedStatement2.setString(4, subContent);
        preparedStatement2.setString(5, String.valueOf(imgList));
        preparedStatement2.setString(6, currentDate);
        preparedStatement2.addBatch();
        preparedStatement2.executeBatch();
    }
}
