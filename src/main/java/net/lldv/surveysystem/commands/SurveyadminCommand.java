package net.lldv.surveysystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import net.lldv.surveysystem.SurveySystem;
import net.lldv.surveysystem.components.language.Language;

public class SurveyadminCommand extends PluginCommand<SurveySystem> {

    public SurveyadminCommand(SurveySystem owner) {
        super(owner.getConfig().getString("Commands.Surveyadmin.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Surveyadmin.Description"));
        this.setPermission(owner.getConfig().getString("Commands.Surveyadmin.Permission"));
        this.setAliases(owner.getConfig().getStringList("Commands.Surveyadmin.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission(this.getPermission())) {
                SurveySystem.getApi().getFormWindows().openAdminPanel(((Player) sender).getPlayer());
            } else sender.sendMessage(Language.get("permission.insufficient"));
        }
        return true;
    }
}
