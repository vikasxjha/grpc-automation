package com.mapbox.mock;

import com.mapbox.grpc.campaign.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Mock gRPC server for testing
 * Implements Campaign service with test data
 */
@Slf4j
public class MockCampaignServer {

    private final int port;
    private final Server server;

    public MockCampaignServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new CampaignServiceImpl())
                .build();
    }

    /**
     * Start the mock server
     */
    public void start() throws IOException {
        server.start();
        log.info("Mock gRPC server started on port {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down mock gRPC server...");
            try {
                MockCampaignServer.this.stop();
            } catch (InterruptedException e) {
                log.error("Error shutting down server", e);
            }
        }));
    }

    /**
     * Stop the mock server
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("Mock gRPC server stopped");
        }
    }

    /**
     * Block until server shuts down
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Campaign Service Implementation
     */
    private static class CampaignServiceImpl extends CampaignServiceGrpc.CampaignServiceImplBase {

        @Override
        public void getCampaign(GetCampaignRequest request,
                                StreamObserver<GetCampaignResponse> responseObserver) {

            log.info("Received GetCampaign request: caller={}, receiver={}, campaignId={}",
                    request.getCaller(), request.getReceiver(), request.getCampaignId());

            // Validate request
            if (request.getCaller().isEmpty() || !request.getCaller().matches("\\d{12}")) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Invalid phone number format")
                        .asRuntimeException());
                return;
            }

            if (request.getCampaignId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Campaign ID is required")
                        .asRuntimeException());
                return;
            }

            if (request.getCampaignId().equals("campaign-999")) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Campaign not found")
                        .asRuntimeException());
                return;
            }

            // Build successful response
            GetCampaignResponse response = GetCampaignResponse.newBuilder()
                    .setCampaignId(request.getCampaignId())
                    .setTitle("Test Campaign")
                    .setSubtitle("Test Subtitle")
                    .setDescription("This is a test campaign from mock server")
                    .setStatus(CampaignStatus.ACTIVE)
                    .addTags("test")
                    .addTags("automation")
                    .addTags("grpc")
                    .putProperties("priority", "high")
                    .putProperties("channel", "sms")
                    .putProperties("region", "us-west")
                    .setCreatedAt(System.currentTimeMillis())
                    .setUpdatedAt(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void listCampaigns(ListCampaignsRequest request,
                                  StreamObserver<CampaignMessage> responseObserver) {

            log.info("Received ListCampaigns request: userId={}, pageSize={}",
                    request.getUserId(), request.getPageSize());

            // Send multiple campaign messages
            int count = Math.min(request.getPageSize(), 10);

            for (int i = 1; i <= count; i++) {
                CampaignMessage message = CampaignMessage.newBuilder()
                        .setCampaignId("campaign-" + i)
                        .setTitle("Campaign " + i)
                        .setStatus(CampaignStatus.ACTIVE)
                        .build();

                responseObserver.onNext(message);

                // Simulate some delay
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<CampaignDataChunk> uploadCampaignData(
                StreamObserver<UploadResponse> responseObserver) {

            return new StreamObserver<CampaignDataChunk>() {
                private int chunksReceived = 0;

                @Override
                public void onNext(CampaignDataChunk chunk) {
                    chunksReceived++;
                    log.info("Received chunk #{}", chunksReceived);
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Upload error", t);
                }

                @Override
                public void onCompleted() {
                    UploadResponse response = UploadResponse.newBuilder()
                            .setSuccess(true)
                            .setMessage("Upload completed successfully")
                            .setChunksReceived(chunksReceived)
                            .build();

                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<CampaignSyncRequest> syncCampaigns(
                StreamObserver<CampaignSyncResponse> responseObserver) {

            return new StreamObserver<CampaignSyncRequest>() {
                @Override
                public void onNext(CampaignSyncRequest request) {
                    log.info("Sync request for campaign: {}", request.getCampaignId());

                    CampaignSyncResponse response = CampaignSyncResponse.newBuilder()
                            .setCampaignId(request.getCampaignId())
                            .setSynced(true)
                            .setMessage("Synced successfully")
                            .build();

                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Sync error", t);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }

    /**
     * Main method to run mock server standalone
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9090;

        MockCampaignServer server = new MockCampaignServer(port);
        server.start();

        log.info("Mock server is ready to accept connections on port {}", port);
        server.blockUntilShutdown();
    }
}

