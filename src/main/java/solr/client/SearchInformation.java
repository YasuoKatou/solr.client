package solr.client;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SearchInformation {
	/** 検索文字列. */
	private String searchWord;
	/** 検索結果を処理するクラス. */
	private SolrResponse resp;
	/** 検索結果文字列 */
	private String responseJson;
	/** レスポンスステータスコード. */
	private int statusCode;
	/** 検索結果開始インデックス. */
	private int start;
	/** 検索結果最大取得件数. */
	private int rows;

	private long startTime;
	private long endTime;

	public void startTimer() {
		this.startTime = System.currentTimeMillis();
	}

	public void stopTimer() {
		this.endTime = System.currentTimeMillis();
	}

	public String getProcessTime() {
		return (this.endTime - this.startTime) + "";
	}
}