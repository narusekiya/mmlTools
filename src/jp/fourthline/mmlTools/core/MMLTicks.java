/*
 * Copyright (C) 2013-2025 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.util.List;
import java.util.Optional;


/**
 * 音符の時間変換値
 * @author たんらる
 */
public class MMLTicks {

	private static final MMLTickTable tickTable = MMLTickTable.getInstance();

	public static int getTick(String gt) throws MMLException {
		String str = gt;
		while (!tickTable.getTable().containsKey(str)) {
			int len = str.length();
			if (len == 0) {
				break;
			}
			char ch = str.charAt(len-1);
			if (!Character.isDigit(ch)) {
				str = str.substring(0, len - 1);
			} else {
				throw MMLException.createUndefinedTickException(gt);
			}
		}

		return tickTable.getTable().get(str);
	}

	public static Optional<List<List<String>>> getAlt(int tick) {
		var t = tickTable.getInvTable().get(tick);
		return t == null ? Optional.empty() : Optional.of(t.alt);
	}

	private static Integer minimum = null;
	public static int minimumTick() {
		if (minimum != null) {
			return minimum;
		}

		Integer v = null;
		for (Integer i : tickTable.getTable().values()) {
			if ( (v == null) || (i < v) ) {
				v = i;
			}
		}
		minimum = v;
		return minimum;
	}


	private final String noteName;
	int tick;
	boolean needTie;

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName ノート名
	 * @param tick tick長
	 */
	public MMLTicks(String noteName, int tick) {
		this(noteName, tick, true);
	}

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName ノート名
	 * @param tick tick長
	 * @param needTie noteNameを連結するときに tie が必要かどうかを指定します. 休符 or 音量ゼロのときは, falseを指定してください.
	 */
	public MMLTicks(String noteName, int tick, boolean needTie) {
		this.noteName = noteName;
		this.tick = tick;
		this.needTie = needTie;
	}

	protected String mmlNotePart(String phoneticString) {
		return mmlNotePart(List.of(phoneticString));
	}

	private String mmlNotePart(List<String> phoneticString) {
		StringBuilder sb = new StringBuilder();

		phoneticString.forEach(t -> {
			if (needTie) {
				sb.append('&');
			}
			sb.append(noteName).append(t);
		});

		return sb.toString();
	}

	protected String makeMMLText(StringBuilder sb, int remTick) throws MMLException {
		// 1~64の分割
		if (remTick > 0) {
			for (int base = 1; base <= 64; base *= 2) {
				int baseTick = getTick(""+base);
				if (tickTable.getInvTable().containsKey(remTick)) {
					sb.append( mmlNotePart(tickTable.getInvTable().get(remTick).primary) );
					remTick = 0;
					break;
				}
				while (remTick >= baseTick) {
					sb.append( mmlNotePart(""+base) );
					remTick -= baseTick;
				}
			}
			if (remTick > 0) {
				throw MMLException.createUndefinedTickException(remTick, tick);
			}
		}

		if (needTie) {
			if (sb.length() > 0) {
				return sb.substring(1);
			}
			return "";
		} else {
			return sb.toString();
		}
	}

	/**
	 * noteNameとtickをMMLの文字列に変換します.
	 * needTieがtrueのときは、'&amp;' による連結を行います.
	 * @return MML文字列
	 * @throws MMLException 変換に失敗した
	 */
	public String toMMLText() throws MMLException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();

		// "1."
		int mTick = getTick("1.");
		int tick1 = getTick("1");
		while (remTick > (tick1*2)) {
			sb.append( mmlNotePart("1.") );
			remTick -= mTick;
		}

		return makeMMLText(sb, remTick);
	}

	/**
	 * Base長を使って変換します.　（調律用）
	 * @param base 使用する調律指定
	 * @return MML文字列
	 * @throws MMLException 変換に失敗した
	 */
	public String toMMLTextByBase(TuningBase base) throws MMLException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();
		int min = minimumTick();

		int baseTick = base.getTick();
		while (remTick >= baseTick + min) {
			sb.append( mmlNotePart(base.getBase()) );
			remTick -= baseTick;
		}

		return makeMMLText(sb, remTick);
	}
}
