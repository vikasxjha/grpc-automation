package com.mapbox.services.campaign;

import com.mapbox.client.BaseGrpcClient;
import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.core.channel.GrpcChannelFactory;
import com.mapbox.core.streaming.StreamingResponseCollector;
import com.mapbox.grpc.campaign.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Campaign Service Client
 * Provides methods to interact with Campaign gRPC service
 */
@Slf4j
public class CampaignClient extends BaseGrpcClient<CampaignServiceGrpc.CampaignServiceBlockingStub> {

    private CampaignServiceGrpc.CampaignServiceStub asyncStubInternal;

    public CampaignClient(GrpcChannelFactory channelFactory) {
        super(channelFactory);
    }

    @Override
    protected void initializeStubs() {
        this.blockingStub = CampaignServiceGrpc.newBlockingStub(channel);
        this.asyncStubInternal = CampaignServiceGrpc.newStub(channel);
    }

    /**
     * Get campaign details
     */
    public GrpcResponse<GetCampaignResponse> getCampaign(GetCampaignRequest request) {
        log.info("Getting campaign for caller: {}, receiver: {}",
                request.getCaller(), request.getReceiver());

        return executeUnaryCall(request, blockingStub::getCampaign);
    }

    /**
     * Get campaign with custom deadline
     */
    public GrpcResponse<GetCampaignResponse> getCampaignWithDeadline(
            GetCampaignRequest request,
            long timeout,
            TimeUnit unit) {

        log.info("Getting campaign with deadline: {} {}", timeout, unit);

        return executeUnaryCall(request, req ->
                blockingStub.withDeadlineAfter(timeout, unit).getCampaign(req));
    }

    /**
     * List campaigns with server streaming
     */
    public StreamingResponseCollector<CampaignMessage> listCampaigns(
            ListCampaignsRequest request) {

        log.info("Listing campaigns for user: {}", request.getUserId());

        StreamingResponseCollector<CampaignMessage> collector =
                new StreamingResponseCollector<>();

        asyncStubInternal.listCampaigns(request, collector);

        return collector;
    }

    /**
     * List campaigns with expected count
     */
    public StreamingResponseCollector<CampaignMessage> listCampaigns(
            ListCampaignsRequest request,
            int expectedCount) {

        log.info("Listing campaigns for user: {}, expecting {} messages",
                request.getUserId(), expectedCount);

        StreamingResponseCollector<CampaignMessage> collector =
                new StreamingResponseCollector<>(expectedCount);

        asyncStubInternal.listCampaigns(request, collector);

        return collector;
    }

    /**
     * Upload campaign data with client streaming
     */
    public StreamObserver<CampaignDataChunk> uploadCampaignData(
            StreamObserver<UploadResponse> responseObserver) {

        log.info("Starting upload campaign data stream");
        return asyncStubInternal.uploadCampaignData(responseObserver);
    }

    /**
     * Sync campaigns with bidirectional streaming
     */
    public StreamObserver<CampaignSyncRequest> syncCampaigns(
            StreamObserver<CampaignSyncResponse> responseObserver) {

        log.info("Starting bidirectional campaign sync");
        return asyncStubInternal.syncCampaigns(responseObserver);
    }
}

