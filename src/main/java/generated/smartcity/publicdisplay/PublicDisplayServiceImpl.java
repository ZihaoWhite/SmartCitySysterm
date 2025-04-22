/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package generated.smartcity.publicdisplay;

/**
 *
 * @author zihaobai
 */


import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PublicDisplayServiceImpl extends PublicDisplayServiceGrpc.PublicDisplayServiceImplBase {

    private static final Logger logger = Logger.getLogger(PublicDisplayServiceImpl.class.getName());

    static {
        try {
            FileHandler fh = new FileHandler("logs/publicdisplay.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
    }

    // API Key validation
    private boolean isApiKeyValid(String apiKey) {
        return apiKey != null && apiKey.equals("zihao");
    }

    // Unary: Send a single message to a screen
    @Override
    public void sendMessage(DisplayMessage request, StreamObserver<DisplayAck> responseObserver) {
        if (!isApiKeyValid(request.getApiKey())) {
            logger.warning("Request denied: Invalid API key");
            DisplayAck ack = DisplayAck.newBuilder()
                    .setSuccess(false)
                    .setConfirmation("Unauthorized access: Invalid API Key")
                    .build();
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
            return;
        }

        logger.info(" Message request received -- Screen: " + request.getScreenId() +
                    ", Content: " + request.getContent() + ", Priority: " + request.getPriority());

        DisplayAck ack = DisplayAck.newBuilder()
                .setSuccess(true)
                .setConfirmation("Message successfully sent to screen: " + request.getScreenId())
                .build();

        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }

    // Client Streaming: Receive a stream of messages and return summary
    @Override
    public StreamObserver<DisplayMessage> streamMessages(StreamObserver<DisplaySummary> responseObserver) {
        return new StreamObserver<DisplayMessage>() {
            int total = 0;
            int success = 0;

            @Override
            public void onNext(DisplayMessage message) {
                total++;
                if (!isApiKeyValid(message.getApiKey())) {
                    logger.warning("Batch message denied: Invalid API Key - Screen" + message.getScreenId());
                    return;
                }

                success++;
                logger.info("Batch message received-- ID: " + message.getMessageId() +
                            ", Screen: " + message.getScreenId() +
                            ", Content: " + message.getContent());
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Error during batch message stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                DisplaySummary summary = DisplaySummary.newBuilder()
                        .setTotalMessages(total)
                        .setSuccessfulDisplays(success)
                        .setSummaryNote("Batch processing complete")
                        .build();

                responseObserver.onNext(summary);
                responseObserver.onCompleted();

                logger.info("Total Batch display summary: " + total + "，Success: " + success);
            }
        };
    }

    // Bidirectional Streaming: Real-time messages
    @Override
    public StreamObserver<DisplayMessage> realTimeDisplay(StreamObserver<DisplayStatus> responseObserver) {
        return new StreamObserver<DisplayMessage>() {
            @Override
            public void onNext(DisplayMessage message) {
                if (!isApiKeyValid(message.getApiKey())) {
                    logger.warning("Real-time display denied: Invalid API Key = " + message.getScreenId());
                    DisplayStatus status = DisplayStatus.newBuilder()
                            .setMessageId(message.getMessageId())
                            .setScreenId(message.getScreenId())
                            .setIsDisplayed(false)
                            .setStatusNote("Unauthorized: Invalid API Key")
                            .build();
                    responseObserver.onNext(status);
                    return;
                }

                logger.info("Real-time display = " + message.getContent() + " @ Screen " + message.getScreenId());

                DisplayStatus status = DisplayStatus.newBuilder()
                        .setMessageId(message.getMessageId())
                        .setScreenId(message.getScreenId())
                        .setIsDisplayed(true)
                        .setStatusNote("Displayed successfully")
                        .build();

                responseObserver.onNext(status);
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Real-time display error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                logger.info("Real-time display session completed");
            }
        };
    }
}