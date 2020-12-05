package net.lldv.surveysystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import net.lldv.surveysystem.SurveySystem;

public class SurveyCommand extends PluginCommand<SurveySystem> {

    public SurveyCommand(SurveySystem owner) {
        super(owner.getConfig().getString("Commands.Survey.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Survey.Description"));
        this.setAliases(owner.getConfig().getStringList("Commands.Survey.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SurveySystem.getApi().getFormWindows().openSurveys(player);
        }
        return true;
    }

}
