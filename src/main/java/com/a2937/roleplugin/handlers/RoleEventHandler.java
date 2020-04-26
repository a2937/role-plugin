package com.a2937.roleplugin.handlers;


import com.a2937.roleplugin.RoleRestoreMain;
import com.avairebot.contracts.handlers.EventListener;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleEventHandler extends EventListener
{
    private final RoleRestoreMain roleRestoreMain;

    public RoleEventHandler(RoleRestoreMain roleRestoreMain)
    {
        this.roleRestoreMain = roleRestoreMain;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        User user = event.getUser();
        List<Role> roles = roleRestoreMain.getUserRolesFromGuild(user.getIdLong(),event.getGuild().getIdLong());
        GuildController guildController = event.getGuild().getController();
        for (Role role: roles)
        {
            if (!RoleUtil.isRoleHierarchyHigher(event.getGuild().getSelfMember().getRoles(), role))
            {
                guildController.addSingleRoleToMember(event.getMember(),role).queue();
            }
        }
        //if(!roles.isEmpty())
      //  {
       //     guildController.addRolesToMember(event.getMember(),roles).queue();
      //  }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event)
    {
         User user = event.getUser();
         List<Role> roleList = user.getJDA().getRoles();
         ArrayList<Role> newList = new ArrayList<>();
         Collections.copy(roleList,newList);
         roleRestoreMain.saveRoles(user.getIdLong(),event.getGuild().getIdLong(),newList);
    }

}
