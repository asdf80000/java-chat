package chat.server.handler;

import chat.common.handler.ChannelState;
import chat.common.main.Utils;
import chat.server.main.ChannelManager;
import io.netty.util.AttributeKey;

public class AttributeSaver {
	public static AttributeKey<String> username = AttributeKey.newInstance("username");
	public static AttributeKey<ChannelState> state = AttributeKey.newInstance("state");
	public static AttributeKey<ChannelManager> manager = Utils.newAttributeKey(ChannelManager.class, "manager");
	public static AttributeKey<Boolean> loggedin = AttributeKey.newInstance("loggedin");
	public static AttributeKey<String> id = AttributeKey.newInstance("id");
}
