/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package generated.smartcity.airquality;

/**
 *
 * @author zihaobai
 * AirQualityServer
 * This class initializes and starts the gRPC server for the Air Quality Service.
 * It binds the service implementation to a specific port and waits for client connections.
 * 
 */

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class AirQualityServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 50052; // port used by this service 
        Server server = ServerBuilder.forPort(port)
                .addService(new AirQualityServiceImpl())
                .build();

        // start the server 
        server.start();
        System.out.println("AirQuality Server started successfully on port: " + port);
        System.out.println("Awaiting cllient connections...");

        // keep the server running 
        server.awaitTermination();
    }
}