/*
 * Copyright (C) 2014-2024 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mmlTools.core.MMLText;
import jp.fourthline.mmlTools.core.NanoTime;
import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;
import jp.fourthline.mmlTools.parser.IMMLFileParser;
import jp.fourthline.mmlTools.parser.MMLParseException;

public class MMLScoreTest extends UseLoadingDLS {

	@Before
	public void setup() {
		MMLBuilder.setMMLVZeroTempo(true);
		MMLScore.setMMLFix64(false);
	}

	@After
	public void cleanup() {
		MMLBuilder.setMMLVZeroTempo(false);
		MMLScore.setMMLFix64(true);
	}

	/**
	 * testLocalMMLParseでみているローカルのファイルに対して上書きします.
	 * 最適化向上した際の更新用です.
	 * 更新するばあい trueに設定.
	 */
	private static final boolean overwriteToLocalMMLOption = false;

	private MMLScore score = new MMLScore();

	public static void checkMMLScoreWriteToOutputStream(MMLScore score, InputStream inputStream) {
		try {
			int size = inputStream.available();
			byte[] expectBuf = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			// MMLScore -> mmi check
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			new MMLScoreSerializer(score).writeToOutputStream(outputStream);
			String mmiOutput = outputStream.toString(StandardCharsets.UTF_8);
			assertEquals(new String(expectBuf), mmiOutput);

			// mmi -> re-parse check
			ByteArrayInputStream bis = new ByteArrayInputStream(mmiOutput.getBytes());
			MMLScore reparseScore = new MMLScoreSerializer(new MMLScore()).parse(bis);
			assertEquals(mmiOutput, new String(reparseScore.getObjectState()));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private void checkMMLFileOutput(MMLScore score, String expectFileName, String[] expectMML) {
		try {
			/* MMLScore.writeToOutputStream() */
			InputStream inputStream = fileSelect(expectFileName);
			checkMMLScoreWriteToOutputStream(score, inputStream);

			/* MMLScore.parse() */
			inputStream = fileSelect(expectFileName);
			MMLScore inputScore = new MMLScoreSerializer(new MMLScore()).parse(inputStream).generateAll();
			inputStream.close();
			int i = 0;
			assertEquals(expectMML.length, inputScore.getTrackCount());
			for (MMLTrack track : inputScore.getTrackList()) {
				assertEquals(expectMML[i++], track.getMabiMML());
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMMLFileFormat0() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track = new MMLTrack().setMML("MML@aaa,bbb,ccc,dd1;");
		track.setTrackName("track1");
		score.addTrack(track);
		score.getMarkerList().add(new Marker("marker1", 96));

		String[] mml = { "MML@aaa,bbb,ccc,dd1;" };

		checkMMLFileOutput(score.generateAll(), "format0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat1() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track1 = new MMLTrack().setMML("MML@at150aa1,bbb,ccc,dd1;");
		track1.setTrackName("track1");
		score.addTrack(track1);
		MMLTrack track2 = new MMLTrack().setMML("MML@aaa2,bbt120b,ccc,dd2;");
		track2.setTrackName("track2");
		track2.setProgram(4);
		track2.setSongProgram(120);
		score.addTrack(track2);

		String[] mml = {
				"MML@at150at120a1,bbb,ccc,dt150dt120v0d2.v8;",
				"MML@at150at120a2,bbb,ccc,dt150dt120v0dv8;"
		};

		checkMMLFileOutput(score.generateAll(), "format1.mmi", mml);
	}

	@Test
	public void testMMLFileFormat1_ex() throws MMLExceptionList, MMLVerifyException {
		/* MMLScore.parse() */
		try {
			MMLScore score1 = new MMLScoreSerializer(new MMLScore()).parse(fileSelect("format1.mmi")).generateAll();
			MMLScore score2 = new MMLScoreSerializer(new MMLScore()).parse(fileSelect("format1_ex.mmi")).generateAll();

			assertArrayEquals(score1.getObjectState(), score2.getObjectState());
		} catch (MMLParseException | IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testMMLFileFormat_r0() throws MMLExceptionList, MMLVerifyException {
		MMLBuilder.setMMLVZeroTempo(true);

		MMLTrack track = new MMLTrack().setMML("MML@r1t180c8;");
		track.setTrackName("track1");
		track.setOptTempoOnlyMelody(true);
		score.addTrack(track);

		String[] mml = { "MML@v0c1t180v8c8,,;" };

		checkMMLFileOutput(score.generateAll(), "format_r0_tom.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r0_q() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track = new MMLTrack().setMML("MML@r1t180c8;");
		track.setTrackName("track1");
		track.setOptTempoOnlyMelody(false);
		score.addTrack(track);

		String[] mml = { "MML@v0c1t180v8c8,,;" };

		checkMMLFileOutput(score.generateAll(), "format_r0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r1() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track1 = new MMLTrack().setMML("MML@r1>f+1t120&f+1;");
		track1.setTrackName("track1");
		track1.setOptTempoOnlyMelody(true);
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack().setMML("MML@r1r1a+1;");
		track2.setTrackName("track2");
		track2.setOptTempoOnlyMelody(true);
		score.addTrack(track2);

		MMLTrack track3 = new MMLTrack().setMML("MML@d1;");
		track3.setTrackName("track3");
		track3.setOptTempoOnlyMelody(true);
		score.addTrack(track3);

		String[] mml = {
				"MML@l1r>f+t120v0f+v8,,;",
				"MML@l1rv0ct120v8a+,,;",
				"MML@d1,,;" // 後方にあるテンポは出力しない.
		};

		checkMMLFileOutput(score.generateAll(), "format_r1_tom.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r1_q() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track1 = new MMLTrack().setMML("MML@r1>f+1t120&f+1;");
		track1.setTrackName("track1");
		track1.setOptTempoOnlyMelody(false);
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack().setMML("MML@r1r1a+1;");
		track2.setTrackName("track2");
		track2.setOptTempoOnlyMelody(false);
		score.addTrack(track2);

		MMLTrack track3 = new MMLTrack().setMML("MML@d1;");
		track3.setTrackName("track3");
		track3.setOptTempoOnlyMelody(false);
		score.addTrack(track3);

		String[] mml = {
				"MML@l1r>f+&f+,l1rv0ct120,;",
				"MML@l1rv0ct120v8a+,,;",
				"MML@d1,,;" // 後方にあるテンポは出力しない.
		};

		checkMMLFileOutput(score.generateAll(), "format_r1.mmi", mml);
	}

	@Test
	public void testMMLFileFormat2() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track1 = new MMLTrack(768, -576, -672).setMML("MML@c1,,,e1;");
		track1.setSongProgram(110);
		track1.setTrackName("Track1");
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack(768, 0, 0).setMML("MML@d1,,;");
		track2.setTrackName("Track2");
		score.addTrack(track2);

		score.getTempoEventList().add(new MMLTempoEvent(140, 0));

		String[] mml = {
				"MML@t140c1,,,t140e1;",
				"MML@t140d1,,;",
		};

		checkMMLFileOutput(score.generateAll(), "format2.mmi", mml);
	}

	@Test
	public void testMMLFileFormat3() throws MMLExceptionList, MMLVerifyException {
		MMLTrack track1 = new MMLTrack(1152, 0, 0).setMML("MML@c1&c,,,<d;");
		track1.setSongProgram(110);
		track1.setTrackName("Track1");
		track1.setAttackDelayCorrect(-6);
		track1.setAttackSongDelayCorrect(-12);
		track1.setDisableNopt(true);
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack(1152, 0, 0).setMML("MML@<b1&b,,;");
		track2.setTrackName("Track2");
		track2.setVolume(25);
		score.addTrack(track2);

		score.getTempoEventList().add(new MMLTempoEvent(100, 0));
		score.getTempoEventList().add(new MMLTempoEvent(220, 1152));

		String[] mml = {
				"MML@t220c1&c8&c9,,,t220<d8&d16.;",
				"MML@t220<b1&b,,;",
		};

		checkMMLFileOutput(score.generateAll(), "format3.mmi", mml);
	}

	private PrintStream reportStream = null;
	private void printReport(String s1, String s2) {
		if (reportStream != null) {
			MMLText mml3 = new MMLText().setMMLText(s1);
			MMLText mml4 = new MMLText().setMMLText(s2);
			for (int i = 0; i < 4; i++) {
				int l1 = mml3.getText(i).length();
				int l2 = mml4.getText(i).length();
				if ( (l1 > 0) && (l2 > 0) ) {
					reportStream.printf("%d\t%d\n", l1, l2);
				}
			}
		}
	}

	/**
	 * ファイルをparseして, 出力文字が増加していないか確認する.
	 * @param filename
	 */
	private void mmlFileParse(String filename, boolean updateOption) {
		File file = new File(filename);
		if (file.exists()) {
			IMMLFileParser fileParser = IMMLFileParser.getParser(file);
			try {
				MMLScore score = fileParser.parse(new FileInputStream(file));
				System.out.println(filename);
				if (filename.startsWith("# ")) {
					return;
				}
				score.getTrackList().forEach(t -> {
					try {
						MMLText mml1 = new MMLText().setMMLText(t.getOriginalMML());
						String rank1 = mml1.mmlRankFormat();
						System.out.println("mml1: "+mml1.getMML());
						MMLTrack.setMabiMMLOptimizeFunc(optNormal);
						t.generate();
						MMLTrack.setMabiMMLOptimizeFunc(tt -> tt.toString());
						MMLText mml2 = new MMLText().setMMLText(t.getOriginalMML());
						System.out.println("mml2: "+mml2.getMML());
						String rank2 = mml2.mmlRankFormat();
						String rank3 = t.mmlRankFormat();
						if (!mml1.getMML().equals(mml2.getMML())) {
							System.err.print("!!! "+t.getTrackName()+" ");
							System.err.println(rank1 + " -> " + rank2 + ", " + rank3);
						}
						System.out.println(rank1 + " -> " + rank2 + ", " + rank3);
//						assertTrue(mml1.getText(0).length() >= mml2.getText(0).length()*0.97);
						assertTrue(mml1.getText(1).length() >= mml2.getText(1).length()*0.97);
						assertTrue(mml1.getText(2).length() >= mml2.getText(2).length()*0.97);
						assertTrue(mml1.getText(3).length() >= mml2.getText(3).length()*0.97);
						assertEquals(new MMLTrack().setMML(mml1.getMML()), new MMLTrack().setMML(mml2.getMML()));

						String mabiMMLoptGen1 = t.getMabiMML();
						MMLTrack.setMabiMMLOptimizeFunc(optGen2);
						String mabiMMLoptGen2 = t.generate().getMabiMML();
						MMLTrack.setMabiMMLOptimizeFunc(optGen3);
						String mabiMMLoptGen3 = t.generate().getMabiMML();
						MMLTrack.setMabiMMLOptimizeFunc(tt -> tt.toString());
						System.out.println("gen1: " + mabiMMLoptGen1);
						System.out.println("gen2: " + mabiMMLoptGen2);
						System.out.println("gen3: " + mabiMMLoptGen3);
						System.out.println("gen1: " + mabiMMLoptGen1.length() + ", gen2: " + mabiMMLoptGen2.length() + ", gen3: " + mabiMMLoptGen3.length());
						assertTrue(mabiMMLoptGen1.length() >= mabiMMLoptGen2.length());
						assertTrue(mabiMMLoptGen2.length() >= mabiMMLoptGen3.length());
						MMLTrack.setMabiMMLOptimizeFunc(null);

						// reparse
						String re1 = new MMLTrack().setMML(mabiMMLoptGen1).generate().getMabiMML();
						String re2 = new MMLTrack().setMML(mabiMMLoptGen2).generate().getMabiMML();
						String re3 = new MMLTrack().setMML(mabiMMLoptGen3).generate().getMabiMML();
//						assertEquals(re1, re2);
//						assertEquals(re1, re3);
						assertTrue(new MMLTrack().setMML(re1).equals(new MMLTrack().setMML(re2)));
						assertTrue(new MMLTrack().setMML(re1).equals(new MMLTrack().setMML(re3)));

						printReport(mabiMMLoptGen1, mabiMMLoptGen2);
					} catch (MMLExceptionList | MMLVerifyException e) {
						fail(e.getMessage());
					}
				});

				try {
					long t1 = System.currentTimeMillis();
					score.generateAll();
					long t2 = System.currentTimeMillis();
					System.out.println("MMLScore generateAll: "+(t2-t1)+"ms");
				} catch (MMLExceptionList | MMLVerifyException e) {
					fail(e.getMessage());
				}

				if (updateOption) {
					try {
						score.generateAll();
						new MMLScoreSerializer(score).writeToOutputStream(new FileOutputStream(file));
					} catch (MMLExceptionList | MMLVerifyException e) {
						fail(e.getMessage());
					}
				}
			} catch (MMLParseException | FileNotFoundException e) {}
		} else {
			System.out.println("  -> not found. ");
		}
	}

	private final MMLOptimizerPerfoｒmanceCounter optNormal = new MMLOptimizerPerfoｒmanceCounter("Normal", t -> t.optimize(true));
	private final MMLOptimizerPerfoｒmanceCounter optGen2   = new MMLOptimizerPerfoｒmanceCounter("Gen2  ", t -> t.optimizeGen2());
	private final MMLOptimizerPerfoｒmanceCounter optGen3   = new MMLOptimizerPerfoｒmanceCounter("Gen3  ", t -> t.optimizeGen3());
	/**
	 * ローカルのファイルを読み取って, MML最適化に劣化がないかどうかを確認するテスト.
	 */
	@Test
	public void testLocalMMLParse() {
		try {
			String listFile = "localMMLFileList.txt";
			InputStream stream = fileSelect(listFile);
			if (stream == null) {
				fail("not found "+listFile);
				return;
			}
			InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			new BufferedReader(reader).lines().forEach(s -> {
				System.out.println(s);
				mmlFileParse(s, overwriteToLocalMMLOption);
			});
		} catch (IOException e) {}

		optNormal.printReport();
		optGen2.printReport();
		optGen3.printReport();
	}

	/**
	 * 指定Tickにあるノートをすべて取得する.
	 */
	@Test
	public void test_getNoteListOnTickOffset() {
		score.addTrack(new MMLTrack().setMML("MML@c,,rd"));
		score.addTrack(new MMLTrack().setMML("MML@rf,e"));
		score.addTrack(new MMLTrack().setMML("MML@,ra,g"));

		MMLNoteEvent[] expect1 = {
				new MMLNoteEvent(48, 96, 0),
				new MMLNoteEvent(52, 96, 0),
				new MMLNoteEvent(55, 96, 0)
		};
		MMLNoteEvent[] expect2 = {
				new MMLNoteEvent(50, 96, 96),
				new MMLNoteEvent(53, 96, 96),
				new MMLNoteEvent(57, 96, 96)
		};

		assertArrayEquals(expect1,
				score.getNoteListOnTickOffset(0).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).toArray());
		assertArrayEquals(expect2,
				score.getNoteListOnTickOffset(96).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).toArray());
		assertEquals(0,
				score.getNoteListOnTickOffset(96+96).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).count());
	}

	private void checkGenerateAll(String mml, String expect, boolean tempoOnlyMelody) throws MMLExceptionList, MMLVerifyException {
		var track = new MMLTrack().setMML(mml);
		track.setOptTempoOnlyMelody(tempoOnlyMelody);
		score.addTrack(track);
		score.generateAll();
		assertEquals(expect, score.getTrack(0).getMabiMML());
	}

	@Test
	public void test_v0ct_temp1() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに d がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l1rd,;",
				"MML@l2dv0ct130rv8g,l1rd,;",
				false);
	}

	@Test
	public void test_v0ct_temp2() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに c がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l2rc,;",
				"MML@l2dv0dt130rv8g,l2rc,;",
				true);
	}

	@Test
	public void test_v0ct_temp3() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに c, d がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l2rc,l2rd;",
				"MML@l2dv0et130rv8g,l2rc,l2rd;",
				true);
	}

	@Test
	public void test_v0ct_temp4() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに c, d がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l2rd,l2rc;",
				"MML@l2dv0et130rv8g,l2rd,l2rc;",
				true);
	}

	@Test
	public void test_v0ct_temp5() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに b, c がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l2rb,l2rc;",
				"MML@l2dv0dt130rv8g,l2rb,l2rc;",
				true);
	}

	@Test
	public void test_v0ct_temp6() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに g, a がある場合のテンポ補正 */
		checkGenerateAll(
				"MML@l2drt130rv8g,l2rg,l2ra;",
				"MML@l2dv0ct130rv8g,l2rg,l2ra;",
				true);
	}

	@Test
	public void test_v0ct_temp7() throws MMLExceptionList, MMLVerifyException {
		/* 長い休符の場合最後のみ */
		checkGenerateAll(
				"MML@l1r.r.rrt130,,l1r.r.rrc;",
				"MML@l1r.r.rv0dt130,,l1r.r.rrc;",
				true);
	}

	@Test
	public void test_v0ct_temp8() throws MMLExceptionList, MMLVerifyException {
		/* とちゅうで切る場合 */
		checkGenerateAll(
				"MML@l1r.r.rt240,v12ccccdd2deeec2fffggggaaaabbbb>c1,d1d1.d1.;",
				"MML@l1r.r.v0et240,v12ccccdd2deeec2fffggggaaaabbbbb+1,l1dd.d.;",
				true);
	}

	@Test
	public void test_v0ct_temp9() throws MMLExceptionList, MMLVerifyException {
		/* 他のパートに c, d がある場合のテンポ補正, 和音1にテンポ */
		checkGenerateAll(
				"MML@l2rd,l2drt130rv8g,l2rc;",
				"MML@l2rd,l2dv0et130rv8g,l2rc;",
				false);
	}

	@Test
	public void test_v0ct_temp10() throws MMLExceptionList, MMLVerifyException {
		/* 和音2にテンポをつくる */
		checkGenerateAll(
				"MML@l2rd,l2rc,l2drt130rv8g;",
				"MML@l2rd,l2rc,l2dv0et130rv8g;",
				false);
	}

	@Test
	public void test_tempo_q01() throws MMLExceptionList, MMLVerifyException {
		/* メロディ以外 */
		checkGenerateAll(
				"MML@l1>c&c&c,ggggt180gggggggg,;",
				"MML@l1.>c&c,ggggt180gggggggg,;",
				false);
	}

	@Test
	public void test_getTotalTickLength() {
		score.addTrack(new MMLTrack().setMML("MML@c1"));

		assertEquals(384, score.getTotalTickLength());
		assertEquals(384, score.getTotalTickLengthWithAll());

		score.getMarkerList().add(new Marker("test", 1000));
		score.getMarkerList().add(new Marker("test", 800));
		assertEquals(384, score.getTotalTickLength());
		assertEquals(1000, score.getTotalTickLengthWithAll());

		score.getTempoEventList().add(new MMLTempoEvent(90, 2000));
		score.getTempoEventList().add(new MMLTempoEvent(92, 1800));
		assertEquals(384, score.getTotalTickLength());
		assertEquals(2000, score.getTotalTickLengthWithAll());
	}

	public static class MMLOptimizerPerfoｒmanceCounter implements Function<MMLStringOptimizer, String> {
		private long output = 0;
		private long time = 0;
		private long cachedTime = 0;
		private final String name;
		private final Function<MMLStringOptimizer, String> f;

		public MMLOptimizerPerfoｒmanceCounter(String name, Function<MMLStringOptimizer, String> func) {
			this.name = name;
			this.f = func;
		}

		public void printReport() {
			System.out.println(name+": output = " + output + ", time = " + (time/1000) + "(" + (cachedTime/1000) + ") [ms], speed = " + output/(time/1000) + "(" + output/(cachedTime/1000) + ") [k/s]");
		}

		@Override
		public String apply(MMLStringOptimizer t) {
			MMLStringOptimizer.clearAllCache();

			NanoTime time = NanoTime.start();
			String ret = f.apply(t);
			this.time += time.us();
			output += ret.length();

			// 同じ処理をしたときのキャッシュ効果を測定する.
			NanoTime ctime = NanoTime.start();
			String ret2 = f.apply(t);
			if (!ret.equals(ret2)) {
				throw new AssertionError();
			}
			this.cachedTime += ctime.us();

			return ret;
		}
	}

	/**
	 * Delta設定済みでStartOffsetを設定するテスト
	 */
	@Test
	public void test_setStartOffsetAll_0() {
		score.addTrack(new MMLTrack().setMML("MML@rrc1"));
		score.getTrack(0).setStartDelta(96);
		assertTrue(score.setStartOffsetAll(48));
		assertEquals(48, score.getTrack(0).getCommonStartOffset());
	}

	/**
	 * Delta設定済みでStartOffsetを設定するテスト (エラー)
	 */
	@Test(expected=IllegalArgumentException.class)
	public void test_setStartOffsetAll_0i() {
		score.addTrack(new MMLTrack().setMML("MML@c1"));
		score.getTrack(0).setStartDelta(96);
	}

	/**
	 * Delta設定済みでマイナス方向に振り切るテスト
	 */
	@Test
	public void test_setStartOffsetAll_1() {
		score.addTrack(new MMLTrack().setMML("MML@c1"));
		score.setStartOffsetAll(96);
		score.getTrack(0).setStartDelta(-48);
		assertFalse(score.setStartOffsetAll(0));
		assertEquals(96, score.getTrack(0).getCommonStartOffset());
	}

	/**
	 * SongDelta設定済みでStartOffsetを設定するテスト
	 */
	@Test
	public void test_setStartOffsetAll_2() {
		score.addTrack(new MMLTrack().setMML("MML@,,,rrc1"));
		score.getTrack(0).setStartSongDelta(96);
		assertTrue(score.setStartOffsetAll(48));
		assertEquals(48, score.getTrack(0).getCommonStartOffset());
	}

	/**
	 * SongDelta設定済みでStartOffsetを設定するテスト (エラー)
	 */
	@Test(expected=IllegalArgumentException.class)
	public void test_setStartOffsetAll_2i() {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@,,,c1"));
		score.getTrack(0).setStartSongDelta(96);
	}

	/**
	 * SongDelta設定済みでマイナス方向に振り切るテスト
	 */
	@Test
	public void test_setStartOffsetAll_3() {
		score.addTrack(new MMLTrack().setMML("MML@,,,c1"));
		score.setStartOffsetAll(96);
		score.getTrack(0).setStartSongDelta(-48);
		assertFalse(score.setStartOffsetAll(0));
		assertEquals(96, score.getTrack(0).getCommonStartOffset());
	}

	/**
	 * t120テスト
	 * @throws MMLException 
	 */
	@Test
	public void test_setStartOffsetAll_4() throws MMLExceptionList, MMLVerifyException {
		score.addTrack(new MMLTrack(0, 0, 96).setMML("MML@d,,,c"));
		score.getTrack(0).setSongProgram(110);
		score.getTempoEventList().add(new MMLTempoEvent(140, 0));
		score.getTempoEventList().add(new MMLTempoEvent(120, 96));
		score.generateAll();
		assertEquals("MML@t140d,,,t120c;", score.getTrack(0).getMabiMML());
	}

	/**
	 * addTrack test
	 * @throws MMLException 
	 */
	@Test
	public void test_setStartOffsetAll_5() throws MMLExceptionList, MMLVerifyException {
		score.addTrack(new MMLTrack(0, 0, 96).setMML("MML@d,,,c"));
		score.getTrack(0).setSongProgram(110);
		score.getTempoEventList().add(new MMLTempoEvent(140, 0));
		score.getTempoEventList().add(new MMLTempoEvent(120, 96));
		score.setStartOffsetAll(384);
		score.generateAll();
		assertEquals("MML@t140d,,,t120c;", score.getTrack(0).getMabiMML());
		score.addTrack(new MMLTrack().setMML("MML@t170g"));
		score.generateAll();
		assertEquals("MML@t170d,,,t120c;", score.getTrack(0).getMabiMML());
		assertEquals("MML@t170g,,;", score.getTrack(1).getMabiMML());
		score.getTrack(0).setMML("MML@t88f");
		score.generateAll();
		assertEquals("MML@t88f,,,t120c;", score.getTrack(0).getMabiMML());
		assertEquals("MML@t88g,,;", score.getTrack(1).getMabiMML());
	}

	/**
	 * import MML test (delay)
	 * @throws MMLException 
	 */
	@Test
	public void test_setMabiMML_0() throws MMLExceptionList, MMLVerifyException {
		score.addTrack(new MMLTrack(0, 0, 96).setMML("MML@d,,,c"));
		score.getTempoEventList().add(new MMLTempoEvent(140, 0));
		score.getTrack(0).setSongProgram(110);
		score.setStartOffsetAll(384);
		score.getTrack(0).setAttackDelayCorrect(-6);
		score.getTrack(0).setAttackSongDelayCorrect(6);
		score.generateAll();
		assertEquals("MML@t140d,,,t140c;", score.getTrack(0).getOriginalMML());
		assertEquals("MML@t140d8&d9,,,t140r64c;", score.getTrack(0).getMabiMML());
		score.getTrack(0).setMabiMML("MML@t140d8&d9,,,t140r64c;");
		score.generateAll();
		assertEquals("MML@t140r64d8&d9,,,t140c;", score.getTrack(0).getOriginalMML());
		assertEquals("MML@t140d8&d9,,,t140r64c;", score.getTrack(0).getMabiMML());
	}

	@Test
	public void test_trackClone() throws MMLExceptionList, MMLVerifyException {
		score.addTrack(new MMLTrack().setMML("MML@T60aaaT90bbbb;"));
		score.addTrack(score.getTrack(0).clone());
		score.generateAll();

		assertEquals(2, score.getTrackCount());
		assertTrue(score.getTrack(0) != score.getTrack(1));
		assertEquals("MML@t60aaat90bbbb,,;", score.getTrack(0).getMabiMML());
		assertEquals("MML@t60aaat90bbbb,,;", score.getTrack(1).getMabiMML());
	}

	@Test
	public void test_fix64() throws MMLExceptionList, MMLVerifyException {
		MMLScore.setMMLFix64(true);
		var track = new MMLTrack();
		score.addTrack(track);

		score.generateAll();
		// (2023/03/12 無条件でfix64有効に変更)
		assertEquals(1, score.getTrackCount());
		assertEquals(true, track.getFix64());

		// 楽器パート部のみのMML (2023/03/12 無条件でfix64有効に変更)
		track.setMML("MML@cccc,cccc,cccc,;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		// 楽器パートと歌パート部のMML
		track.setMML("MML@cccc,cccc,cccc,cccc;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		track.setMML("MML@cccc,,,cccc;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		track.setMML("MML@,cccc,,cccc;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		track.setMML("MML@,,cccc,cccc;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		// 歌パート部のみのMML (2023/03/12 無条件でfix64有効に変更)
		track.setMML("MML@,,,cccc;");
		score.generateAll();
		assertEquals(true, track.getFix64());

		// 複数トラック
		score.addTrack(new MMLTrack());
		score.generateAll();
		assertEquals(true, track.getFix64());
	}
}
