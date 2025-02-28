package com.protectra.sp.repo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Service
public class MongoTemplateService {
	
	public static final String MONGO_STRING = "mongodb://%s:%s@%s:%d";

  //  private final Cache<String, MongoTemplate> mongoTemplateCache;
   // private final Cache<String, MongoTemplate> mongoTemplateForColudCollectionCache;
   // private final Cache<String, MongoTemplate> mongoTemplateForMsgCache;
    @Autowired
    ApplicationContext applicationContext;

   // private final CacheManager cacheManager;

    @Value("${mongotemplate.db.host}")
    private String host;
    
    @Value("${mongotemplate.db.username}")
    private String username;

    @Value("${mongotemplate.db.password}")
    private String password;

    @Value("${mongotemplate.db.port}")
    private int port;

    @Value("${mongotemplate.db.name}")
    private String dbName;
    
 
    @Autowired
    public MongoTemplateService() {
      /*  this.cacheManager = cacheManager;
        
        if (cacheManager != null) {
            logger.debug("Injected CacheManager: " + cacheManager.getClass().getName());
        } else {
            logger.debug("No CacheManager found. Proceeding without cache.");
        }
   
        this.mongoTemplateCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATECACHE, String.class, MongoTemplate.class);
        this.mongoTemplateForColudCollectionCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORCOLUDCOLLECTIONCACHE, String.class, MongoTemplate.class);
        this.mongoTemplateForMsgCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORMSGCACHE, String.class, MongoTemplate.class);
*/
    }

  /*  

public MongoTemplateService(@Qualifier(SafeStackEntityConstant.EHCACHEMANAGER) CacheManager cacheManager) {
    logger.debug("##### Checking CacheManager Beans #####");
    for (String beanName : applicationContext.getBeanNamesForType(CacheManager.class)) {
        logger.debug("Found CacheManager bean: " + beanName);
    }
    
    this.mongoTemplateCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATECACHE, String.class, MongoTemplate.class);
    this.mongoTemplateForColudCollectionCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORCOLUDCOLLECTIONCACHE, String.class, MongoTemplate.class);
    this.mongoTemplateForMsgCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORMSGCACHE, String.class, MongoTemplate.class);
}
*/

    


 /*   @Autowired
    public MongoTemplateService(@Qualifier(SafeStackEntityConstant.EHCACHEMANAGER) CacheManager cacheManager) {
        this.mongoTemplateCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATECACHE, String.class, MongoTemplate.class);
        this.mongoTemplateForColudCollectionCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORCOLUDCOLLECTIONCACHE, String.class, MongoTemplate.class);
        this.mongoTemplateForMsgCache = cacheManager.getCache(SafeStackEntityConstant.MONGOTEMPLATEFORMSGCACHE, String.class, MongoTemplate.class);
    }
*/
    public MongoTemplate getMongoTemplate() {
        MongoTemplate mongoTemplate = null;
        //mongoTemplateCache.get(host);
        if (mongoTemplate == null) {
            mongoTemplate = createMongoTemplate();
         //   mongoTemplateCache.put(host, mongoTemplate);
        }
        return mongoTemplate;
    }

    public MongoTemplate getMongoTemplateForCouldCollection( String dbNameForCouldCollection) {
        MongoTemplate mongoTemplate = null;
         // mongoTemplateForColudCollectionCache.get(host);
        if (mongoTemplate == null) {
            mongoTemplate = createMongoTemplateForCouldCollection( dbNameForCouldCollection);
           // mongoTemplateForColudCollectionCache.put(host, mongoTemplate);
        }
        return mongoTemplate;
    }

    public MongoTemplate getMongoTemplateForMsg(String host, String dbNameForMsgCollection) {
        MongoTemplate mongoTemplate = null;
        //mongoTemplateForMsgCache.get(host);
        if (mongoTemplate == null) {
            mongoTemplate = createMongoTemplateForMsg( dbNameForMsgCollection);
          //  mongoTemplateForMsgCache.put(host, mongoTemplate);
        }
        return mongoTemplate;
    }

    private MongoTemplate createMongoTemplate() {
        String connectionString = String.format(MONGO_STRING, username, password, host, port);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return new MongoTemplate(mongoClient, dbName);
    }

    private MongoTemplate createMongoTemplateForCouldCollection( String dbNameForCouldCollection) {
        String connectionString = String.format(MONGO_STRING, username, password, host, port);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return new MongoTemplate(mongoClient, dbNameForCouldCollection);
    }

    private MongoTemplate createMongoTemplateForMsg( String dbNameForMsg) {
        String connectionString = String.format(MONGO_STRING, username, password, host, port);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return new MongoTemplate(mongoClient, dbNameForMsg);
    }
}