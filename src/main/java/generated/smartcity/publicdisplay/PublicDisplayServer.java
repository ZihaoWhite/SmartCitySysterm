/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package generated.smartcity.publicdisplay;

/**
 *
 * @author zihaobai
 */

//Main class to start the gRPC server for the Public Display Service.

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class PublicDisplayServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 50053; // Port dedicated to the Public Display service

       
        // Build and start the gRPC server
        Server server = ServerBuilder.forPort(port)
                .addService(new PublicDisplayServiceImpl())
                .build();

        // Start the server 
        server.start();
        System.out.println("PublicDisplay gRPC Server started successfully on port:: " + port);
        System.out.println("Waiting for client connections...");

        // Keep the server running
        server.awaitTermination();
    }
}