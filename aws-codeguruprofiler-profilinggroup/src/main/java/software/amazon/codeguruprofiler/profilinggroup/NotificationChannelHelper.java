package software.amazon.codeguruprofiler.profilinggroup;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.services.codeguruprofiler.CodeGuruProfilerClient;
import software.amazon.awssdk.services.codeguruprofiler.model.AddNotificationChannelsRequest;
import software.amazon.awssdk.services.codeguruprofiler.model.Channel;
import software.amazon.awssdk.services.codeguruprofiler.model.EventPublisher;
import software.amazon.awssdk.services.codeguruprofiler.model.GetNotificationConfigurationRequest;
import software.amazon.awssdk.services.codeguruprofiler.model.GetNotificationConfigurationResponse;
import software.amazon.awssdk.services.codeguruprofiler.model.RemoveNotificationChannelRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

public class NotificationChannelHelper {
    private NotificationChannelHelper() {
        // prevent instantiation
    }

    public static void addChannelNotifications(String pgName, List<software.amazon.codeguruprofiler.profilinggroup.Channel> channels,
                                               AmazonWebServicesClientProxy proxy, CodeGuruProfilerClient profilerClient) {
        AddNotificationChannelsRequest.Builder addNotificationChannelsRequest = AddNotificationChannelsRequest.builder()
                .profilingGroupName(pgName);
        channels.forEach(channel -> addNotificationChannelsRequest.channels(Channel.builder()
                .uri(channel.getChannelUri())
                .eventPublishers(ImmutableSet.of(EventPublisher.ANOMALY_DETECTION))
                .id(channel.getId())
                .build()
        ));

        proxy.injectCredentialsAndInvokeV2(addNotificationChannelsRequest.build(), profilerClient::addNotificationChannels);
    }

    public static void addChannelNotification(String pgName, Channel channel, AmazonWebServicesClientProxy proxy, CodeGuruProfilerClient profilerClient) {
        AddNotificationChannelsRequest.Builder addNotificationChannelsRequest = AddNotificationChannelsRequest.builder()
                .profilingGroupName(pgName)
                .channels(Collections.singletonList(Channel.builder()
                        .id(channel.id())
                        .eventPublishers(ImmutableSet.of(EventPublisher.ANOMALY_DETECTION))
                        .uri(channel.uri())
                        .build()));

        proxy.injectCredentialsAndInvokeV2(addNotificationChannelsRequest.build(), profilerClient::addNotificationChannels);

    }

    public static void deleteNotificationChannel(final String pgName, final String channelId, final AmazonWebServicesClientProxy proxy, CodeGuruProfilerClient profilerClient) {
        RemoveNotificationChannelRequest removeNotificationChannelRequest = RemoveNotificationChannelRequest.builder()
                .channelId(channelId)
                .profilingGroupName(pgName)
                .build();
        proxy.injectCredentialsAndInvokeV2(removeNotificationChannelRequest, profilerClient::removeNotificationChannel);
    }

    public static void deleteNotificationChannelsForProfilingGroup(final String pgName, final AmazonWebServicesClientProxy proxy, CodeGuruProfilerClient profilerClient) {
        GetNotificationConfigurationRequest getNotificationConfigurationRequest = GetNotificationConfigurationRequest.builder()
                .profilingGroupName(pgName)
                .build();

        GetNotificationConfigurationResponse getNotificationConfigurationResponse = proxy.injectCredentialsAndInvokeV2(getNotificationConfigurationRequest,
                profilerClient::getNotificationConfiguration);

        // Iterate through all channels and remove them
        getNotificationConfigurationResponse.notificationConfiguration().channels().forEach(channel -> {
            RemoveNotificationChannelRequest removeNotificationChannelRequest = RemoveNotificationChannelRequest.builder()
                    .profilingGroupName(pgName)
                    .channelId(channel.id())
                    .build();
            proxy.injectCredentialsAndInvokeV2(removeNotificationChannelRequest, profilerClient::removeNotificationChannel);
        });
    }
}
