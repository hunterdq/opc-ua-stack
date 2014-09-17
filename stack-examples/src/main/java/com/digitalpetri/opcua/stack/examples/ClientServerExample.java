package com.digitalpetri.opcua.stack.examples;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import com.digitalpetri.opcua.stack.core.types.structured.TestStackResponse;
import com.digitalpetri.opcua.stack.examples.client.ClientExample;
import com.digitalpetri.opcua.stack.examples.server.ServerExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientServerExample {

    private static final String CLIENT_ALIAS = "client-test-certificate";
    private static final String SERVER_ALIAS = "server-test-certificate";
    private static final char[] PASSWORD = "test".toCharArray();

    public static void main(String[] args) throws Exception {
        new ClientServerExample();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ClientServerExample() throws Exception {
        KeyStoreLoader loader = new KeyStoreLoader().load();

        ServerExample server = new ServerExample(loader.getServerCertificate(), loader.getServerKeyPair());
        server.startup();

        ClientExample client = new ClientExample(loader.getClientCertificate(), loader.getClientKeyPair());

        for (int i = 0; i < 5; i++) {
            logger.info("Sending synchronous TestStackRequest input={}", i);
            TestStackResponse response = client.testStack(i).get();
            logger.info("Received TestStackResponse output={}", response.getOutput());
        }

        for (int i = 0; i < 5; i++) {
            logger.info("Sending asynchronous TestStackRequest input={}", i);

            client.testStack(i).whenComplete((response, ex) -> {
                if (response != null) {
                    logger.info("Received TestStackResponse output={}", response.getOutput());
                } else {
                    logger.error("Error: {}", ex.getMessage(), ex);
                }
            });
        }
    }

    private class KeyStoreLoader {

        private Certificate serverCertificate;
        private KeyPair serverKeyPair;
        private Certificate clientCertificate;
        private KeyPair clientKeyPair;

        public KeyStoreLoader load() throws Exception {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getClass().getClassLoader().getResourceAsStream("example-keystore.pfx"), PASSWORD);

            Key serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD);
            if (serverPrivateKey instanceof PrivateKey) {
                serverCertificate = keyStore.getCertificate(SERVER_ALIAS);
                PublicKey serverPublicKey = serverCertificate.getPublicKey();
                serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
            }

            Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
            if (clientPrivateKey instanceof PrivateKey) {
                clientCertificate = keyStore.getCertificate(CLIENT_ALIAS);
                PublicKey clientPublicKey = clientCertificate.getPublicKey();
                clientKeyPair = new KeyPair(clientPublicKey, (PrivateKey) clientPrivateKey);
            }

            return this;
        }

        public Certificate getServerCertificate() {
            return serverCertificate;
        }

        public KeyPair getServerKeyPair() {
            return serverKeyPair;
        }

        public Certificate getClientCertificate() {
            return clientCertificate;
        }

        public KeyPair getClientKeyPair() {
            return clientKeyPair;
        }

    }


}
