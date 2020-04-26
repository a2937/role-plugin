package com.a2937.roleplugin.migrations;

import com.a2937.roleplugin.RoleRestoreMain;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class CreateRoleTrackerTable  implements Migration
{
    @Override
    public String created_at() {
        return "Fri, April 24, 2020 7:44 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException
    {
        return schema.createIfNotExists(RoleRestoreMain.ROLE_TABLE, table ->
        {
            table.Long("user_id");
            table.Long("guild_id");
            table.String("roleList").nullable();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException
    {
        return schema.dropIfExists(RoleRestoreMain.ROLE_TABLE);
    }
}
