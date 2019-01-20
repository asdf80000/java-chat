package chat.common.handler;

import java.util.HashMap;
import java.util.LinkedList;

import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Process priority queue method. Do not process packet with handler.
 * 
 * @author User
 * @see ChatCommonInboundPipelineHandler
 */
public class ChatCommonInboundPriorityHandler extends ChannelInboundHandlerAdapter {

	@SuppressWarnings("rawtypes")
	private HashMap<Class<? extends Packet<?>>, LinkedList<HandleQueueGeneric>> queue = new HashMap<>();

	public void addQueue(Class<? extends Packet<?>> clazz, HandleQueueGeneric<?> run) {
		if (!queue.containsKey(clazz)) {
			queue.put(clazz, new LinkedList<>());
		}
		queue.get(clazz).addLast(run);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (queue.containsKey(msg.getClass())) {
			if (queue.get(msg.getClass()).size() > 0) {
				if (Utils.log)
					System.out.println("[Priority]: Invoking reserved method...");
				queue.get(msg.getClass()).removeFirst().run(msg);
			} else
				queue.remove(msg.getClass());
		} else {
			super.channelRead(ctx, msg);
		}
	}
}
