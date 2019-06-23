package solr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringUtilTest {
	private static StringUtil testClass;

	@BeforeAll
	static void setUp() throws Exception {
		testClass = new StringUtil();
	}

	@Test
	@DisplayName("対象文字列がnull")
	void test_01() {
		String str = null;
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(0, result.size(), "一覧の個数");
	}

	@Test
	@DisplayName("対象文字列が空文字")
	void test_02() {
		String str = null;
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(0, result.size(), "一覧の個数");
	}

	@Test
	@DisplayName("emタグ無し（あり得ないけど念のため）")
	void test_03() {
		String str = "hello world";
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(1, result.size(), "一覧の個数");
		StringUtil.StringEmphasis e = result.get(0);
		assertEquals(false, e.isEmphasis(), "協調文字列でないこと");
		assertEquals(str, e.getString(), "文字列");
	}

	@Test
	@DisplayName("対象文字列の両端がemタグ")
	void test_04() {
		String str = "<em>hello world</em>";
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(1, result.size(), "一覧の個数");
		StringUtil.StringEmphasis e = result.get(0);
		assertEquals(true, e.isEmphasis(), "協調文字列");
		assertEquals("hello world", e.getString(), "文字列");
	}

	@Test
	@DisplayName("emタグの連続")
	void test_05() {
		String str = "<em>hello</em><em>world</em>";
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(1, result.size(), "一覧の個数");
		StringUtil.StringEmphasis e = result.get(0);
		assertEquals(true, e.isEmphasis(), "協調文字列");
		assertEquals("helloworld", e.getString(), "文字列");
	}

	@Test
	@DisplayName("emタグと通常文字列が混在")
	void test_06() {
		String str = " ラジエーション <em>hello</em>医院長<em>world</em> hogehoge ";
		List<StringUtil.StringEmphasis> result = testClass.separateEmphasis(str);
		assertNotNull(result, "戻り値");
		assertEquals(5, result.size(), "一覧の個数");
		StringUtil.StringEmphasis e = result.get(0);
		assertEquals(false, e.isEmphasis(), "通常文字列");
		assertEquals("ラジエーション ", e.getString(), "文字列");
		e = result.get(1);
		assertEquals(true, e.isEmphasis(), "協調文字列");
		assertEquals("hello", e.getString(), "文字列");
		e = result.get(2);
		assertEquals(false, e.isEmphasis(), "通常文字列");
		assertEquals("医院長", e.getString(), "文字列");
		e = result.get(3);
		assertEquals(true, e.isEmphasis(), "協調文字列");
		assertEquals("world", e.getString(), "文字列");
		e = result.get(4);
		assertEquals(false, e.isEmphasis(), "通常文字列");
		assertEquals(" hogehoge", e.getString(), "文字列");
	}
}