/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;

/**
 * @author fourthline
 *
 */
public class MMLEventListTest {

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
	 * @throws MMLException 
	 */
	@Test
	public void testGetMMLNoteEventList() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("aaa");
		List<MMLNoteEvent> expectList = 
				Arrays.asList(
						new MMLNoteEvent(57, t4, 0),
						new MMLNoteEvent(57, t4, t4),
						new MMLNoteEvent(57, t4, t4*2)
						);

		List<MMLNoteEvent> noteList = eventList.getMMLNoteEventList();
		System.out.println(noteList);
		System.out.println(expectList);
		assertEquals(expectList.toString(), noteList.toString());
	}

	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_0() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("ab-a");

		// 2つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t4, t4));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_1() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16aa");

		// 1つ目のノートの途中位置に新しく挿入します.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}


	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_2() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-a");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t4, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}


	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_3() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		int t8 = MMLTicks.getTick("8");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-8r8a");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目のノートを上書きしますが、tickが足りないパターンです.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t8, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_4() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-4&b-4");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目、3つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t4+t4, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLEventList#addMMLNoteEvent(jp.fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws MMLException 
	 */
	@Test
	public void testAddMMLNoteEvent_5() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("rra");
		MMLEventList expectList = new MMLEventList("rb-a");

		// 休符の位置に挿入します.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t4, t4));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * @throws MMLException 
	 */
	@Test
	public void testSearchOnTickOffset() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("ab-a");

		// 0
		MMLNoteEvent noteEvent0 = eventList.searchOnTickOffset(0);
		assertEquals(57, noteEvent0.getNote());

		// 1
		MMLNoteEvent noteEvent1 = eventList.searchOnTickOffset(t4-1);
		assertEquals(57, noteEvent1.getNote());

		// 2
		MMLNoteEvent noteEvent2 = eventList.searchOnTickOffset(t4);
		assertEquals(58, noteEvent2.getNote());

		// 2
		MMLNoteEvent noteEvent3 = eventList.searchOnTickOffset(t4+t16);
		assertEquals(58, noteEvent3.getNote());
	}

	/**
	 * @throws MMLException 
	 */
	@Test
	public void testSearchPrevNoteOnTickOffset() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("ab-a");

		// 0
		MMLNoteEvent noteEvent0 = eventList.searchPrevNoteOnTickOffset(0);
		assertEquals(null, noteEvent0);

		// 1
		MMLNoteEvent noteEvent1 = eventList.searchPrevNoteOnTickOffset(t4);
		assertEquals(57, noteEvent1.getNote());

		// 2
		MMLNoteEvent noteEvent2 = eventList.searchPrevNoteOnTickOffset(t4+t4-1);
		assertEquals(58, noteEvent2.getNote());

		// 2
		MMLNoteEvent noteEvent3 = eventList.searchPrevNoteOnTickOffset(t4+t4);
		assertEquals(58, noteEvent3.getNote());
	}

	@Test
	public void testToMMLString_0() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("ara");

		String mml = MMLBuilder.create(eventList).toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * オクターブ変化を付けたMMLの双方向変換のテスト.
	 * @throws MMLExceptionList
	 */
	@Test
	public void testToMMLString_1() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("cdef-gab>cdef+gab+<c1");

		String mml = MMLBuilder.create(eventList).toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tieによる連結が必要な音価さん.
	 * @throws MMLExceptionList
	 */
	@Test
	public void testToMMLString_2() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("c4&c16g2&g8d1.&d1.&d1.&d24");

		String mml = MMLBuilder.create(eventList).toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tempo, velocity を含むMML.
	 * @throws MMLExceptionList
	 */
	@Test
	public void testToMMLString_3() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("T150v10c8.g16e4v8g-<at120<b-");

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tempo, velocity を含むMML. (テンポを跨ぐNoteがある)
	 * @throws MMLExceptionList 
	 */
	@Test
	public void testToMMLString_4() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("c2d4");
		List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();
		String expectMML = "c8t150v0c8t130c4v8d4";
		globalTempoList.add(new MMLTempoEvent(150, 48));
		globalTempoList.add(new MMLTempoEvent(130, 96));
		eventList.setGlobalTempoList(globalTempoList);

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (テンポ指定が最終ノートより後方にある)
	 * @throws MMLExceptionList 
	 */
	@Test
	public void testToMMLString_5() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("c4d4");
		String expectMML = "c4d4r1.r1.t150";
		List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();
		globalTempoList.add(new MMLTempoEvent(150, 96*2+96*12));
		eventList.setGlobalTempoList(globalTempoList);

		String mml = MMLBuilder.create(eventList).toMMLString(true, false);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (rTn式)
	 * @throws MMLExceptionList
	 */
	@Test
	public void testToMMLString_7() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("c1r1t150c1");
		String expectMML = "c1v0c1t150v8c1";

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (r後のv)
	 * @throws MMLExceptionList
	 */
	@Test
	public void testToMMLString_8() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("v12rc");
		String expectMML = "r4v12c4";

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * 調律符の判定テスト.
	 * @throws MMLExceptionList
	 */
	@Test
	public void testTuningNote() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("c64&c64&c64&c64");
		String expectMML = "c64&c64&c64&c64";

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * オクターブ上限のテスト.
	 */
	@Test
	public void testTopOctave() {
		MMLEventList eventList = new MMLEventList("c>>>>>c<<<<c");
		MMLNoteEvent event1 = eventList.getMMLNoteEventList().get(0);
		MMLNoteEvent event2 = eventList.getMMLNoteEventList().get(2);

		assertEquals(event1.getNote(), event2.getNote());
	}

	/**
	 * オクターブ下限のテスト.
	 */
	@Test
	public void testBottomOctave() {
		MMLEventList eventList = new MMLEventList("c<<<<<c>>>>c");
		MMLNoteEvent event1 = eventList.getMMLNoteEventList().get(0);
		MMLNoteEvent event2 = eventList.getMMLNoteEventList().get(2);

		assertEquals(event1.getNote(), event2.getNote());
	}

	/**
	 * テンポ＋休符のテスト.
	 * @throws MMLExceptionList
	 */
	@Test
	public void testTempoAndR() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("v12t110rt60rt90c");
		String expectMML = "t110v0c4t60c4t90v12c4";

		String mml = MMLBuilder.create(eventList).toMMLString(true, true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * list2に1つだけ
	 */
	@Test
	public void testAlignmentStartTick0() {
		MMLEventList eventList1 = new MMLEventList("rrrc");
		MMLEventList eventList2 = new MMLEventList("rrc2");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList2.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentStartTick(eventList2, note1.getTickOffset());

		assertEquals(note2.getTickOffset(), result);
	}

	/**
	 * list1のひとつ前まで.
	 */
	@Test
	public void testAlignmentStartTick1() {
		MMLEventList eventList1 = new MMLEventList("rcrc");
		MMLEventList eventList2 = new MMLEventList("r.c2");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList1.getMMLNoteEventList().get(1);

		int result = eventList1.getAlignmentStartTick(eventList2, note2.getTickOffset());

		assertEquals(note1.getTickOffset(), result);
	}

	/**
	 * 同じoffset
	 */
	@Test
	public void testAlignmentStartTick2() {
		MMLEventList eventList1 = new MMLEventList("rc");
		MMLEventList eventList2 = new MMLEventList("rc");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList1.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentStartTick(eventList2, note1.getTickOffset());

		assertEquals(note2.getTickOffset(), result);
	}

	/**
	 * endTick = tickOffset
	 */
	@Test
	public void testAlignmentStartTick3() {
		MMLEventList eventList1 = new MMLEventList("c");
		MMLEventList eventList2 = new MMLEventList("rc");

		MMLNoteEvent note1 = eventList2.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentStartTick(eventList2, note1.getTickOffset());

		assertEquals(note1.getTickOffset(), result);
	}

	/**
	 * list2に1つだけ
	 */
	@Test
	public void testAlignmentEndTick0() {
		MMLEventList eventList1 = new MMLEventList("rrc");
		MMLEventList eventList2 = new MMLEventList("rrc2");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList2.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentEndTick(eventList2, note1.getEndTick());

		assertEquals(note2.getEndTick(), result);
	}

	/**
	 * list1の1つ後ろまで.
	 */
	@Test
	public void testAlignmentEndTick1() {
		MMLEventList eventList1 = new MMLEventList("rcrc");
		MMLEventList eventList2 = new MMLEventList("r.c2");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList1.getMMLNoteEventList().get(1);

		int result = eventList1.getAlignmentEndTick(eventList2, note1.getEndTick());

		assertEquals(note2.getEndTick(), result);
	}

	/**
	 * 同じoffset
	 */
	@Test
	public void testAlignmentEndTick2() {
		MMLEventList eventList1 = new MMLEventList("rc");
		MMLEventList eventList2 = new MMLEventList("rc");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);
		MMLNoteEvent note2 = eventList1.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentEndTick(eventList2, note1.getEndTick());

		assertEquals(note2.getEndTick(), result);
	}

	/**
	 * endTick = tickOffset
	 */
	@Test
	public void testAlignmentEndTick3() {
		MMLEventList eventList1 = new MMLEventList("c");
		MMLEventList eventList2 = new MMLEventList("rc");

		MMLNoteEvent note1 = eventList1.getMMLNoteEventList().get(0);

		int result = eventList1.getAlignmentEndTick(eventList2, note1.getEndTick());

		assertEquals(note1.getEndTick(), result);
	}

	/**
	 * swap
	 * @throws MMLExceptionList
	 */
	@Test
	public void testSwap0() throws MMLExceptionList {
		MMLEventList eventList1 = new MMLEventList("rcrc");
		MMLEventList eventList2 = new MMLEventList("r.c2");

		String mml1 = MMLBuilder.create(eventList1).toMMLString();
		String mml2 = MMLBuilder.create(eventList2).toMMLString();
		int endTick = eventList1.getAlignmentEndTick(eventList2, eventList1.getMMLNoteEventList().get(1).getEndTick());

		eventList1.swap(eventList2, 0, endTick);
		System.out.println(eventList1.getMMLNoteEventList().size());
		System.out.println(eventList2.getMMLNoteEventList().size());

		assertEquals(mml1, MMLBuilder.create(eventList2).toMMLString());
		assertEquals(mml2, MMLBuilder.create(eventList1).toMMLString());
	}

	/**
	 * move
	 * @throws MMLExceptionList
	 */
	@Test
	public void testMove0() throws MMLExceptionList {
		MMLEventList eventList1 = new MMLEventList("rcrc");
		MMLEventList eventList2 = new MMLEventList("r.c2");

		String mml1 = MMLBuilder.create(eventList1).toMMLString();
		int endTick = eventList1.getAlignmentEndTick(eventList2, eventList1.getMMLNoteEventList().get(1).getEndTick());

		eventList1.move(eventList2, 0, endTick);
		System.out.println(eventList1.getMMLNoteEventList().size());
		System.out.println(eventList2.getMMLNoteEventList().size());

		assertEquals(mml1, MMLBuilder.create(eventList2).toMMLString());
		assertEquals("", MMLBuilder.create(eventList1).toMMLString());
	}

	/**
	 * copy
	 * @throws MMLExceptionList
	 */
	@Test
	public void testCopy0() throws MMLExceptionList {
		MMLEventList eventList1 = new MMLEventList("rcrc");
		MMLEventList eventList2 = new MMLEventList("r.c2");

		String mml1 = MMLBuilder.create(eventList1).toMMLString();
		int endTick = eventList1.getAlignmentEndTick(eventList2, eventList1.getMMLNoteEventList().get(1).getEndTick());

		eventList1.copy(eventList2, 0, endTick);
		System.out.println(eventList1.getMMLNoteEventList().size());
		System.out.println(eventList2.getMMLNoteEventList().size());

		assertEquals(mml1, MMLBuilder.create(eventList2).toMMLString());
		assertEquals(mml1, MMLBuilder.create(eventList1).toMMLString());
	}

	@Test
	public void testIndexOfMMLString_1() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("rrcd&drrf.");

		assertArrayEquals( new int[]{0, 2}, eventList.indexOfMMLString(0) );
		assertArrayEquals( new int[]{0, 2}, eventList.indexOfMMLString(t4) );
		assertArrayEquals( new int[]{0, 2}, eventList.indexOfMMLString(t4*2-1) );
		assertArrayEquals( new int[]{2, 3}, eventList.indexOfMMLString(t4*2) );
		assertArrayEquals( new int[]{2, 3}, eventList.indexOfMMLString(t4*3-1) );
		assertArrayEquals( new int[]{3, 6}, eventList.indexOfMMLString(t4*3) );
		assertArrayEquals( new int[]{3, 6}, eventList.indexOfMMLString(t4*4) );
		assertArrayEquals( new int[]{3, 6}, eventList.indexOfMMLString(t4*5-1) );
		assertArrayEquals( new int[]{6, 8}, eventList.indexOfMMLString(t4*5) );
		assertArrayEquals( new int[]{6, 8}, eventList.indexOfMMLString(t4*6) );
		assertArrayEquals( new int[]{6, 8}, eventList.indexOfMMLString(t4*7-1) );
		assertArrayEquals( new int[]{8, 10}, eventList.indexOfMMLString(t4*7) );
		assertArrayEquals( new int[]{8, 10}, eventList.indexOfMMLString(t4*8+t4/2-1) );
		assertArrayEquals( new int[]{10, 10}, eventList.indexOfMMLString(t4*8+t4/2) );
	}

	@Test
	public void testIndexOfMMLString_2() throws MMLException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("rrv10c8t250l16d");

		assertArrayEquals( new int[]{0, 5}, eventList.indexOfMMLString(0) );
		assertArrayEquals( new int[]{5, 7}, eventList.indexOfMMLString(t4*2) );
		assertArrayEquals( new int[]{14, 15}, eventList.indexOfMMLString(t4*2+t4/2) );
		assertArrayEquals( new int[]{15, 15}, eventList.indexOfMMLString(t4*4) );
	}

	@Test
	public void testIndexOfMMLString_3() throws MMLException {
		MMLEventList eventList = new MMLEventList("l64c&c&c&c&c&c&c&c");
		assertArrayEquals( new int[]{3, 18}, eventList.indexOfMMLString(0) );
	}

	@Test
	public void testV0_4() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("v0cdcd");
		assertEquals("v0c4d4c4d4", MMLBuilder.create(eventList).toMMLString());
	}

	@Test
	public void testSetUnsetVelocity_0() throws MMLExceptionList {
		MMLEventList eventList = new MMLEventList("v12cdefv7g");
		MMLEventList eventList2 = new MMLEventList("v12cdv10efv7g");
		MMLEventList eventList3 = new MMLEventList("v12cdefv7g");

		MMLNoteEvent target = eventList.getMMLNoteEventList().get(2);

		eventList.setVelocityCommand(target, 10);
		assertEquals(MMLBuilder.create(eventList2).toMMLString(), MMLBuilder.create(eventList).toMMLString());

		eventList.unsetVelocityCommand(target);
		assertEquals(MMLBuilder.create(eventList3).toMMLString(), MMLBuilder.create(eventList).toMMLString());
	}

	@Test
	public void test_clone() throws CloneNotSupportedException {
		MMLEventList eventList1 = new MMLEventList("t120cde");
		MMLEventList eventList2 = eventList1.clone();

		Objects.deepEquals(eventList1, eventList2);
		assertEquals(
				eventList1.getGlobalTempoList().get(0),
				eventList2.getGlobalTempoList().get(0));
		assertNotSame(
				eventList1.getGlobalTempoList().get(0),
				eventList2.getGlobalTempoList().get(0));
		assertEquals(
				eventList1.getMMLNoteEventList().get(0),
				eventList2.getMMLNoteEventList().get(0));
		assertNotSame(
				eventList1.getMMLNoteEventList().get(0),
				eventList2.getMMLNoteEventList().get(0));
	}

	@Test
	public void test_plus() throws MMLException {
		MMLEventList eventList1 = new MMLEventList("c+");
		MMLEventList eventList2 = new MMLEventList("c#");
		assertEquals(eventList1.toString(), eventList2.toString());
	}

	@Test
	public void test_illegalMML() throws MMLExceptionList {
		MMLEventList eventList1 = new MMLEventList("l8bNwMub+4.ebb+htt16b16c.cc+c2.");
		System.out.println(eventList1.getTickLength());
		MMLEventList eventList2 = new MMLEventList("l8bb+4.ebb+b16c.cc+c2.");
		System.out.println(eventList2.getTickLength());
		assertEquals(MMLBuilder.create(eventList1).toMMLString(), MMLBuilder.create(eventList2).toMMLString());
	}

	@Test
	public void testParse_m1() throws MMLExceptionList {
		MMLEventList eventList1 = new MMLEventList("o0c-");
		assertEquals("<<<<c-4", MMLBuilder.create(eventList1).toMMLString());
	}

	@Test
	public void testOverlap() {
		MMLEventList eventList = new MMLEventList("c8c8rc8c8");
		MMLNoteEvent e1 = new MMLNoteEvent(48, 96, 96, 8);
		assertEquals(false, eventList.isOverlapNote(e1));

		e1.setTickOffset(e1.getTickOffset()-1);
		assertEquals(true, eventList.isOverlapNote(e1));

		e1.setTickOffset(e1.getTickOffset()+2);
		assertEquals(true, eventList.isOverlapNote(e1));

		e1.setTick(e1.getTick()-1);
		assertEquals(false, eventList.isOverlapNote(e1));
	}

	private int deleteMinRestPattern(int start1, int tick1, int start2, int tick2) {
		MMLEventList eventList = new MMLEventList("");
		eventList.addMMLNoteEvent(new MMLNoteEvent(67, tick1, start1, 10));
		eventList.addMMLNoteEvent(new MMLNoteEvent(61, tick2, start2, 10));
		eventList.deleteMinRest();
		return eventList.getMMLNoteEventList().get(0).getTick();
	}

	@Test
	public void deleteMinRest() {
		assertEquals(6, deleteMinRestPattern(12, 6, 12+6, 48));
		assertEquals(7, deleteMinRestPattern(12, 6, 12+7, 48));
		assertEquals(8, deleteMinRestPattern(12, 7, 12+7+1, 48));
		assertEquals(6, deleteMinRestPattern(12, 7, 12+7+5, 48));
		assertEquals(11, deleteMinRestPattern(12, 11, 12+11, 48));
		assertEquals(6, deleteMinRestPattern(12, 11, 12+11+1, 48));
		assertEquals(7, deleteMinRestPattern(12, 11, 12+11+2, 48));
		assertEquals(8, deleteMinRestPattern(12, 11, 12+11+3, 48));
		assertEquals(9, deleteMinRestPattern(12, 11, 12+11+4, 48));
		assertEquals(10, deleteMinRestPattern(12, 11, 12+11+5, 48));
		assertEquals(11, deleteMinRestPattern(12, 11, 12+11+6, 48));
		assertEquals(11, deleteMinRestPattern(12, 11, 12+11+7, 48));
	}

	@Test
	public void deleteMinRestTopNote() {
		String s = "<unknown>";
		for (int i = 1; i <= 6; i++) {
			MMLEventList eventList = new MMLEventList("");
			eventList.addMMLNoteEvent(new MMLNoteEvent(52, 6, i));
			eventList.deleteMinRest();
			try {
				s = eventList.getInternalMMLString();
			} catch (MMLExceptionList e) {
				assertEquals("Undefined tick table: "+i, e.getMessage());
			}
		}
		assertEquals("r64e64", s);
	}

	@Test
	public void testTempoNotTuneBase() {
		String mml;
		MMLEventList eventList;

		// テンポで分割されている場合は調律符ではない.
		mml = "l16ft150&f";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(false, eventList.getMMLNoteEventList().get(0).isTuningNote());

		// 手動パターン.
		mml = "f64t160&f64&f32";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(false, eventList.getMMLNoteEventList().get(0).isTuningNote());

		// 3連結 (調律符として扱わない)
		mml = "f64t160&f64&f64";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(false, eventList.getMMLNoteEventList().get(0).isTuningNote());

		// 4連結 (調律符として扱う)
		mml = "f64t160&f64&f64&f64";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(true, eventList.getMMLNoteEventList().get(0).isTuningNote());

		// 手動パターン3.
		mml = "d64t150&d64&d64&d64&d64&d64&d64&d64&d64&d64";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(true, eventList.getMMLNoteEventList().get(0).isTuningNote());		

		// 手動パターン4.
		mml = "d64&d64&d64&d64&d64&d64&d64&d64&d64t150&d64";
		eventList = new MMLEventList(mml);
		assertEquals(1, eventList.getMMLNoteEventList().size());
		assertEquals(true, eventList.getMMLNoteEventList().get(0).isTuningNote());		
	}
}
