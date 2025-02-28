package com.protectra.sp;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.core.authentication.AzureIdentityAuthenticationProvider;
import com.microsoft.graph.drives.item.items.item.deltawithtoken.DeltaWithTokenGetResponse;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveCollectionResponse;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AuthenticationProvider;

@Configuration
public class GraphClientConfig {
	private static final Logger logger = LoggerFactory.getLogger(GraphClientConfig.class);
	private GraphServiceClient graphClient;
	
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

	@Bean
	public GraphServiceClient getGraphServiceClient(@Value("${graph.tenant-id}") String tenantId,
			@Value("${graph.client-id}") String clientId, @Value("${graph.client-secret}") String clientSecret) {
		ClientSecretCredential credential = new ClientSecretCredentialBuilder().clientId(clientId)
				.clientSecret(clientSecret).tenantId(tenantId).build();

		AuthenticationProvider authenticationProvider = new AzureIdentityAuthenticationProvider(credential, null,
				"https://graph.microsoft.com/.default");
		this.graphClient = new GraphServiceClient(authenticationProvider);
		return graphClient;
	}

	public List<User> getAllUsers() {

		try {

			logger.debug("..$$$$$$$$$$$$$$$$$$$$$$$$....getAllUsers.....");

			Consumer<com.microsoft.graph.users.delta.DeltaRequestBuilder.GetRequestConfiguration> configuration = requestConfiguration -> {
				requestConfiguration.queryParameters.select = new String[] { "displayName", "id", "mail", "jobTitle" };
				requestConfiguration.queryParameters.top = 20;
				requestConfiguration.headers.add("Prefer", "odata.maxpagesize=100");
			};

			com.microsoft.graph.users.delta.DeltaGetResponse deltaGetResponse = graphClient.users().delta()
					.get(configuration);

			List<com.microsoft.graph.models.User> users = new ArrayList<>();
			users.addAll(deltaGetResponse.getValue());
			String oDataNextLink = deltaGetResponse.getOdataNextLink();
			logger.debug(users.size() + "..$$$$$$$$$$$$$$$$$$$$$$$$....deltalink....." + oDataNextLink);

			do {
				if (oDataNextLink != null) {
					deltaGetResponse = graphClient.users().delta().withUrl(oDataNextLink).get(configuration);

					oDataNextLink = deltaGetResponse.getOdataNextLink();
					users.addAll(deltaGetResponse.getValue());
					logger.debug(".... sites count...." + users.size());

				} else {
					logger.debug("....no delta link...");

				}
			} while (oDataNextLink != null);

			logger.debug(".....total users...." + users.size());

			return users;
		} catch (Exception e) {
			System.err.println("Error fetching users: " + e.getMessage());
			return Collections.emptyList();
		}
	}


	
}
