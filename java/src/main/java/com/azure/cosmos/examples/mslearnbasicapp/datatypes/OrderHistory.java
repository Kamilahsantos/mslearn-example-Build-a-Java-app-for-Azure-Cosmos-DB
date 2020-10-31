// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples.mslearnbasicapp.datatypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory {

    /**
     * Document ID (Azure Cosmos DB requirement).
     */
    private String id;


    private String orderId;


    private String dateShipped;


    private String total;
}
