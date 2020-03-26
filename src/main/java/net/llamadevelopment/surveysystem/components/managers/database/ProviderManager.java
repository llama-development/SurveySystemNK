package net.llamadevelopment.surveysystem.components.managers.database;

import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.utils.SurveyUtil;

public class ProviderManager {

    public void setUp(SurveySystem instance) {
    }

    public void disconnect(SurveySystem instance) {

    }

    public void createSurvey(String title, String text, int time) {

    }

    public void deleteSurvey(String id) {

    }

    public boolean hasVoted(String player, String id) {
        return false;
    }

    public void closeSurvey(String id) {

    }

    public void updateSurvey(String id, String player, boolean type) {

    }

    public void checkSurvey() {

    }

    public String convertToID(String title) {
        return "null";
    }

    public SurveyUtil getSurvey(String id) {
        return null;
    }

    public boolean surveyExistsByTitle(String title) {
        return false;
    }

    public String getProvider() {
        return "null";
    }

}
