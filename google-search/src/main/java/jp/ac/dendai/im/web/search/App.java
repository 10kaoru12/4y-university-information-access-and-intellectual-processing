package jp.ac.dendai.im.web.search;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;

/** フィード(RSS) の全 item 要素 の description 要素の内容を Sudachi で形態素解析 */

public class App {
	public static void main(String[] args) {
		String urlString = "https://rss.itmedia.co.jp/rss/2.0/itmedia_all.xml";	// 解析対象のRSS (ITmedia 総合記事一覧)
		try {
			// フィード(RSS)に接続
			URLConnection connection = new URL(urlString).openConnection();
			connection.connect();
			// DOMツリーの構築
			Document document = buildDocument(connection.getInputStream(), "utf-8");
			// XPath の表現を扱う XPath オブジェクトを生成
			XPath xPath = XPathFactory.newInstance().newXPath();
			// 各item要素のdescription要素のリストを得る
			NodeList descriptionList = (NodeList)xPath.evaluate("//item/description",
					document, XPathConstants.NODESET);

			// 形態素解析器の用意
			Dictionary dictionary = null;
			try {
				// 設定ファイル sudachi.json を用意した場合にはそれを読み込む
				//dictionary = new DictionaryFactory().create(Files.readString(Paths.get("sudachi.json")));
				dictionary = new DictionaryFactory().create();
			}
			catch(Exception e) {
				System.err.println("辞書が読み込めません: " + e);
				System.exit(-1);
			}
			Tokenizer tokenizer = dictionary.create();

			// 各description要素を形態素解析
			for(int i = 0; i < descriptionList.getLength(); i++) {
				// description要素の内容をプレーンテキストとして得る
				Node descriptionNode= descriptionList.item(i);
				String descriptionString = descriptionNode.getTextContent().replaceAll("<.+?>", "");	// タグを削除
				System.out.println("Text: " + descriptionString);
				System.out.println();
				// 形態素解析
				for(List<Morpheme> list: tokenizer.tokenizeSentences(Tokenizer.SplitMode.C, descriptionString)) {
					for(Morpheme morpheme: list) {
						System.out.println(morpheme.surface() + "\t" // 表層形
								+ String.join("-", morpheme.partOfSpeech()) + "," // 品詞
								+ morpheme.dictionaryForm() + "," // 原形
								+ morpheme.readingForm() + "," // 読み
								+ morpheme.normalizedForm()); // 正規形
					}
					System.out.println("EOS");	// 文の終端 (End of Sentence)
				}
				System.out.println();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** DOM Tree の構築 */
	public static Document buildDocument(InputStream inputStream, String encoding) {
		Document document = null;
		try {
			// DOM実装(implementation)の用意 (Load and Save用)
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS implementation = (DOMImplementationLS)registry.getDOMImplementation("XML 1.0");
			// 読み込み対象の用意
			LSInput input = implementation.createLSInput();
			input.setByteStream(inputStream);
			input.setEncoding(encoding);
			// 構文解析器(parser)の用意
			LSParser parser = implementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			parser.getDomConfig().setParameter("namespaces", false);
			// DOMの構築
			document = parser.parse(input);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}
}