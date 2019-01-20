package chat.work;

import java.util.HashMap;

import chat.common.packet.play.PacketPlayCbSafeMessage.PacketPlayCbSafeMessageType;

public class PacketPlayCbSafeMessageToString {
	private static HashMap<PacketPlayCbSafeMessageType, StrGetter> map;
	static {
		map = new HashMap<>();
		map.put(PacketPlayCbSafeMessageType.NULL, new StaticStrGetter("알 수 없는 내용"));
		map.put(PacketPlayCbSafeMessageType.EXITED, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return arr[0] + "(이)가 매치를 나갔습니다.";
			}
		});
		map.put(PacketPlayCbSafeMessageType.JOINED, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return arr[0] + "(이)가 매치에 참가했습니다.";
			}
		});
		map.put(PacketPlayCbSafeMessageType.STILL_0, new StaticStrGetter("서버는 자동으로 인원을 정해진 인원으로 충당합니다."));
		map.put(PacketPlayCbSafeMessageType.STILL_1,
				new StaticStrGetter("방에 인원이 없다면 서버에서 \'추가 인원이 있을 때\' 인원을 충당합니다."));
		map.put(PacketPlayCbSafeMessageType.STILL_2, new StaticStrGetter("인원이 없다는 이유로 다른 매치를 찾는 것은 효과가 없습니다."));
		map.put(PacketPlayCbSafeMessageType.MATCHINFO_0, new StaticStrGetter("-------- 매치 정보 --------"));
		map.put(PacketPlayCbSafeMessageType.MATCHINFO_1, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return "현재 매치 ID: " + arr[0];
			}
		});
		map.put(PacketPlayCbSafeMessageType.MATCHINFO_2, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return "현재 매치 인원 수: " + arr[0];
			}
		});
		map.put(PacketPlayCbSafeMessageType.MATCHINFO_3, new StrGetter() {

			@Override
			public String get(String[] arr) {
				long millis = Long.parseLong(arr[0]);
				int mins = 0;
				int secs = (int) millis / 1000;
				String btxt = "";
				String txt = secs + "초";
				if (secs >= 60) {
					mins = secs / 60;
					secs = secs % 60;
					btxt = " " + secs + "초";
					txt = mins + "분" + btxt;

					int hours = 0;
					if (mins >= 60) {
						hours = mins / 60;
						mins = mins % 60;
						btxt = " " + txt;
						txt = hours + "시간" + btxt;

						int days = 0;
						if (hours >= 24) {
							days = hours / 24;
							hours = hours % 24;
							btxt = " " + txt;
							txt = days + "일 " + btxt;

							int years = 0;
							if (days > 365) {
								years = days / 365;
								days = days % 365;
								btxt = " " + txt;
								txt = years + "년 " + btxt;
							}
						}
					}
				}
				return "매치 진행 시간: " + txt;
			}
		});
		map.put(PacketPlayCbSafeMessageType.MSG_TOO_LONG, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return "메시지의 최대 길이를 초과합니다. (최대 " + arr[0] + "바이트)";
			}
		});
		map.put(PacketPlayCbSafeMessageType.MSG_TOO_SHORT, new StrGetter() {

			@Override
			public String get(String[] arr) {
				return "메시지가 너무 짧습니다. (최소 " + arr[0] + "바이트)";
			}
		});
	}

	public static String getString(PacketPlayCbSafeMessageType type, String[] arr) {
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
