package chat.common.handler;

import java.util.HashMap;
import java.util.LinkedList;

import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Process packet first. Second, process priority queue method.
 * @author User
 * @see ChatCommonInboundPriorityHandler
 */
public class ChatCommonInboundPipelineHandler extends ChannelInboundHandlerAdapter {

	@SuppressWarnings("rawtypes")
	private HashMap<Class<? extends Packet<?>>, LinkedList<HandleQueueGeneric>> queue = new HashMap<>();

	public void addQueue(Class<? extends Packet<?>> clazz, HandleQueueGeneric<?> run) {
		if (!queue.containsKey(clazz)) {
			queue.put(clazz, new LinkedList<>());
		}
		queue.get(clazz).addLast(run);
	}
	public void removeAll(Class<? extends Packet<?>> clazz) {
		queue.remove(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (queue.containsKey(msg.getClass())) {
			if (queue.get(msg.getClass()).size() > 0) {
				if(Utils.log) System.out.println("[Priority]: Invoking reserved method...");
				queue.get(msg.getClass()).removeFirst().run(msg);
			} else
				queue.remove(msg.getClass());
		}
		super.channelRead(ctx, msg);
	}
}
