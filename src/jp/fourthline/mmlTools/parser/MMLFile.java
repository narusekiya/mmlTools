/*
 * Copyright (C) 2014-2025 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Base64.Decoder;
import java.util.zip.CRC32;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLEvent;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.MMLVerifyException;
import jp.fourthline.mmlTools.Marker;
import jp.fourthline.mmlTools.core.MMLTickTable;


/**
 * "*.mml" (3mleさん) のファイルを扱います.
 */
public final class MMLFile extends AbstractMMLParser {
	private final MMLScore score = new MMLScore();
	private String encoding = "Shift_JIS";
	private short resolution = 96;

	private final TextParser parser = 
			new TextParser()
			.pattern("Title=",    t -> score.setTitle(t))
			.pattern("Source=",   t -> score.setAuthor(t))
			.pattern("Encoding=", t -> this.encoding = t);

	// channel sections
	private final LinkedList<String> mmlParts = new LinkedList<>();
	private List<Extension3mleTrack> trackList = null;

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, encoding);
		if (contentsList.isEmpty()) {
			throw(new MMLParseException("no contents"));
		}
		parseSection(contentsList);
		if ( (trackList == null) || (trackList.size() == 0) ) {
			throw new MMLParseException("no track");
		}
		createTrack();
		try {
			setStartPosition();
		} catch (MMLExceptionList | MMLVerifyException e) {
			throw new MMLParseException(e.getLocalizedMessage());
		}
		return score;
	}

	private static final Pattern CRLF_PATTERN = Pattern.compile("\r\n");
	private static final Pattern CR_PATTERN = Pattern.compile("\r");
	private static final Pattern END_LINE_PATTERN = Pattern.compile("//.*\n");
	private static final Pattern COMMENT_PATTERN = Pattern.compile("/\\*/?([^/]|[^*]/)*\\*/");
	private static final Pattern SPACE_PATTERN = Pattern.compile("[ \t\n]");
	public static String toMMLText(String text) {
		String s = text;
		s = CRLF_PATTERN.matcher(s).replaceAll("\n");
		s = CR_PATTERN.matcher(s).replaceAll("\n");
		s = END_LINE_PATTERN.matcher(s + '\n').replaceAll("\n");
		s = COMMENT_PATTERN.matcher(s).replaceAll("");
		s = SPACE_PATTERN.matcher(s).replaceAll("");
		return s;
	}

	private void parseSection(List<SectionContents> contentsList) throws MMLParseException {
		for (SectionContents contents : contentsList) {
			if (contents.getName().equals("[3MLE EXTENSION]")) {
				trackList = parse3mleExtension(contents.getContents());
			} else if (contents.getName().matches("\\[Channel[0-9]*\\]")) {
				String s = contents.getContents();
				mmlParts.add(toMMLText(s));
			} else if (contents.getName().equals("[Settings]")) {
				parseSettings(contents.getContents());
			}
		}

	}

	private void createTrack() {
		for (Extension3mleTrack track : trackList) {
			int program = track.getInstrument() - 1; // 3MLEのInstruments番号は1がスタート.
			if (MabiDLS.getInstance().getInstByProgram(program) == null) {
				program = 0;
			}
			String[] text = new String[] { "", "", "" };
			for (int i = 0; i < track.getTrackCount(); i++) {
				text[i] = mmlParts.pop();
			}
			InstType instType = MabiDLS.getInstance().getInstByProgram(program).getType();
			MMLTrack mmlTrack;
			if ( (instType == InstType.VOICE) || (instType == InstType.CHORUS) ) {
				// 歌パート
				mmlTrack = new MMLTrack().setMML("", "", "", text[0]);
			} else {
				mmlTrack = new MMLTrack().setMML(text[0], text[1], text[2], "");
			}
			score.addTrack(mmlTrack);
			mmlTrack.setProgram(program);
			mmlTrack.setPanpot(track.getPanpot());
			mmlTrack.setTrackName(track.getTrackName());
		}
	}

	/**
	 * 再生開始位置を設定します.
	 * @throws MMLVerifyException 
	 * @throws MMLExceptionList 
	 */
	private void setStartPosition() throws MMLExceptionList, MMLVerifyException {
		if (score.getMarkerList().size() == 0) {
			return;
		}
		for (int i = 0; i < trackList.size(); i++) {
			Extension3mleTrack track = trackList.get(i);
			MMLTrack mmlTrack = score.getTrack(i);
			int markerId = track.getStartMarker();
			if (markerId > 0) {
				Marker marker = score.getMarkerList().get(markerId-1);
				int tickOffset = marker.getTickOffset();
				for (MMLEventList eventList : mmlTrack.getMMLEventList()) {
					MMLEvent.insertTick(eventList.getMMLNoteEventList(), 0, tickOffset);
				}
				mmlTrack.setStartDelta(tickOffset);
				mmlTrack.setStartSongDelta(tickOffset);
			}
			mmlTrack.generate();
		}
	}

	/**
	 * parse [Settings] contents
	 * @param contents
	 */
	private void parseSettings(String contents) {
		parser.parse(contents);
	}

	/**
	 * [3MLE EXTENSION] をパースし, トラック構成情報を取得します.
	 * @param str [3MLE EXTENSION] セクションのコンテンツ
	 * @return トラック構成情報
	 */
	private List<Extension3mleTrack> parse3mleExtension(String str) throws MMLParseException {
		StringBuilder sb = new StringBuilder();
		long c = 0;
		for (String s : str.split("\n")) {
			if (s.startsWith("d=")) {
				sb.append(s.substring(2));
			} else if (s.startsWith("c=")) {
				c = Long.parseLong(s.substring(2));
			}
		}

		byte[] data = decode(sb.toString(), c);
		return parseData(data);
	}

	private static byte[] decode(String dSection, long c) throws MMLParseException {
		CRC32 crc = new CRC32();
		crc.update(dSection.getBytes());
		if (c != crc.getValue()) {
			throw new MMLParseException("invalid c="+c+" <> "+crc.getValue());
		}
		Decoder decoder = Base64.getDecoder();
		byte[] b = decoder.decode(dSection);

		int dataLength = ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		byte[] data = new byte[dataLength];

		try (BZip2CompressorInputStream bz2istream = new BZip2CompressorInputStream(new ByteArrayInputStream(b, 12, b.length-12))) {
			bz2istream.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < dataLength; i++) {
			System.out.printf("%02x ", data[i]);
		}
		System.out.println();
		return data;
	}

	/**
	 * @param data decompress済みのバイト列
	 * @return トラック構成情報
	 */
	private List<Extension3mleTrack> parseData(byte[] data) {
		LinkedList<Extension3mleTrack> trackList = new LinkedList<>();
		trackList.add(new Extension3mleTrack(-1, -1, -1, null, 0)); // dummy

		ByteArrayInputStream istream = new ByteArrayInputStream(data);
		int b = 0;
		int hb = 0;
		while ( (b = istream.read()) != -1) {
			if ( (hb == 0x12) && (b == 0x10) ) {
				parseHeader(istream);
			} else if ( (hb == 0x02) && (b == 0x1c) ) {
				parseTrack(trackList, istream);
			} else if ( (hb == 0x09) && ( (b > 0x00) && (b < 0x20) )) {
				parseMarker(istream);
			}

			hb = b;
		}

		trackList.removeFirst();
		return trackList;
	}

	private void parseHeader(ByteArrayInputStream istream) {
		istream.skip(24);
		resolution = readLEShortValue(istream);
		System.out.println("resolution: " + resolution);
		istream.skip(11);
		int len = readLEIntValue(istream);
		istream.skip(len);
	}

	private void parseTrack(LinkedList<Extension3mleTrack> trackList, ByteArrayInputStream istream) {
		// parse Track
		istream.skip(3);
		int trackNo = istream.read();
		istream.skip(1); // volume
		int panpot = istream.read();
		istream.skip(5);
		int startMarker = istream.read();
		istream.skip(7);
		int instrument = istream.read();
		istream.skip(3);
		int group = istream.read();
		istream.skip(13);
		String trackName = readString(istream);
		System.out.println(trackNo+" "+instrument+" "+trackName);

		Extension3mleTrack lastTrack = trackList.getLast();
		if ( (lastTrack.getGroup() != group) || (lastTrack.getInstrument() != instrument) || (lastTrack.getPanpot() != panpot) || (lastTrack.isLimit())) {
			// new Track
			trackList.add(new Extension3mleTrack(instrument, group, panpot, trackName, startMarker));
		} else {
			lastTrack.addTrack();
		}
	}

	private int convertTick(int tick) {
		if (resolution == MMLTickTable.TPQN) {
			return tick;
		}
		return (tick * MMLTickTable.TPQN / resolution);
	}

	private int readLEIntValue(InputStream istream) {
		byte[] b = new byte[4];
		try {
			istream.read(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	private short readLEShortValue(InputStream istream) {
		byte[] b = new byte[2];
		try {
			istream.read(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	private void parseMarker(ByteArrayInputStream istream) {
		List<Marker> markerList = score.getMarkerList();

		// parse Marker
		istream.skip(7);
		int tickOffset = convertTick( readLEIntValue(istream) );
		istream.skip(4);
		String name = readString(istream);
		System.out.println("Marker " + name + "=" + tickOffset);
		if (markerList != null) {
			markerList.add(new Marker(name, tickOffset));
		}
	}

	private String readString(InputStream istream) {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		int b;
		try {
			while ( (b = istream.read()) != 0 ) {
				ostream.write(b);
			}
			return ostream.toString(encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		try {
			String str = "c=3902331007\nd=4wAAAJvYl0oBAAAAQlpoOTFBWSZTWReDTXYAAEH/i/7U0AQCAHgAQAAEAGwIEABAAECAAAoABKAAcivUCaZGmRiAyNqDEgnqRpkPTUZGh5S6QfOGHRg+AfSJE3ebNDxInstECT3owI1yYiuIY5IwTCLAQz1oZyAogJFOhVYmv39cWsLxsbh0MkELhClECHm5wCBjLYz8XckU4UJAXg012A==";
			str = "c=1269420526\n"
					+ "d=4wAAADQf0IYBAAAAQlpoOTFBWSZTWeWlqEkAAEL/i/7U0AQCAHgAQAAEADwIEEBAAECAAAoABKAAdBkp6IAAGgBtQZFB6IBoAANIlymxq3am6cmZmRF5lIvQDYpBCCcc\n"
					+ "d=lAGcFJEEGKGSQKoC366GV40QB5S5BBKDD35hGIZ0TE03VMVBYo53pYu61QliAs034u5IpwoSHLS1CSA=\n";
			str = "c=1888284627\n"
					+ "d=4wAAAHCCN2ABAAAAQlpoOTFBWSZTWSTXaLYAAEL/j/7U0AQCAHgAQAAEADwIEABAAEAAQIAACgAEoAByI9VNNGgAAyAbUEiSjyQaPSGEAbUsroXKl92irNLbSjcSRgw0\n"
					+ "d=TqwNzBHXtAGNYSMmYNRBKQSp7yxVJmLCafNmb3rP7+sa6ltkSJVfWpKgpI1vop3tOwFERjnfi7kinChIEmu0WwA=\n";
			MMLFile mmlFile = new MMLFile();
			List<Extension3mleTrack> trackList = mmlFile.parse3mleExtension(str);
			for (Extension3mleTrack track : trackList) {
				System.out.println(track);
			}
		} catch (MMLParseException e) {
			e.printStackTrace();
		}
	}
}
