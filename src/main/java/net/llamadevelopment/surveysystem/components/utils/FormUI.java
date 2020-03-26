package net.llamadevelopment.surveysystem.components.utils;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.managers.MongoDBManager;
import net.llamadevelopment.surveysystem.components.managers.MySqlManager;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class FormUI {

    private static SurveySystem instance = SurveySystem.getInstance();
    private static Config config = instance.getConfig();
    private static Configuration cg = new Configuration();
    private static ProviderManager provider = SurveySystem.provider;

    public static HashMap<Player, String> deleteCache = new HashMap<Player, String>();
    public static HashMap<Player, String> surveyOpenCache = new HashMap<Player, String>();

    public static void openSurveys(Player player) {
        FormWindowSimple surveyForm = new FormWindowSimple(config.getString("Ui.Title.Surveys"), config.getString("Ui.Text.Surveys"));
        int count = 0;
        if (provider.getProvider().equalsIgnoreCase("MongoDB")) {
            for (Document document : MongoDBManager.getSurveyCollection().find(new Document("status", "Open"))) {
                surveyForm.addButton(new ElementButton(document.getString("title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("MySql")) {
            try {
                PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE STATUS = ?");
                preparedStatement.setString(1, "Open");
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    count++;
                    surveyForm.addButton(new ElementButton(rs.getString("TITLE")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("Yaml")) {
            Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
            for (String s : survey.getStringList("Opened")) {
                surveyForm.addButton(new ElementButton(survey.getString("Survey." + s + ".Title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        }
        if (player.hasPermission("surveys.function.manage")) {
            surveyForm.addButton(new ElementButton(config.getString("Ui.Button.Management")));
        }
        player.showFormWindow(surveyForm);
    }

    public static void openSurvey(Player player, String id) {
        SurveyUtil surveyUtil = provider.getSurvey(id);
        if (!provider.hasVoted(player.getName(), id)) {
            FormWindowModal formWindowModal = new FormWindowModal(surveyUtil.getTitle(), cg.getAndReplaceNP("Ui.Text.Survey", surveyUtil.getTitle(), surveyUtil.getText()), cg.getAndReplaceNP("Ui.Button.VoteYes"), cg.getAndReplaceNP("Ui.Button.VoteNo"));
            player.showFormWindow(formWindowModal);
        } else {
            FormWindowSimple formWindowSimple = new FormWindowSimple(surveyUtil.getTitle(), cg.getAndReplaceNP("Ui.Text.Result", surveyUtil.getTitle(), surveyUtil.getText(), String.valueOf(surveyUtil.getAll()), String.valueOf(surveyUtil.getYes()), String.valueOf(surveyUtil.getNo()), surveyUtil.getStatus(), surveyUtil.getId()));
            player.showFormWindow(formWindowSimple);
        }
    }

    public static void openResultMenu(Player player, String id) {
        SurveyUtil surveyUtil = provider.getSurvey(id);
        FormWindowSimple form = new FormWindowSimple(surveyUtil.getTitle(), cg.getAndReplaceNP("Ui.Text.Result", surveyUtil.getTitle(), surveyUtil.getText(), String.valueOf(surveyUtil.getAll()), String.valueOf(surveyUtil.getYes()), String.valueOf(surveyUtil.getNo()), surveyUtil.getStatus(), surveyUtil.getId()));
        player.showFormWindow(form);
    }

    public static void openDeleteConfirm(Player player, String id) {
        FormWindowModal form = new FormWindowModal(config.getString("Ui.Title.DeleteConfirm"), config.getString("Ui.Text.DeleteConfirm"),
                config.getString("Ui.Button.Delete"), config.getString("Ui.Button.Back"));
        deleteCache.put(player, id);
        player.showFormWindow(form);
    }

    public static void openPanel(Player player) {
        FormWindowSimple surveyForm = new FormWindowSimple(config.getString("Ui.Title.Management"), config.getString("Ui.Text.Management"));
        surveyForm.addButton(new ElementButton(config.getString("Ui.Button.CreateSurvey")));
        surveyForm.addButton(new ElementButton(config.getString("Ui.Button.DeleteSurvey")));
        surveyForm.addButton(new ElementButton(config.getString("Ui.Button.ClosedSurveys")));
        player.showFormWindow(surveyForm);
    }

    public static void openCreationMenu(Player player) {
        FormWindowCustom form = new FormWindowCustom(config.getString("Ui.Title.CreateSurvey"));
        ElementInput inputTitle = new ElementInput(config.getString("Ui.Text.CreateTitle"), "Title");
        ElementInput inputText = new ElementInput(config.getString("Ui.Text.CreateQuestion"), "Text");
        ElementInput inputTime = new ElementInput(config.getString("Ui.Text.CreateTime"), "Time in hours");
        form.addElement(inputTitle);
        form.addElement(inputText);
        form.addElement(inputTime);
        player.showFormWindow(form);
    }

    public static void openDeleteMenu(Player player) {
        FormWindowSimple surveyForm = new FormWindowSimple(config.getString("Ui.Title.DeleteSurvey"), config.getString("Ui.Text.DeleteSurvey"));
        int count = 0;
        if (provider.getProvider().equalsIgnoreCase("MongoDB")) {
            for (Document document : MongoDBManager.getSurveyCollection().find(new Document("status", "Closed"))) {
                surveyForm.addButton(new ElementButton(document.getString("title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("MySql")) {
            try {
                PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE STATUS = ?");
                preparedStatement.setString(1, "Closed");
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    count++;
                    surveyForm.addButton(new ElementButton(rs.getString("TITLE")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("Yaml")) {
            Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
            List<String> list = survey.getStringList("Closed");
            for (String s : list) {
                surveyForm.addButton(new ElementButton(survey.getString("Survey." + s + ".Title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        }
        surveyForm.addButton(new ElementButton(config.getString("Ui.Button.Back")));
        player.showFormWindow(surveyForm);
    }

    public static void openClosedSurveyMenu(Player player) {
        FormWindowSimple surveyForm = new FormWindowSimple(config.getString("Ui.Title.ClosedSurveys"), config.getString("Ui.Text.ClosedSurveys"));
        int count = 0;
        if (provider.getProvider().equalsIgnoreCase("MongoDB")) {
            for (Document document : MongoDBManager.getSurveyCollection().find(new Document("status", "Closed"))) {
                surveyForm.addButton(new ElementButton(document.getString("title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("MySql")) {
            try {
                PreparedStatement preparedStatement = MySqlManager.getConnection().prepareStatement("SELECT * FROM surveys WHERE STATUS = ?");
                preparedStatement.setString(1, "Closed");
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    count++;
                    surveyForm.addButton(new ElementButton(rs.getString("TITLE")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        } else if (provider.getProvider().equalsIgnoreCase("Yaml")) {
            Config survey = new Config(SurveySystem.getInstance().getDataFolder() + "/data/survey-data.yml", Config.YAML);
            List<String> list = survey.getStringList("Closed");
            for (String s : list) {
                surveyForm.addButton(new ElementButton(survey.getString("Survey." + s + ".Title")));
                count++;
            }
            if (count == 0) surveyForm.addButton(new ElementButton(config.getString("Ui.Button.NothingToDisplay")));
        }
        surveyForm.addButton(new ElementButton(config.getString("Ui.Button.Back")));
        player.showFormWindow(surveyForm);
    }
}
