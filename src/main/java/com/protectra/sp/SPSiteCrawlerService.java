package com.protectra.sp;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.microsoft.graph.drives.item.items.item.deltawithtoken.DeltaWithTokenGetResponse;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveCollectionResponse;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.sites.delta.DeltaGetResponse;
import com.microsoft.graph.sites.delta.DeltaRequestBuilder.GetRequestConfiguration;
import com.protectra.sp.entity.Asset;
import com.protectra.sp.entity.DeltaTokenEntity;
import com.protectra.sp.repo.AssetRepository;
import com.protectra.sp.repo.DeltaTokenRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class SPSiteCrawlerService {

	private static Logger logger = LoggerFactory.getLogger(SPSiteCrawlerService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final GraphServiceClient graphClient;

    public SPSiteCrawlerService(GraphServiceClient graphClient) {
        this.graphClient = graphClient;
    }
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private AssetRepository assetRepository; // Repository to check and save assets
    
    @Autowired
    DeltaTokenRepository deltaTokenRepository;

    // Start the task after the Spring Bean has been initialized
    @PostConstruct
    public void startScheduledTask() {
    	  System.out.println("SPSiteCrawlerService Scheduling the task..&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&.");
        // Schedule the SharePoint site job to run immediately and then every 5 minutes
        scheduler.scheduleAtFixedRate(
                this::fetchAndStoreSharePointSites,
                0,  // Initial delay: 0 means it runs immediately
                2,  // Repeat every 1minutes
                TimeUnit.MINUTES
        );
    }

 public void fetchAndStoreSharePointSites() {    
    	
        List<Site> sites = getAllSites();
        // Iterate over all sites and check if they exist in the Asset collection
        for (Site site : sites) {
            boolean siteAlreadyExists = isSiteAlreadyExists(site.getId());
			if (!siteAlreadyExists) {
                Asset asset = new Asset();
                asset.setSiteId(site.getId());
                asset.setSiteName(site.getDisplayName());
                assetRepository.save(asset); // Save to MongoDB
                logger.debug("Saved new site: " + site.getDisplayName());
            } else {
                logger.debug("Site already exists: " + site.getDisplayName());
            }
        }
    }

    // Check if the site already exists in the Asset collection
    private boolean isSiteAlreadyExists(String siteId) {
        return assetRepository.existsBySiteId(siteId); // Check for existence by siteId
    }
    // Gracefully shut down the scheduler when the application is stopped
    @PreDestroy
    public void stopScheduledTask() {
        scheduler.shutdown();
    }
    

	public List<com.microsoft.graph.models.Site> getAllSites() {
		try {

			logger.debug("..$$$$$$$$$$$$$$$$$$$$$$$$....getAllSites.....");
			Consumer<GetRequestConfiguration> configuration = requestConfiguration -> {
				requestConfiguration.queryParameters.select = new String[] { "displayName", "id" };
				requestConfiguration.queryParameters.top = 20;
				requestConfiguration.headers.add("Prefer", "odata.maxpagesize=100");
			};
			DeltaGetResponse deltaGetResponse = graphClient.sites().delta().get(configuration);

			List<com.microsoft.graph.models.Site> sites = new ArrayList<>();
			sites.addAll(deltaGetResponse.getValue());
			String oDataNextLink = deltaGetResponse.getOdataNextLink();
			logger.debug(sites.size() + "..$$$$$$$$$$$$$$$$$$$$$$$$....deltalink....." + oDataNextLink);

			do {
				if (oDataNextLink != null) {
					deltaGetResponse = graphClient.sites().delta().withUrl(oDataNextLink).get(configuration);

					oDataNextLink = deltaGetResponse.getOdataNextLink();
					sites.addAll(deltaGetResponse.getValue());
					logger.debug(".... sites count...." + sites.size());

				} else {
					logger.debug("....no delta link...");

				}
			} while (oDataNextLink != null);

			logger.debug(".....total sites...." + sites.size());

			return sites;
		} catch (Exception e) {
			System.err.println("Error fetching sites: " + e.getMessage());
			return Collections.emptyList();
		}
	}
	
	public void fetchSharePointFiles(String siteId) {

		// Container for all items.
		List<DriveItem> allItems = new ArrayList<>();

		// ---------------------------------------------------------------------
		// Step 1: Execute the initial delta query.
		// ---------------------------------------------------------------------
		Site site = graphClient.sites().bySiteId(siteId).get();
		DriveCollectionResponse driveCollectionResponse = graphClient.sites().bySiteId(siteId).drives().get();
		List<Drive> allDrives = driveCollectionResponse.getValue();

		// Container to hold all file items (non-folders) from all document libraries.
		List<DriveItem> allFileItems = new ArrayList<>();

		// Process each drive. Here, we only process the drive named "Documents".
		for (Drive drive : allDrives) {
		
			 if (!drive.getName().equals("myLibrary")) continue;
			String driveId = drive.getId();
			DriveItem rootItem = graphClient.drives().byDriveId(drive.getId()).root().get();
			String rootItemId = rootItem.getId();
			System.out.println(drive.getDriveType()+"...Executing initial delta query..."+drive.getName());
			DeltaTokenEntity tokenEntityObj = null;
			
			Optional<DeltaTokenEntity> deltaTokenEntity = deltaTokenRepository.findBySiteIdAndDriveId(siteId, driveId);
			String token = "";
			if (deltaTokenEntity.isPresent()) {
				token = deltaTokenEntity.get().getDeltaToken();
			}
			
			Consumer<com.microsoft.graph.drives.item.items.item.delta.DeltaRequestBuilder.GetRequestConfiguration> deltaConsumer = requestConfiguration -> {
		//		requestConfiguration.queryParameters.select = new String[] { "displayName", "id"};
				requestConfiguration.queryParameters.top = 10;
				 requestConfiguration.queryParameters.filter = "file ne null"; 
				requestConfiguration.headers.add("Prefer", "odata.maxpagesize=100");
			};
			
			// Continue delta query using the token for pagination.
			Consumer<com.microsoft.graph.drives.item.items.item.deltawithtoken.DeltaWithTokenRequestBuilder.GetRequestConfiguration> deltaTokenConsumer = requestConfiguration -> {
			//	requestConfiguration.queryParameters.select = new String[] { "displayName", "id"};
				requestConfiguration.queryParameters.top = 10;
			//	 requestConfiguration.queryParameters.filter = "file ne null"; 
				requestConfiguration.headers.add("Prefer", "odata.maxpagesize=100");
			};

			String  nextLink = null;
			com.microsoft.graph.drives.item.items.item.delta.DeltaGetResponse deltaResponse = null;
			if (org.apache.commons.lang3.StringUtils.isEmpty(token)) {
				// Initial delta query (no token)
				
			
				deltaResponse = graphClient.drives().byDriveId(driveId).items().byDriveItemId(rootItem.getId()).delta()
						.get( deltaConsumer);
				if (deltaResponse.getValue() != null) {
					allItems.addAll(deltaResponse.getValue());
				}
				nextLink = deltaResponse.getOdataNextLink();
			} else {
				
				com.microsoft.graph.drives.item.items.item.deltawithtoken.DeltaWithTokenGetResponse tokenResponse = graphClient
						.drives().byDriveId(driveId).items().byDriveItemId(rootItem.getId()).deltaWithToken(token)
						.get(deltaTokenConsumer);
				// Process the token response:
				if (tokenResponse.getValue() != null) {
					allItems.addAll(tokenResponse.getValue());
					System.out
							.println("Delta with token query returned " + tokenResponse.getValue().size() + " items.");
				}

				// Update your token for the next iteration.
				nextLink = tokenResponse.getOdataNextLink();
				
				
			}
			if (deltaTokenEntity.isPresent()) {
			    // Optional contains a value
				tokenEntityObj= deltaTokenEntity.get();
			    // Use entity
			} else {					
				tokenEntityObj = new DeltaTokenEntity();
				tokenEntityObj.setDriveId(driveId);
				tokenEntityObj.setSiteId(siteId);
				tokenEntityObj.setSiteName(site.getName());
				tokenEntityObj.setDriveName(drive.getName());
			}
			tokenEntityObj.setDeltaToken(token);
		    deltaTokenRepository.save(tokenEntityObj);
			
			
			
			System.out.println("Initial delta query returned " + allItems.size() + " items.");

			// ---------------------------------------------------------------------
			// Step 2: Use pagination via deltaWithToken to get all items.
			
			
			
			while (!StringUtils.isEmpty(nextLink)) {
				System.out.println("\nNext link detected: " + nextLink);
				
				if (!StringUtils.isEmpty(nextLink)) {
					token = extractToken(nextLink);
				}
				// Use deltaWithToken with the nextLink (or extract the $skiptoken if needed).
				if (org.apache.commons.lang3.StringUtils.isEmpty(token))
					break;
	
				DeltaWithTokenGetResponse tokenResponse = graphClient.drives().byDriveId(driveId).items()
						.byDriveItemId(rootItemId).deltaWithToken(token).get(deltaTokenConsumer);

				if (tokenResponse.getValue() != null) {
					allItems.addAll(tokenResponse.getValue());
					System.out
							.println("Delta with token query returned " + tokenResponse.getValue().size() + " items.");
				}
				
				if (deltaTokenEntity.isPresent()) {
				    // Optional contains a value
					tokenEntityObj= deltaTokenEntity.get();
				    // Use entity
				} else {					
					tokenEntityObj = new DeltaTokenEntity();
					tokenEntityObj.setDriveId(driveId);
					tokenEntityObj.setSiteId(siteId);
				}
				tokenEntityObj.setSiteName(site.getName());
				tokenEntityObj.setDriveName(drive.getName());
				tokenEntityObj.setDeltaToken(token);
			    deltaTokenRepository.save(tokenEntityObj);
				// Update nextLink for the next iteration.
				nextLink = tokenResponse.getOdataNextLink();
			}

			// ---------------------------------------------------------------------
			// Final Output: All retrieved items.
			// ---------------------------------------------------------------------
			System.out.println("\nTotal items retrieved: " + allItems.size());
			for (DriveItem item : allItems) {

				if(item.getFolder()!=null || org.apache.commons.lang3.StringUtils.isEmpty(item.getName()))
					continue;
				String folderPath = "Root"; // default if no parentReference is set
				if (item.getParentReference() != null && item.getParentReference().getPath() != null) {
					folderPath = org.apache.commons.lang3.StringUtils.substringAfterLast(item.getParentReference().getPath(), "root:");
				}
				 System.out.println("File: " + item.getName() + " Folder Path: " +folderPath);
				
				 
				 if("root".equalsIgnoreCase(item.getName())) {
					 System.out.println("....."+item.getName()+"....."+item.getParentReference()+"........"+item.getWebUrl());
				 }
				 kafkaTemplate.send("backup-file", item.getId());

			}

		}
		System.out.println("....." + allItems.size());
	}

	
	
	private static String extractToken(String nextLink) {
	    String token = "";
	    try {
	        // First, try to extract the token using regex from the entire URL.
	        Pattern pattern = Pattern.compile("delta\\(token='(.+?)'\\)");
	        Matcher matcher = pattern.matcher(nextLink);
	        if (matcher.find()) {
	            token = matcher.group(1);
	        }
	        
	        // If the regex didn't find a token, check the query parameters.
	        if (StringUtils.isEmpty(token)) {
	            URL url = new URL(nextLink);
	            String query = url.getQuery();
	            if (query != null) {
	                String[] params = query.split("&");
	                for (String param : params) {
	                    String[] keyValue = param.split("=");
	                    if (keyValue.length == 2 &&
	                        (keyValue[0].equalsIgnoreCase("$skiptoken") || keyValue[0].equalsIgnoreCase("token"))) {
	                        token = keyValue[1];
	                        break;
	                    }
	                }
	            }
	        }
	        
	        if (!StringUtils.isEmpty(token)) {
	            token = URLDecoder.decode(token, StandardCharsets.UTF_8);
	            System.out.println("Extracted token: " + token);
	        } else {
	            System.out.println("Token parameter not found in the nextLink URL.");
	        }
	    } catch (Exception e) {
	        System.err.println("Error extracting token: " + e.getMessage());
	    }
	    return token;
	}

	
	
}
