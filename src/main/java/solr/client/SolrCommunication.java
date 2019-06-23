package solr.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.commons.codec.net.URLCodec;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Builder;

@Builder
public class SolrCommunication extends Service<String> {
	private SearchInformation searchInformation;
//	private String searchWord;
//	private SolrResponse resp;

	@Builder
	private static class CommTask extends Task<String> {
		private SearchInformation searchInformation;
//		@Setter private String searchWord;
//		@Setter private SolrResponse resp;

		@Override
		protected String call() throws Exception {
//			System.out.println("start call at Task : " + Thread.currentThread().getName());
			URLCodec codec = new URLCodec("UTF-8");
			String encodeWord = codec.encode(this.searchInformation.getSearchWord(), "UTF-8");
			String searcher = "http://localhost:8983/solr/files/select?fl=id%2Cattr_resourcename&hl=on&hl.fl=content&q=" + encodeWord;
			System.out.println("GET : " + searcher);
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(searcher))
					.GET()
					.build();

			HttpClient httpClient = HttpClient.newBuilder()
					.build();

			final HttpResponse<String> response = httpClient.send(
					request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

//			System.out.println("http response : " + response.statusCode());
			this.searchInformation.setStatusCode(response.statusCode());
			this.searchInformation.setResponseJson(response.body());
			Platform.runLater( () -> this.searchInformation.getResp().executeResoponse(this.searchInformation));
			return response.body();
		}
		
	}

	@Override
	protected Task<String> createTask() {
//		System.out.println("start createTask : " + Thread.currentThread().getName());
		CommTask task = CommTask.builder().searchInformation(this.searchInformation).build();
		return task;
	}
}