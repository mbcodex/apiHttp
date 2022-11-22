package com.powin.modbusfiles.utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.slf4j.LoggerFactory;

import com.powin.powinwebappbase.HttpHelper;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;


public class SlackUtils {
	private final static Logger LOG = LogManager.getLogger();
	private static HttpClient cHttpClient;
	private static String cWebHookUrl = PowinProperty.WEBHOOK_URL.toString();

	private static MethodsClient slackClient = Slack.getInstance().methods();

	public enum SlackEmoji {
		Exclamation(":exclamation: "), StormTrooper(":storm_trooper: "), DancingMan(":man_dancing: "),
		Check(":white_check_mark:"), RedX(":x:"), Tada(":tada: ");

		public String id;

		SlackEmoji(String id) {
			this.id = id;
		}
	}

    static {
    	init();
    }
	
	public static String getWebHookUrl() {
		return cWebHookUrl;
	}
   
	public static void init() {
		LOG.debug("Initializing SlackUtils");
		cHttpClient = HttpHelper.buildHttpClient(false);
	}

	public static String buildNotifyString(String msgTitle, Map<String, String> appVersions, List<String> testResult) {
		final StringBuilder mBuilder = new StringBuilder();
		final StringBuilder wrapper = new StringBuilder();
		mBuilder
		.append(msgTitle.replace("*null*", "none"))
		.append("Software Versions: ")
		.append(appVersions.toString())
		.append("\n");

		for (String line : testResult) {
			SlackEmoji emoji = SlackEmoji.Check;
			if (line.contains("[ERROR]")) {
				emoji = SlackEmoji.RedX;
			}
			wrapper.append(emoji.id).append(line).append("\n");
		}
		return mBuilder.append(wrapper.toString()).toString();
	}

	public static String buildMsgString(String emoji, String msgTitle, String msg) {
		final StringBuilder mBuilder = new StringBuilder();
		mBuilder.append(emoji).append(msgTitle).append(msg).append(".");
		return mBuilder.toString();
	}

	public static boolean sendNotification(String message) {
		final BasicResponseHandler mResponseHandler = new BasicResponseHandler();
		boolean mNotificationSent = true;
		final HttpEntity mHttpEntity = new StringEntity("{\"text\":\"" + message + "\"}", Charset.defaultCharset());
		final HttpPost mHttpMethod = new HttpPost(getWebHookUrl());
		mHttpMethod.setEntity(mHttpEntity);
		mHttpMethod.addHeader("Content-type", "application/json");
		try {
			final String mResponseBody = cHttpClient.execute(mHttpMethod, mResponseHandler);
			if (!mResponseBody.equalsIgnoreCase("ok")) {
				mNotificationSent = false;
				LOG.error("Could not send message to Slack/Response body - {}. ", mResponseBody.getBytes());
			}
		} catch (final Exception e) {
			System.out.println("Could not send message to Slack." + e);
			LOG.error("Could not send message to Slack.", e);
			mNotificationSent = false;
		}
		return mNotificationSent;
	}

	public static void findConversation(String name) {
		try {
			// Call the conversations.list method using the built-in WebClient
			ConversationsListResponse result = slackClient.conversationsList(r -> r.token(PowinProperty.TOKEN_STRING.toString()));
					// The token you used to initialize your app
					
			for (Conversation channel : result.getChannels()) {
				LOG.info("Channel name: {}, id: {}", channel.getName(), channel.getId());
				if (channel.getName().toLowerCase().contains(name)) {
					String conversationId = channel.getId();
					// Print result
					LOG.info("Found conversation ID: {}", conversationId);
					// Break from for loop
					break;
				}
			}
		} catch (IOException | SlackApiException e) {
			LOG.error("error: {}", e.getMessage(), e);
		}
	}

	public static void fetchHistory(String id) {
		try {
			// Call the conversations.history method using the built-in WebClient
			ConversationsHistoryResponse result = slackClient.conversationsHistory(r -> r.token(PowinProperty.TOKEN_STRING.toString()).channel(id));

			Optional<List<Message>> conversationHistory = Optional.empty();
			conversationHistory = Optional.ofNullable(result.getMessages());
			// Print results
			if (conversationHistory.isPresent()) {
				int count = conversationHistory.get().size();
				LOG.info("{} messages found.", count);

				LOG.info("Latest message TS is {}. Content is {}", conversationHistory.get().get(0).getTs(),
						conversationHistory.get().get(0).getText());

			} else {
				LOG.error("No messages found.");
			}

		} catch (IOException | SlackApiException e) {
			LOG.error("error: {}", e.getMessage(), e);
		}
	}

	public static String getLatestTs(String id) {
		try {
			ConversationsHistoryResponse result = slackClient.conversationsHistory(r -> r.token(PowinProperty.TOKEN_STRING.toString()).channel(id));
			Optional<List<Message>> conversationHistory = Optional.empty();
			conversationHistory = Optional.ofNullable(result.getMessages());
			if (conversationHistory.isPresent()) {
				return conversationHistory.get().get(0).getTs();

			} else {
				LOG.error("No messages found.");
				return null;
			}

		} catch (IOException | SlackApiException e) {
			LOG.error("error: {}", e.getMessage(), e);
			return null;
		}
	}
    // TODO multiple returns
	public static String getNewMessage(String id, int timeOutSeconds) {
		String originTs = getLatestTs(id);
		if (originTs == null)
			return null;
		String timer = TimeOut.create(timeOutSeconds);
		String newTs = "";
		while (!TimeOut.isExpired(timer)) {
			newTs = getLatestTs(id);
			if (newTs.contains(originTs) == false) {
				String ts = newTs;
				try {
					ConversationsHistoryResponse result = slackClient.conversationsHistory(r -> r
							.token(PowinProperty.TOKEN_STRING.toString()).channel(id).latest(ts)
							.inclusive(true).limit(1));

					Message message = result.getMessages().get(0);

					LOG.info("result {}", message.getText());
					TimeOut.remove(timer);
					return message.getText();
				} catch (IOException | SlackApiException e) {
					LOG.error("error: {}", e.getMessage(), e);
					TimeOut.remove(timer);
					return null;
				}
			}

			CommonHelper.quietSleep(1000);
		}
		LOG.info("Did not get any new message before timeout.");
		return null;
	}
}
