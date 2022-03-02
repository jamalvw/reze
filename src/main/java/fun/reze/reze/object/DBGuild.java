package fun.reze.reze.object;

import fun.reze.reze.object.json.DBGuildData;

public class DBGuild
{
    private final DBGuildData data;

    public DBGuild(DBGuildData data)
    {
        this.data = data;
    }

    public static DBGuild create(String id)
    {
        DBGuildData data = new DBGuildData();
        data.id = id;
        return new DBGuild(data);
    }

    public DBGuildData getData()
    {
        return data;
    }
}
