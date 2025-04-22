/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package generated.smartcity.lighting;

/**
 *
 * @author zihaobai
 * SmartLightingServer
 * This class starts the gRPC server for the Smart Lighting Service.
 * The service listens on a specific port and handles incoming gRPC calls defined in SmartLightingServiceImp
 */

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class SmartLightingServer {

    public static void main(String[] args) {
        int port = 50051;

        try {
            // Build the gRPC server and attach the service implementation
            Server server = ServerBuilder.forPort(port)
                    .addService(new SmartLightingServiceImpl())
                    .build();

            // Start the server
            server.start();
            System.out.println("SmartLighting gRPC Server started. Listening on port:" + port);
            System.out.println("Waiting for client connections...");

            // Keep the server running
            server.awaitTermination();

        } catch (Exception e) {
            System.err.println("Failed to start SmartLighting gRPC Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}