package net.llamadevelopment.surveysystem.components.managers;

import cn.nukkit.utils.Config;
import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import net.llamadevelopment.surveysystem.components.utils.SurveyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlManager extends ProviderManager {

    @Override
    public void setUp(SurveySystem instance) {
        instance.getLogger().info("Using YAML as provider...");
        instance.saveResource("data/survey-data.yml");
    }

    @Override
    public void createSurvey(String title, String text, int time) {
        String id = SurveySystem.getInstance().getSurveyID();
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        List<String> list = new ArrayList<String>();
        List<String> list2 = survey.getStringList("Opened");
        list2.add(id);
        int seconds = time * 3600;
        long current = System.currentTimeMillis();
        long end = current + seconds * 1000L;
        survey.set("Survey." + id + ".Title", title);
        survey.set("Survey." + id + ".Text", text);
        survey.set("Survey." + id + ".Status", "Open");
        survey.set("Survey." + id + ".Time", end);
        survey.set("Survey." + id + ".Yes", 0);
        survey.set("Survey." + id + ".No", 0);
        survey.set("Survey." + id + ".All", 0);
        survey.set("Data." + title, id);
        survey.set("Player." + id, list);
        survey.set("Opened", list2);
        survey.save();
        survey.reload();
    }

    @Override
    public void deleteSurvey(String id) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        SurveyUtil surveyUtil = getSurvey(id);
        Map<String, Object> map = survey.getSection("Survey").getAllMap();
        map.remove(id);
        survey.set("Survey", map);
        Map<String, Object> map1 = survey.getSection("Data").getAllMap();
        map1.remove(surveyUtil.getTitle());
        survey.set("Data", map1);
        Map<String, Object> map2 = survey.getSection("Player").getAllMap();
        map2.remove(id);
        survey.set("Player", map2);
        List<String> list = survey.getStringList("Closed");
        list.remove(id);
        survey.set("Closed", list);
        survey.save();
        survey.reload();
    }

    @Override
    public boolean hasVoted(String player, String id) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        List<String> list = survey.getStringList("Player." + id);
        return list.contains(player);
    }

    @Override
    public void updateSurvey(String id, String player, boolean type) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        if (type) {
            int r = survey.getInt("Survey." + id + ".Yes");
            int u = r + 1;
            int g = survey.getInt("Survey." + id + ".All");
            int k = g + 1;
            survey.set("Survey." + id + ".Yes", u);
            survey.set("Survey." + id + ".All", k);
            List<String> list = survey.getStringList("Player." + id);
            list.add(player);
            survey.set("Player." + id, list);
            survey.save();
            survey.reload();
        } else {
            int r = survey.getInt("Survey." + id + ".No");
            int u = r + 1;
            int g = survey.getInt("Survey." + id + ".All");
            int k = g + 1;
            survey.set("Survey." + id + ".No", u);
            survey.set("Survey." + id + ".All", k);
            List<String> list = survey.getStringList("Player." + id);
            list.add(player);
            survey.set("Player." + id, list);
            survey.save();
            survey.reload();
        }
    }

    @Override
    public void closeSurvey(String id) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        survey.set("Survey." + id + ".Status", "Closed");
        List<String> openList = survey.getStringList("Opened");
        openList.remove(id);
        survey.set("Opened", openList);
        List<String> closedList = survey.getStringList("Closed");
        closedList.add(id);
        survey.set("Closed", closedList);
        survey.save();
        survey.reload();
    }

    @Override
    public void checkSurvey() {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        for (String s : survey.getStringList("Opened")) {
            long f = survey.getLong("Survey." + s + ".Time");
            if (f < System.currentTimeMillis()) closeSurvey(s);
        }
    }

    @Override
    public String convertToID(String title) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        if (survey.exists("Data." + title)) {
            return survey.getString("Data." + title);
        }
        return "null";
    }

    @Override
    public SurveyUtil getSurvey(String id) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        String title = survey.getString("Survey." + id + ".Title");
        String text = survey.getString("Survey." + id + ".Text");
        String status = survey.getString("Survey." + id + ".Status");
        long time = survey.getLong("Survey." + id + ".Time");
        int yes = survey.getInt("Survey." + id + ".Yes");
        int no = survey.getInt("Survey." + id + ".No");
        int all = survey.getInt("Survey." + id + ".All");
        return new SurveyUtil(title, text, status, id, time, yes, no, all);
    }

    @Override
    public boolean surveyExistsByTitle(String title) {
        Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
        return survey.exists("Data." + title);
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }
}
