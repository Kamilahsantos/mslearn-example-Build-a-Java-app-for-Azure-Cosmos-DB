// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples.mslearnbasicapp;

import com.azure.cosmos.*;
import com.azure.cosmos.examples.mslearnbasicapp.datatypes.CouponsUsed;
import com.azure.cosmos.examples.mslearnbasicapp.datatypes.OrderHistory;
import com.azure.cosmos.examples.mslearnbasicapp.datatypes.ShippingPreference;
import com.azure.cosmos.examples.mslearnbasicapp.datatypes.User;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CosmosApp {


  /**
   * Azure Cosmos DB client instance.
   */
  private static CosmosAsyncClient client;

  /**
   * Azure Cosmos DB database instance.
   */
  private static CosmosAsyncDatabase database;

  /**
   * Azure Cosmos DB container instance.
   */
  private static CosmosAsyncContainer container;

  /**
   * Azure Cosmos DB endpoint URI.
   */
  private static String endpointUri = "https://deva.documents.azure.com:443/";

  /**
   * Azure Cosmos DB primary key.
   */
  private static String primaryKey = "{your-primary-key}";


  private static Logger logger = LoggerFactory.getLogger(CosmosApp.class.getSimpleName());


  private void basicOperations() {
    client = new CosmosClientBuilder()
      .endpoint(endpointUri)
      .key(primaryKey)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .directMode()
      .contentResponseOnWriteEnabled(true)
      .buildAsyncClient();

    database = client.getDatabase("Users");
    container = database.getContainer("WebCustomers");


    logger.info("Database and container validation complete");


    User maxaxam = new User(
      "1",
      "maxaxam",
      "Axam",
      "Max",
      "maxaxam@contoso.com",
      "2.0",
      new ShippingPreference(
        1,
        "90 W 8th St",
        "",
        "New York",
        "NY",
        "10001",
        "USA"
      ),
      new ArrayList<OrderHistory>(Arrays.asList(
        new OrderHistory(
          "3",
          "1000",
          "08/17/2018",
          "52.49"
        )
      )),
      new ArrayList<CouponsUsed>(Arrays.asList(
        new CouponsUsed(
          "A7B89F"
        )
      ))
    );

    User nelapin = new User(
      "2",
      "nelapin",
      "Pindakova",
      "Nela",
      "nelapin@contoso.com",
      "8.50",
      new ShippingPreference(
        1,
        "505 NW 5th St",
        "",
        "New York",
        "NY",
        "10001",
        "USA"
      ),
      new ArrayList<OrderHistory>(Arrays.asList(
        new OrderHistory(
          "4",
          "1001",
          "08/17/2018",
          "105.89"
        )
      )),
      new ArrayList<CouponsUsed>(Arrays.asList(
        new CouponsUsed(
          "Fall 2018"
        )
      ))
    );

    createUserDocumentsIfNotExist(new ArrayList(Arrays.asList(maxaxam, nelapin)));

    readUserDocument(maxaxam);

    maxaxam.setLastName("Suh");
    replaceUserDocument(maxaxam);
    executeSimpleQuery("SELECT * FROM User WHERE User.lastName = 'Pindakova'");
    deleteUserDocument(maxaxam);
    client.close();

  }

  private CosmosApp() {
  }

  private static void createUserDocumentsIfNotExist(final List<User> users) {
    Flux.fromIterable(users).flatMap(user -> {
      try {
        container.readItem(user.getId(), new PartitionKey(user.getUserId()), User.class).block();

        logger.info("User {} already exists in the database", user.getId());
        return Mono.empty();
      } catch (Exception err) {
        logger.info("Creating User {}", user.getId());

        return container.createItem(user, new PartitionKey(user.getUserId()), new CosmosItemRequestOptions());
      }
    }).blockLast();
  }


  private static CosmosItemResponse<User> readUserDocument(final User user) {
    CosmosItemResponse<User> userReadResponse = null;

    try {
      userReadResponse = container.readItem(user.getId(), new PartitionKey(user.getUserId()), User.class).block();
      logger.info("Read user {}", user.getId());
    } catch (CosmosException de) {
      logger.error("Failed to read user {}", user.getId(), de);
    }

    return userReadResponse;
  }


  private static void replaceUserDocument(final User user) {
    try {
      CosmosItemResponse<User> userReplaceResponse = container.replaceItem(user, user.getId(), new PartitionKey(user.getUserId())).block();
      logger.info("Replaced User {}", user.getId());
    } catch (CosmosException de) {
      logger.error("Failed to replace User {}", user.getUserId());
    }
  }


  private static void deleteUserDocument(final User user) {
    try {
      container.deleteItem(user.getId(), new PartitionKey(user.getUserId())).block();
      logger.info("Deleted user {}", user.getId());
    } catch (CosmosException de) {
      logger.error("User {} could not be deleted.", user.getId());
    }
  }


  public static void main(final String[] args) {
    try {
      CosmosApp p = new CosmosApp();
      p.basicOperations();
    } catch (CosmosException e) {
      logger.error("Failed while executing app.", e);
    } finally {
      logger.info("End of demo, press any key to exit.");
    }
  }

  private static void executeSimpleQuery(final String query) {

    final int preferredPageSize = 10;
    CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

    CosmosPagedFlux<User> pagedFluxResponse = container.queryItems(
      query, queryOptions, User.class);

    logger.info("Running SQL query...");

    pagedFluxResponse.byPage(preferredPageSize).flatMap(fluxResponse -> {
      logger.info("Got a page of query result with " + fluxResponse.getResults().size()
        + " items(s) and request charge of " + fluxResponse.getRequestCharge());

      logger.info("Item Ids " + fluxResponse
        .getResults()
        .stream()
        .map(User::getId)
        .collect(Collectors.toList()));

      return Flux.empty();
    }).blockLast();
  }
}
