package solr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Getter;

public class StringUtil {

	/**
	 * 強調文字列と通常文字列を分離した結果を格納する
	 * @since 2019/6/19
	 */
	@Builder
	public static class StringEmphasis {
		public static final boolean EMPHASIS = true;
		public static final boolean NONE_EMPHASIS = false;
		/** 強調表示する場合、true */
		@Getter private final boolean emphasis;
		@Getter private String string;
		public StringEmphasis(boolean emphasis, String string) {
			this.emphasis = emphasis;
			this.string = string;
		}
		public String addString(String str) {
			this.string = this.string + str;
			return this.string;
		}
	}

	/**
	 * 指定の文字列から強調文字列を抽出する.
	 * @param str 抽出対象の文字列
	 * @return
	 * @since 2019/6/19
	 */
	public List<StringEmphasis> separateEmphasis(String str) {
		List<StringEmphasis> list = new ArrayList<>();
		if (str == null) return list;
		if (str.trim().length() == 0) return list;

		String ramaining = new String(str.trim());
		Pattern p = Pattern.compile("(\\<em\\>)(.*?)(\\<\\/em\\>)");
		Matcher m = p.matcher(ramaining);
		while (m.find()) {
			// 強調文字列の左側
			if (m.start() > 0) {
				list.add(StringEmphasis.builder()
						.emphasis(StringEmphasis.NONE_EMPHASIS)
						.string(ramaining.substring(0, m.start()))
						.build());
			}
			// 強調表示する文字列
			list.add(StringEmphasis.builder()
					.emphasis(StringEmphasis.EMPHASIS)
					.string(m.group(2))
					.build());
			// 協調文字列の右側
			ramaining = ramaining.substring(m.end());
			m = p.matcher(ramaining);
		}
		if (ramaining.trim().length() > 0) {
			list.add(StringEmphasis.builder()
					.emphasis(StringEmphasis.NONE_EMPHASIS)
					.string(ramaining)
					.build());
		}
		return this.joinContinue(list);
	}

	/**
	 * 連続する強調文字列を結合する.
	 * @param source
	 * @return 結合結果
	 */
	private List<StringEmphasis> joinContinue(List<StringEmphasis> source) {
		int num = source.size();
		if (num < 2) return source;
		List<StringEmphasis> result = new ArrayList<>();
		StringEmphasis e = source.get(0);
		result.add(e);
		for (int idx = 1; idx < num; ++idx) {
			StringEmphasis w = source.get(idx);
			if (e.isEmphasis() == w.isEmphasis()) {
				e.addString(w.getString());
			} else {
				e = w;
				result.add(e);
			}
		}
		return result;
	}

	/**
	 * 英語は単語で検索できるが、日本語は部分一致となる.
	 * 従って、単語にダブルクォートで囲む
	 * @param str 検索文字
	 * @return 単語ごとにダブルクォートで囲んだ結果
	 */
	public String doubleQuoteString(String str) {
		return "\"" + str.replaceAll("[\\s　]+", "\" \"") + "\"";
	}
}