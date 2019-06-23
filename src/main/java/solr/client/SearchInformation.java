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
