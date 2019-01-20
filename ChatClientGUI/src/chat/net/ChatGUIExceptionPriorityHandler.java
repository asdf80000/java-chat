package chat.net;

import java.util.HashMap;
import java.util.LinkedList;

import chat.common.handler.ChatCommonInboundPipelineHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Process priority queue method. Do not process packet with handler.
 * 
 * @author User
 * @see ChatCommonInboundPipelineHandler
 */
public class ChatGUIExceptionPriorityHandler extends ChannelInboundHandlerAdapter {

	@SuppressWarnings("rawtypes")
	public HashMap<Class<? extends Throwable>, LinkedList<HandleExceptionGeneric>> queue = new HashMap<>();

	public void addQueue(Class<? extends Throwable> clazz, HandleExceptionGeneric<?> run) {
		if (!queue.containsKey(clazz)) {
			queue.put(clazz, new LinkedList<>());
		}
		queue.get(clazz).addLast(run);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (queue.containsKey(cause.getClass())) {
			if (queue.get(cause.getClass()).size() > 0) {
				System.out.println("[Priority]: Invoking reserved method...");
				queue.get(cause.getClass()).removeFirst().run(cause);
			} else
				queue.remove(cause.getClass());
		} else
			super.exceptionCaught(ctx, cause);
	}
}
