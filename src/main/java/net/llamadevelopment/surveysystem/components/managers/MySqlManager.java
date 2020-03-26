package net.llamadevelopment.surveysystem.components.managers;

import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import net.llamadevelopment.surveysystem.components.utils.SurveyUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySqlManager extends ProviderManager {

    private static Connection connection;

    @Override
    public void setUp(SurveySystem instance) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + instance.getConfig().getString("MySql.Host") + ":" + instance.getConfig().getString("MySql.Port") + "/" + instance.getConfig().getString("MySql.Database") + "?autoReconnect=true", instance.getConfig().getString("MySql.User"), instance.getConfig().getString("MySql.Password"));
            instance.getLogger().info("§aConnected successfully to database!");
            update("CREATE TABLE IF NOT EXISTS surveys(title VARCHAR(255), text VARCHAR(255), status VARCHAR(255), id VARCHAR(255), time BIGINT(255), yes INT(255), no INT(255), total INT(255), players VARCHAR(255), PRIMARY KEY (id));");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void update(String qry) {
        if (connection != null) {
            try {
                PreparedStatement ps = connection.prepareStatement(qry);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean hasVoted(String player, String id) {
        try {
            PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE ID = ?");
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString("players").split(":");
                for (String e : s) {
                    if (e.equalsIgnoreCase(player)) return true;
                }
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void disconnect(SurveySystem instance) {
        try {
            getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createSurvey(String title, String text, int time) {
        long current = System.currentTimeMillis();
        long end = current + time * 1000L;
        update("INSERT INTO surveys (TITLE, TEXT, STATUS, ID, TIME, YES, NO, TOTAL, PLAYERS) VALUES ('" + title + "', '" + text + "', 'Open', '" + SurveySystem.getInstance().getSurveyID() + "', '" + end + "', '0', '0', '0', '');");
    }

    @Override
    public void deleteSurvey(String id) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM surveys WHERE ID = ?");
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeSurvey(String id) {
        update("UPDATE surveys SET STATUS= 'Closed' WHERE ID= '" + id +"';");
    }

    @Override
    public void checkSurvey(String id) {
        SurveyUtil surveyUtil = getSurvey(id);
        if (surveyUtil.getTime() < System.currentTimeMillis()) {
            closeSurvey(id);
        }
    }

    @Override
    public void updateSurvey(String id, String player, boolean type) {
        SurveyUtil surveyUtil = getSurvey(id);
        int a = surveyUtil.getYes();
        int s = surveyUtil.getNo();
        int d = surveyUtil.getAll();
        String players = "";
        try {
            PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE ID = ?");
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                players = rs.getString("PLAYERS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String newPlayers = players + player + ":";
        if (type) {
            int an = a += 1;
            update("UPDATE surveys SET YES= '" + an + "' WHERE ID= '" + id +"';");
            int dn = d += 1;
            update("UPDATE surveys SET TOTAL= '" + dn + "' WHERE ID= '" + id +"';");
            update("UPDATE surveys SET PLAYERS= '" + newPlayers + "' WHERE ID= '" + id +"';");
        } else {
            int sn = s += 1;
            update("UPDATE surveys SET NO= '" + sn + "' WHERE ID= '" + id +"';");
            int dn = d += 1;
            update("UPDATE surveys SET TOTAL= '" + dn + "' WHERE ID= '" + id +"';");
            update("UPDATE surveys SET PLAYERS= '" + newPlayers + "' WHERE ID= '" + id +"';");
        }
    }

    @Override
    public String convertToID(String title) {
        try {
            PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE TITLE = ?");
            preparedStatement.setString(1, title);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    @Override
    public SurveyUtil getSurvey(String id) {
        String title = "";
        String text = "";
        String status = "";
        long time = 0;
        int yes = 0, no = 0, all = 0;
        try {
            PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE ID = ?");
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                title = rs.getString("TITLE");
                text = rs.getString("TEXT");
                status = rs.getString("STATUS");
                time = rs.getLong("TIME");
                yes = rs.getInt("YES");
                no = rs.getInt("NO");
                all = rs.getInt("TOTAL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SurveyUtil(title, text, status, id, time, yes, no, all);
    }

    @Override
    public boolean surveyExistsByTitle(String title) {
        try {
            PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE TITLE = ?");
            preparedStatement.setString(1, title);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("TITLE").equalsIgnoreCase(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getProvider() {
        return "MySql";
    }
}
