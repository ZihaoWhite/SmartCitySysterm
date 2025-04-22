/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package generated.smartcity.airquality;

/**
 *
 * @author zihaobai
 * AirQualityServiceImpl
 * This class implements the AirQuality gRPC service with all four types of RPCs:
 * - Unary
 * - Server Streaming
 * - Client Streaming
 * - Bidirectional Streaming
 * It includes basic authentication with API key, and logs all events.
 */



import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AirQualityServiceImpl extends AirQualityServiceGrpc.AirQualityServiceImplBase {

    private static final Logger logger = Logger.getLogger("AirQualityLogger");
    private static final String VALID_API_KEY = "zihao"; 

    static {
        try {
            FileHandler fh = new FileHandler("logs/airquality.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
    }

    // Unary RPC get current air quality
    @Override
    public void getCurrentAirStatus(LocationRequest request, StreamObserver<AirStatus> responseObserver) {
        if (!VALID_API_KEY.equals(request.getApiKey())) {
            String msg = "Invalid API Key from city: " + request.getCity();
            logger.warning(msg);
            responseObserver.onError(io.grpc.Status.PERMISSION_DENIED.withDescription("Invalid API Key").asRuntimeException());
            return;
        }

        logger.info("Air quality request received from city: " + request.getCity() + ", District: " + request.getDistrict());

        AirStatus status = AirStatus.newBuilder()
                .setTimestamp(LocalDateTime.now().toString())
                .setTemperature(25.0f)
                .setHumidity(55.0f)
                .setPm25(30.0f)
                .setCo2(450.0f)
                .setStatus("Good")
                .build();

        logger.info("Returning air status: " + status);
        responseObserver.onNext(status);
        responseObserver.onCompleted();
    }

    // Server Streaming RPC: stream multiple air status updates 
    @Override
    public void streamAirQuality(LocationRequest request, StreamObserver<AirStatus> responseObserver) {
        if (!VALID_API_KEY.equals(request.getApiKey())) {
            logger.warning("Invalid API Key stream request from: " + request.getCity());
            responseObserver.onError(io.grpc.Status.PERMISSION_DENIED.withDescription("Invalid API Key").asRuntimeException());
            return;
        }

        logger.info("Starting air quality streaming from City: " + request.getCity() + ", District: " + request.getDistrict());

        for (int i = 0; i < 5; i++) {
            AirStatus status = AirStatus.newBuilder()
                    .setTimestamp(LocalDateTime.now().toString())
                    .setTemperature(22 + i)
                    .setHumidity(50 + i)
                    .setPm25(25 + i)
                    .setCo2(400 + i * 5)
                    .setStatus("Moderate")
                    .build();
            responseObserver.onNext(status);
            logger.info("Pushed update: " + status);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe("Streaming interrupted: " + e.getMessage());
            }
        }

        responseObserver.onCompleted();
        logger.info("Air quality streaming completed.");
    }

    // Client Streaming RPC: Upload sensor data and get summary
    @Override
    public StreamObserver<SensorReading> uploadSensorData(StreamObserver<SensorReport> responseObserver) {
        return new StreamObserver<SensorReading>() {
            int count = 0;
            float totalPm25 = 0;
            float totalCo2 = 0;

            @Override
            public void onNext(SensorReading reading) {
                if (!VALID_API_KEY.equals(reading.getApiKey())) {
                    logger.warning("Invalid API Key, discarding data: " + reading);
                    return;
                }
                logger.info("Received SensorReading: " + reading);
                count++;
                totalPm25 += reading.getPm25();
                totalCo2 += reading.getCo2();
            }

            @Override
            public void onCompleted() {
                SensorReport report = SensorReport.newBuilder()
                        .setTotalReadings(count)
                        .setAvgPm25(count == 0 ? 0 : totalPm25 / count)
                        .setAvgCo2(count == 0 ? 0 : totalCo2 / count)
                        .setQualitySummary("Summary generated")
                        .build();

                logger.info("Upload complete. Summary: " + report);
                responseObserver.onNext(report);
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Error during upload: " + t.getMessage());
            }
        };
    }

    // Bidirectional Streaming 
    @Override
    public StreamObserver<SensorReading> liveMonitoring(StreamObserver<AirStatus> responseObserver) {
        return new StreamObserver<SensorReading>() {
            @Override
            public void onNext(SensorReading reading) {
                if (!VALID_API_KEY.equals(reading.getApiKey())) {
                    logger.warning("Invalid API Key during live monitoring: " + reading);
                    return;
                }

                logger.info("Live monitoring: received reading: " + reading);

                AirStatus status = AirStatus.newBuilder()
                        .setTimestamp(LocalDateTime.now().toString())
                        .setTemperature(reading.getTemperature())
                        .setHumidity(reading.getHumidity())
                        .setPm25(reading.getPm25())
                        .setCo2(reading.getCo2())
                        .setStatus("Live Monitored")
                        .build();

                responseObserver.onNext(status);
                logger.info("Returning live AirStatus: " + status);
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Live monitoring error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                logger.info("Live monitoring session ended.");
            }
        };
    }
}