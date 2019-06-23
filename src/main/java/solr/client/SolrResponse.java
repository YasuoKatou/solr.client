package solr.client;

public interface SolrResponse {

	/**
	 * 検索結果を処理するメソッド
	 * @param json 検索結果文字列
	 */
	void executeResoponse(SearchInformation information);
}