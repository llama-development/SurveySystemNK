package net.llamadevelopment.surveysystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.utils.FormUI;

public class SurveyCommand extends CommandManager {

    private SurveySystem plugin;

    public SurveyCommand(SurveySystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Survey"), "", "/survey");
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            SurveySystem.provider.checkSurvey();
            FormUI.openSurveys((Player) sender);
        }
        return false;
    }
}
