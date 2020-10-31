// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples.mslearnbasicapp.datatypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  /** Document ID (required by Azure Cosmos DB). */
  private String id;

  private String userId;

  private String lastName;

  private String firstName;

  private String email;

  private String dividend;

  private ShippingPreference shippingPreference;

  private List<OrderHistory> orderHistory;

  private List<CouponsUsed> coupons;
}
