package net.llamadevelopment.surveysystem.components.utils;

import net.llamadevelopment.surveysystem.SurveySystem;

public class Configuration {

    public String getAndReplace(String path, String... replacements) {
        String message = SurveySystem.getInstance().getConfig().getString(path);
        int i = 0;
        for (String replacement : replacements) {
            message = message.replace("[" + i + "]", replacement);
            i++;
        }
        return SurveySystem.getInstance().getConfig().getString("Messages.Prefix").replace("&", "§") + message.replace("&", "§");
    }

    public String getAndReplaceNP(String path, String... replacements) {
        String message = SurveySystem.getInstance().getConfig().getString(path);
        int i = 0;
        for (String replacement : replacements) {
            message = message.replace("[" + i + "]", replacement);
            i++;
        }
        return message.replace("&", "§");
    }

}
