package solr.client;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import solr.util.StringUtil;

public class SolrSearcherController implements Initializable, SolrResponse {
	/** 検索文字列を入力するエリア */
	@FXML private TextField txtSearchWords;
	/** 検索実行ボタン */
	@FXML private Button btnSearch;
	@FXML private ListView<MyListItem> lstSearchResult;
	@FXML private Label lblProcessStatus;
	@FXML private Label lblSearchStatus;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.lstSearchResult.setCellFactory((ListView<MyListItem> l) -> new ListCellEx());
	}

	private static StringUtil stringUtil = new StringUtil();

	/**
	 * 検索ボタン押下処理
	 * @param event
	 */
	@FXML public void onSearchCliked(ActionEvent event) {
//		System.out.println("start onSearchCliked : " + Thread.currentThread().getName());
		this.executeSearch();
	}

	@FXML public void txtSearchWords_keyPress(KeyEvent e) {
//		System.out.println("start txtSearchWords_keyPress : " + e.toString());
		if (e.getCode() == KeyCode.ENTER) {
			this.executeSearch();
		}
	}

	private void executeSearch() {
		String word = stringUtil.doubleQuoteString(this.txtSearchWords.getText());
		System.out.println("start onSearchCliked : " + word);
		this.lblProcessStatus.setText("検索中．．．");
		this.lblSearchStatus.setText("");
		try {
			// 一覧をクリア
			this.lstSearchResult.getItems().removeAll();
			// 検索
//			SolrCommunication.doGET(word, this);
//			SolrCommunication2.doGET(word, this);		Java9で標準のHttpClientが動作しなかった対応
			SearchInformation info = SearchInformation.builder().searchWord(word).resp(this).build();
			info.startTimer();
			SolrCommunication comm = SolrCommunication.builder().searchInformation(info).build();
			comm.start();
//			System.out.println("execute search by server");
		} catch (Exception e) {
			System.out.println(word);
			e.printStackTrace();
		}
	}

	@FXML public void lstSearchResult_onMouseClick(MouseEvent e) {
//		System.out.println("start lstSearchResult_onMouseClick : " + e.getClickCount());
		if (e.getClickCount() != 2) return;
		int itemIndex = lstSearchResult.getSelectionModel().getSelectedIndex();
		if (itemIndex < 0) return;

		String path = lstSearchResult.getSelectionModel().getSelectedItem().getResourcename();
//		System.out.println("start lstSearchResult_onMouseClick : " + path);
		File file = new File(path);
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(file);
		} catch (IOException e1) {
			System.out.println("can't open : " + path);
		}
	}

	/**
	 * 検索結果を表示する
	 */
	@Override
	public void executeResoponse(SearchInformation information) {
//		System.out.println("resoponse received : " + Thread.currentThread().getName());
//		System.out.println(json);
		ObjectMapper mapper = new ObjectMapper();
		String json = information.getResponseJson();
		try {
			Map<String,Object> map = mapper.readValue(json, new TypeReference<HashMap>(){});
			Map<String,Object> responseHeader = (Map<String,Object>)map.get("responseHeader");
			Map<String,Object> response = (Map<String,Object>)map.get("response");
			List<Map<String,Object>> docs = (List<Map<String,Object>>) response.get("docs");
			Map<String,Object> highlighting = (Map<String,Object>) map.get("highlighting");
			List<MyListItem> items = new ArrayList<>();
			for (Map<String,Object> rec : docs) {
				items.add(new MyListItem(rec, highlighting));
			}
			this.lstSearchResult.setItems(FXCollections.observableArrayList(items));
			information.stopTimer();
			this.lblProcessStatus.setText("処理時間 : " + information.getProcessTime() + " ms");
			this.lblSearchStatus.setText("検索時間 : " + responseHeader.get("QTime").toString() + " ms");
		} catch (Exception e) {
			System.out.println(json);
			e.printStackTrace();
		}
	}

	/**
	 * 一覧の一行を表示する
	 */
	public static class ListCellEx extends ListCell<MyListItem> {
		private VBox cellContainer;
		private TextFlow txtHiglight;
		private Label lblPath;
		private final static Text txtEmpty = new Text("");

		public ListCellEx() {
			super();
			this.cellContainer = new VBox(3);
			this.txtHiglight = new TextFlow();
			this.txtHiglight.setTextAlignment(TextAlignment.JUSTIFY);
			this.lblPath = new Label();
// TODO 右詰めにしたいが、コンポーネントの幅がVBoxの幅にならない
//			this.lblPath.setTextAlignment(TextAlignment.RIGHT);
//			this.lblPath.setMaxWidth(Double.MAX_VALUE);
			this.cellContainer.getChildren().addAll(this.txtHiglight, this.lblPath);
		}
		@Override
		protected void updateItem(MyListItem item, boolean empty) {
			super.updateItem(item, empty);

//			this.txtHiglight.getChildren().clear();
			ObservableList<Node> ch = this.txtHiglight.getChildren();
			ch.clear();
			if (empty || item == null) {
				this.txtHiglight.getChildren().addAll(txtEmpty);
			} else {
				item.getHighlight().forEach(e -> {
					Text text = new Text(e.getString());
					if (e.isEmphasis()) {
						text.getStyleClass().add("string-emphasis");
					}
					ch.add(text);
				});
//				this.txtHiglight.setText(item.toString());
//				this.txtHiglight.wrappingWidthProperty().bind(getListView().widthProperty().subtract(25));
				this.txtHiglight.prefWidthProperty().bind(getListView().widthProperty().subtract(25));
				this.lblPath.setText(item.getResourcename());
			}
			setGraphic(this.cellContainer);
//			setGraphic(this.txtHiglight);
		}
	}

	/**
	 * 検索結果の一覧に表示する情報を保持する
	 */
	public static class MyListItem {
		/** 一覧に表示する文字列 */
		private String id;
		@Getter
		private String resourcename;
		@Getter
		private List<StringUtil.StringEmphasis> highlight;
		/**
		 * コンストラクタ
		 * @param value 検索結果の項目1
		 */
		public MyListItem(String value) {
			this.highlight = stringUtil.separateEmphasis(value);
		}
		public MyListItem(Map<String,Object> doc, Map<String,Object> highlighting) {
			// id
			this.id = (String)doc.get("id");
			// ファイルのパス
			List<Object> list = (List)doc.get("attr_resourcename");
			if (CollectionUtils.isEmpty(list)) {
				this.resourcename = null;
			} else {
				this.resourcename = (String)(list).get(0);
			}
			// ハイライト
			Map<String,Object> content = (Map<String,Object>) highlighting.get(id);
			list = (List)content.get("content");
			if (CollectionUtils.isEmpty(list)) {
				this.highlight = stringUtil.separateEmphasis("");;
			} else {
				String str = (String)(list).get(0);
				str = str.replaceAll("[\\t\\r\\n] ", "");
				str = str.replaceAll("[\\s　]+", " ");
				this.highlight = stringUtil.separateEmphasis(str);
			}
		}
	}
}