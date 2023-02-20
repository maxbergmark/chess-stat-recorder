package com.chessanalysis.chessparser;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.chessanalysis.util.ParseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueHandler {

    Logger logger = LoggerFactory.getLogger(QueueHandler.class);
    private static final String QUEUE_NAME = "java-chess-files";
    private final Main main;

    public QueueHandler(Main main) {
        this.main = main;
    }

    public void createConnection() throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.10.200");
        String charsetName = "UTF-8";

        try (
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()
                ) {
            channel.basicQos(1);

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            logger.info(" [*] Waiting for messages. To exit press CTRL+C");

            boolean firstFound = false;
            boolean queueEmpty = true;
            while (!firstFound || !queueEmpty) {
                GetResponse response = channel.basicGet(QUEUE_NAME, true);
                if (response == null) {
                    queueEmpty = true;
                    Thread.sleep(1000);
                    continue;
                }
                firstFound = true;
                queueEmpty = false;
                String message = new String(response.getBody(), charsetName);
                try {
                    parseMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseMessage(String message) {
        Gson gson = new Gson();
        ParseConfig config = gson.fromJson(message, ParseConfig.class);
        main.parseCompressedFile(config);
    }
}
