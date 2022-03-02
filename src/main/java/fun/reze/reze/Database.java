package fun.reze.reze;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.connection.ServerConnectionState;
import fun.reze.reze.object.DBGuild;
import fun.reze.reze.object.json.DBGuildData;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.HashMap;
import java.util.Map;

public class Database
{
    private final MongoClient client;
    private final MongoDatabase database;

    public Database(String uri, String databaseName)
    {
        // Add BSON/POJO translator
        CodecRegistry pojoRegistry = CodecRegistries
                .fromProviders(PojoCodecProvider.builder()
                        .automatic(true)
                        .build());
        // Add default codec for Java types
        CodecRegistry registry = CodecRegistries
                .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoRegistry);
        // Create settings
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .retryWrites(true)
                .codecRegistry(registry)
                .build();
        client = MongoClients.create(settings);
        database = client.getDatabase(databaseName);
    }

    public ServerConnectionState getState()
    {
        return client.getClusterDescription().getServerDescriptions().get(0).getState();
    }

    private MongoClient getClient()
    {
        return client;
    }

    private MongoDatabase getDatabase()
    {
        return database;
    }

    public MongoCollection<DBGuildData> fetchGuilds()
    {
        return database.getCollection("guilds", DBGuildData.class);
    }

    public Map<String, DBGuild> createGuildMap()
    {
        Map<String, DBGuild> guildMap = new HashMap<>();
        fetchGuilds().find().forEach(data -> guildMap.put(data.id, new DBGuild(data)));
        return guildMap;
    }

    public void saveGuild(DBGuildData data)
    {
        fetchGuilds().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
}
