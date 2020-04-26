
package com.a2937.roleplugin;

import com.a2937.roleplugin.handlers.RoleEventHandler;
import com.a2937.roleplugin.migrations.CreateRoleTrackerTable;
import com.avairebot.config.EnvironmentOverride;
import com.avairebot.database.collection.DataRow;
import com.avairebot.plugin.JavaPlugin;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.dv8tion.jda.core.entities.Role;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class RoleRestoreMain extends JavaPlugin {

    public static final String ROLE_TABLE = "roles";
    public static final Logger LOGGER = LoggerFactory.getLogger(RoleRestoreMain.class);
    private final Table<Long,Long,String> roleCache= HashBasedTable.create();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        registerEventListener(new RoleEventHandler(this));
        registerMigration(new CreateRoleTrackerTable());
        EnvironmentOverride.overrideWithPrefix("RoleRegister", getConfig());
    }

    /** Write the object to a Base64 string. */
    private static String convertoString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static Object convertfromString( String s ) throws IOException ,
        ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    public List<Role> getUserRolesFromGuild(long userId,long guildId)
    {
        String encodedValue ="";
        if (roleCache.contains(userId,guildId )) {
            encodedValue = roleCache.get(userId,guildId);
        }
        try
        {
            DataRow first = getAvaire().getDatabase().newQueryBuilder(ROLE_TABLE)
                .select("roleList")
                .where("guild_id",guildId)
                .andWhere("user_id",userId)
                .get()
                .first();
            if(first == null)
            {
                return Collections.emptyList();
            }
            encodedValue = first.getString("roleList",null);
            roleCache.put(userId,guildId,encodedValue);
            if(!StringUtils.isEmpty(encodedValue))
            {
                return (List<Role>)convertfromString(encodedValue);
            }
            else
            {
                return Collections.emptyList();
            }

        }
        catch (SQLException e)
        {
            LOGGER.error("Failed to fetch the user role list values from the database for: " + guildId, e);
        } catch (IOException | ClassNotFoundException e)
        {
            LOGGER.error("Failed to read serialized role list for : " + guildId, e);
        }
        return Collections.emptyList();
    }

    public void saveRoles(long userId, long guildId, ArrayList<Role> roles)
    {
        try {
            String rolesAsString = convertoString(roles);
             getAvaire().getDatabase().newQueryBuilder(ROLE_TABLE)
                .insert(statement ->
                {
                    statement.set("user_id",userId);
                    statement.set("guild_id",guildId);
                    statement.set("roleList", rolesAsString);
                });
        }
        catch (SQLException e)
        {
            RoleRestoreMain.LOGGER.error("Failed to insert the roles for: " + userId + " on guild " + guildId,e);
        } catch (IOException e)
        {
            RoleRestoreMain.LOGGER.error("Failed to serialize roles");
        }
    }
}
