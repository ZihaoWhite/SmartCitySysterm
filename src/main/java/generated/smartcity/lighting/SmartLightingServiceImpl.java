/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Implementation of the SmartLightingService gRPC service.
 * Handles lighting control via unary, server-streaming, client-streaming, and bidirectional streaming RPCs.
 */
/**
 *
 * @author zihaobai
 */

package generated.smartcity.lighting;

import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SmartLightingServiceImpl extends SmartLightingServiceGrpc.SmartLightingServiceImplBase {

    private static Logger logger = Logger.getLogger(SmartLightingServiceImpl.class.getName());

    static {
        try {
            FileHandler fh = new FileHandler("logs/lighting.log", true); // 追加日志
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
    }

    
    private boolean isApiKeyValid(String apiKey) {
        return apiKey != null && apiKey.equals("zihao");
    }

    // Unary: TurnOnLight
    @Override
    public void turnOnLight(LightRequest request, StreamObserver<LightResponse> responseObserver) {
        if (!isApiKeyValid(request.getApiKey())) {
            logger.warning("Unauthorized turn-on request: Invalid API key");
            responseObserver.onNext(LightResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Unauthorized access: Invalid API key.")
                .build());
            responseObserver.onCompleted();
            return;
        }

        logger.info("Received TurnOnLight request: Light ID =: " + request.getLightId() + ", Location = " + request.getLocation());

        LightResponse response = LightResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Light turned on successfully: " + request.getLightId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Server Streaming: StreamLightStatus
    @Override
    public void streamLightStatus(StatusRequest request, StreamObserver<LightStatus> responseObserver) {
        if (!isApiKeyValid(request.getApiKey())) {
            logger.warning("Unauthorized status request: Invalid API key.");
            responseObserver.onCompleted();
            return;
        }

        logger.info("Streaming light statuses for area: " + request.getAreaId());

        for (int i = 1; i <= 5; i++) {
            LightStatus status = LightStatus.newBuilder()
                    .setLightId("light-" + i)
                    .setIsOn(i % 2 == 0)
                    .setBrightness(i * 10)
                    .setStatusMessage("Normal")
                    .build();
            responseObserver.onNext(status);
        }

        responseObserver.onCompleted();
    }

    // Client Streaming: AdjustBrightness
    @Override
    public StreamObserver<BrightnessLevel> adjustBrightness(StreamObserver<AdjustmentSummary> responseObserver) {
        return new StreamObserver<BrightnessLevel>() {
            int totalBrightness = 0;
            int count = 0;

            @Override
            public void onNext(BrightnessLevel request) {
                if (!isApiKeyValid(request.getApiKey())) {
                    logger.warning("Brightness adjustment rejected: Invalid API key.");
                    return;
                }

                logger.info("Brightness adjustment received: Light ID = " + request.getLightId() + ", Brightness = " + request.getBrightness());
                totalBrightness += request.getBrightness();
                count++;
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Error during brightness adjustment: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                float avg = count > 0 ? (float) totalBrightness / count : 0;
                AdjustmentSummary summary = AdjustmentSummary.newBuilder()
                        .setAdjustedCount(count)
                        .setAverageBrightness(avg)
                        .build();

                responseObserver.onNext(summary);
                responseObserver.onCompleted();

                logger.info("Brightness adjustment completed. Count: " + count + " , Average " + avg);
            }
        };
    }

    // Bidirectional Streaming: LiveLightingControl
    @Override
    public StreamObserver<LightingCommand> liveLightingControl(StreamObserver<LightingFeedback> responseObserver) {
        return new StreamObserver<LightingCommand>() {
            @Override
            public void onNext(LightingCommand command) {
                if (!isApiKeyValid(command.getApiKey())) {
                    logger.warning("Rejected live command: Invalid API key.");

                    LightingFeedback feedback = LightingFeedback.newBuilder()
                            .setLightId(command.getLightId())
                            .setAccepted(false)
                            .setCurrentState("Unauthorized")
                            .build();

                    responseObserver.onNext(feedback);
                    return;
                }

                logger.info("Live command received: Light ID = " + command.getLightId() + ", Command " + command.getCommand());

                String resultState;
                boolean accepted = true;

                switch (command.getCommand().toUpperCase()) {
                    case "ON":
                        resultState = "Turned ON";
                        break;
                    case "OFF":
                        resultState = "Turned OFF";
                        break;
                    case "DIM":
                        resultState = "Dimmed";
                        break;
                    default:
                        resultState = "Invalid command";
                        accepted = false;
                        break;
                }

                LightingFeedback feedback = LightingFeedback.newBuilder()
                        .setLightId(command.getLightId())
                        .setAccepted(accepted)
                        .setCurrentState(resultState)
                        .build();

                responseObserver.onNext(feedback);
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Error in live lighting control: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                logger.info("Live lighting control session ended.");
            }
        };
    }
}