/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import fourthline.FileSelect;
import fourthline.mmlTools.MMLScore;

public class MMSFileTest extends FileSelect {

	@Test
	public final void testParse() {
		try {
			MMLScore score = new MMSFile().parse(fileSelect("sample1.mms"));
			assertEquals(1, score.getTrackCount());
			assertEquals("test", score.getTitle());
			assertEquals("noname", score.getAuthor());
			assertEquals("3/4", score.getBaseTime());
		} catch (IOException | MMLParseException e) {
			fail(e.getMessage());
		}
	}
}
