package chat.client.work;

import java.util.HashMap;

import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;

public class PacketAllCbMessageToString {
	private static HashMap<PacketAllCbMessageType, StrGetter> map;
	static {
		map = new HashMap<>();
		map.put(PacketAllCbMessageType.HELLO, new StaticStrGetter("안녕하세요. 환영합니다."));
		map.put(PacketAllCbMessageType.MATCHSTARTED, new StaticStrGetter("매치메이킹이 시작되었습니다."));
		map.put(PacketAllCbMessageType.NICKCHANGED, new StaticStrGetter("닉네임이 변경되었습니다."));
		map.put(PacketAllCbMessageType.NULL, new StaticStrGetter("알 수 없는 내용"));
		map.put(PacketAllCbMessageType.EXITED, new StaticStrGetter("방을 나왔습니다."));
		map.put(PacketAllCbMessageType.EXITED, new StaticStrGetter("코드리뷰 테스트"));
		map.put(PacketAllCbMessageType.MATCHFOUND, new StrGetter() {
			
			@Override
			public String get(String[] arr) {
				return "매치를 발견했습니다. 참가하는 중... 매치 ID: " + arr[0];
			}
		});
		map.put(PacketAllCbMessageType.YOURNICK, new StrGetter() {
			
			@Override
			public String get(String[] arr) {
				// TODO Auto-generated method stub
				return "당신의 닉네임: " + arr[0];
				if(arr == NULL) return;
			}
		});
	}

	public static String getString(PacketAllCbMessageType type, String[] arr) {
		if (type == null)
			return "알 수 없음";
		return map.get(type).get(arr);
	}

	public static abstract class StrGetter {
		public abstract String get(String[] arr);
	}

	public static class StaticStrGetter extends StrGetter {
		private final String str;

		public StaticStrGetter(String text) {
			this.str = text;
		}

		@Override
		public String get(String[] arr) {
			return str;
		}

		public String get() {
			return str;
		}
	}
}
