/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.api.kafka.model.kafka;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.strimzi.api.kafka.model.AbstractCrdIT;
import io.strimzi.test.CrdUtils;
import io.strimzi.test.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The purpose of this test is to confirm that we can create a
 * resource from the POJOs, serialize it and create the resource in K8S.
 * I.e. that such instance resources obtained from POJOs are valid according to the schema
 * validation done by K8S.
 */
public class KafkaCrdIT extends AbstractCrdIT {
    public static final String NAMESPACE = "kafkacrd-it";

    @Test
    void testKafkaIsNotScaling() {
        assertThrows(KubernetesClientException.class, () -> createScaleDelete(Kafka.class, "Kafka.yaml"));
    }

    @Test
    void testKafkaMinimal() {
        createDeleteCustomResource("Kafka-minimal.yaml");
    }

    @Test
    void testKafkaWithMissingRequired() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-missing-required-property.yaml"));

        assertMissingRequiredPropertiesMessage(exception.getMessage(), "kafka");
    }

    @Test
    public void testKafkaWithEntityOperator() {
        createDeleteCustomResource("Kafka-with-entity-operator.yaml");
    }

    @Test
    public void testKafkaWithMaintenance() {
        createDeleteCustomResource("Kafka-with-maintenance.yaml");
    }

    // @Test
    // public void testKafkaWithNullMaintenance() {
    //     Throwable exception = assertThrows(
    //             KubernetesClientException.class,
    //             () -> createDeleteCustomResource("Kafka-with-null-maintenance.yaml"));

    //     assertThat(exception.getMessage(), containsStringIgnoringCase("invalid: spec.maintenanceTimeWindows: Invalid value: \"null\": spec.maintenanceTimeWindows in body must be of type string: \"null\""));
    // }
    @Test
    public void testKafkaWithNullMaintenance() {
        System.out.println("Starting testKafkaWithNullMaintenance...");

        // Attempt to create and delete the custom resource
        try {    
            System.out.println("About to create the custom resource...");
    
            // This is the actual method that should trigger the exception
            Throwable exception = assertThrows(
                    KubernetesClientException.class,
                    () -> createDeleteCustomResource("Kafka-with-null-maintenance.yaml"));
    
            // Print the exception details to understand the exact cause
            System.out.println("Exception caught: " + exception.getMessage());
    
            // Check for expected exception message
            // String expectedMessageWithIndex = "invalid: spec.maintenanceTimeWindows[0]: Invalid value: \"null\": spec.maintenanceTimeWindows[0] in body must be of type string: \"null\"";
            String expectedMessageWithoutIndex = "invalid: spec.maintenanceTimeWindows: Invalid value: \"null\": spec.maintenanceTimeWindows in body must be of type string: \"null\"";
            String actualMessage = exception.getMessage();
    
            // Print the actual and expected messages
            // System.out.println("Expected message with [0]: " + expectedMessageWithIndex);
            System.out.println("Expected message without [0]: " + expectedMessageWithoutIndex);
            System.out.println("Actual message: " + actualMessage);

            // assertThat(exception.getMessage(), containsStringIgnoringCase(expectedMessageWithIndex));
            assertThat(exception.getMessage(), containsStringIgnoringCase(expectedMessageWithoutIndex));    
        } catch (Exception e) {
            // Print any unexpected exceptions
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    
        System.out.println("Finished testKafkaWithNullMaintenance.");
    }


    @Test
    public void testKafkaWithTemplate() {
        createDeleteCustomResource("Kafka-with-template.yaml");
    }

    @Test
    public void testKafkaWithJbodStorage() {
        createDeleteCustomResource("Kafka-with-jbod-storage.yaml");
    }

    @Test
    public void testKafkaWithJbodStorageOnZookeeper() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-jbod-storage-on-zookeeper.yaml"));

        assertThat(exception.getMessage(), containsStringIgnoringCase("spec.zookeeper.storage.type: Unsupported value: \"jbod\": supported values: \"ephemeral\", \"persistent-claim\""));
    }

    @Test
    public void testKafkaWithInvalidStorage() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-invalid-storage.yaml"));

        assertThat(exception.getMessage(), containsStringIgnoringCase("spec.kafka.storage.type: Unsupported value: \"foobar\": supported values: \"ephemeral\", \"persistent-claim\", \"jbod\""));
    }

    @Test
    public void testKafkaWithInvalidJmxAuthentication() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-invalid-jmx-authentication.yaml"));

        assertThat(exception.getMessage(), containsStringIgnoringCase("spec.kafka.jmxOptions.authentication.type: Unsupported value: \"not-right\": supported values: \"password\""));
    }

    @Test
    public void testKafkaWithInvalidZookeeperJmxAuthentication() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-invalid-zookeeper-jmx-authentication.yaml"));

        assertThat(exception.getMessage(), containsStringIgnoringCase("spec.zookeeper.jmxOptions.authentication.type: Unsupported value: \"not-right\": supported values: \"password\""));
    }

    @Test
    public void testKafkaWithAutoRebalanceDefault() {
        createDeleteCustomResource("Kafka-with-autorebalance-default.yaml");
    }

    @Test
    public void testKafkaWithAutoRebalance() {
        createDeleteCustomResource("Kafka-with-autorebalance.yaml");
    }

    @Test
    public void testKafkaWithAutoRebalanceEmpty() {
        Throwable exception = assertThrows(
                KubernetesClientException.class,
                () -> createDeleteCustomResource("Kafka-with-autorebalance-empty.yaml"));
        assertThat(exception.getMessage(), containsStringIgnoringCase("spec.cruiseControl.autoRebalance in body should have at least 1 items"));
    }

    @BeforeAll
    void setupEnvironment() {
        client = new KubernetesClientBuilder().withConfig(new ConfigBuilder().withNamespace(NAMESPACE).build()).build();
        CrdUtils.createCrd(client, Kafka.CRD_NAME, CrdUtils.CRD_KAFKA);
        TestUtils.createNamespace(client, NAMESPACE);
    }

    @AfterAll
    void teardownEnvironment() {
        CrdUtils.deleteCrd(client, Kafka.CRD_NAME);
        TestUtils.deleteNamespace(client, NAMESPACE);
        client.close();
    }
}
