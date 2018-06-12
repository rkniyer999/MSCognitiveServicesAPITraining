package com.ms.textanalytics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.RequestOptions;
import com.ms.textanalytics.TextTranslator.RequestBody;

/**
 * This class is the template used to send documents to Text Analytics API
 */
class Document {
	public String id, language, text;

	public Document(String id, String language, String text) {
		this.id = id;
		this.language = language;
		this.text = text;
	}
}

/**
 * This class is consists of list of documents template.
 */
class Documents {
	public List<Document> documents;

	public Documents() {
		this.documents = new ArrayList<Document>();
	}

	public void add(String id, String language, String text) {
		this.documents.add(new Document(id, language, text));
	}
}

/**
 * This class is used to perform sentiment analysis and turn unstructured text
 * into meaningful insights. The output is stored in COSMOS DB for persistent
 * storage.
 * 
 * @author raiy
 *
 */
public class SentimentAnalysis {

	// Replace the accessKey string value with your valid access key.
	static String accessKey = "Enter valid Access Key";

	// Replace the region.
	static String host = "https://{region}.api.cognitive.microsoft.com";
	static String path = "/text/analytics/v2.0/sentiment";
	
	// Replace the user name
	static String userName = "";

	/**
	 * 
	 * @param documents
	 * @return
	 * @throws Exception
	 */
	public static String GetSentiment(Documents documents) throws Exception {
		String text = new Gson().toJson(documents);
		System.out.println(text);
		byte[] encoded_text = text.getBytes("UTF-8");

		URL url = new URL(host + path);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "text/json");
		connection.setRequestProperty("Ocp-Apim-Subscription-Key", accessKey);
		connection.setDoOutput(true);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.write(encoded_text, 0, encoded_text.length);
		wr.flush();
		wr.close();

		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			response.append(line);
		}
		in.close();

		return response.toString();
	}

	/**
	 * This method is used to pretty print the JSON text. API.
	 * 
	 * @param json_text
	 *            Json to be pretty printed
	 * @return Pretty printed JSON
	 */
	public static String prettify(String json_text) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(json_text).getAsJsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		return gson.toJson(json);
	}

	/**
	 * This method is used to parse the response from Text Analytics API & store
	 * the same in COSMOS DB.
	 * 
	 * @param json_text
	 *            The response JSON to be stored in COSMOS DB.
	 */

	public static void parseResponseandPersist(String response, Documents objDocuments) {

		DocumentClient client = new DocumentClient("cosmosURL",
				"Cosmos Key",
				new ConnectionPolicy(), ConsistencyLevel.Session);

		// create a document in the collection
		String collectionLink = String.format("/dbs/%s/colls/%s", "cognitiveservicesdb", "SentimentAnalysisOutput");

		JsonElement jelement = new JsonParser().parse(response);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray jarrayDocuments = jobject.getAsJsonArray("documents");
		int i = 0;
		while (i < jarrayDocuments.size()) {

			SentimentAnalysisOutput objSentimentAnalysisOutput = new SentimentAnalysisOutput();
			objSentimentAnalysisOutput.setUserName(userName);
			objSentimentAnalysisOutput.setId(objDocuments.documents.get(i).id);
			objSentimentAnalysisOutput.setInputtext(objDocuments.documents.get(i).text);
			objSentimentAnalysisOutput.setLanguage(objDocuments.documents.get(i).language);

			JsonObject jInnerobject = jarrayDocuments.get(i).getAsJsonObject();
			objSentimentAnalysisOutput.setSentimentScore(jInnerobject.get("score").getAsFloat());

			if (jInnerobject.get("score").getAsFloat() * 100 >= 60) {
				objSentimentAnalysisOutput.setSentimentType("Positive");
			} else {
				objSentimentAnalysisOutput.setSentimentType("Negative");
			}

			try {
				client.createDocument(collectionLink, objSentimentAnalysisOutput, new RequestOptions(), true);
			} catch (DocumentClientException e) {
				System.out.println("Issue while creating document in Cosmos DB" + e);
			}
			i++;
		}
	}

	public static void main(String[] args) {
		try {
			Documents documents = new Documents();
			documents.add("1", "en",
					"I really enjoy the new XBox One S. It has a clean look, it has 4K/HDR resolution and it is affordable.");
			documents.add("2", "es",
					"Este ha sido un dia terrible, llegué tarde al trabajo debido a un accidente automobilistico.");
			documents.add("3", "en", "I really hate this product.");

			String response = GetSentiment(documents);

			System.out.println(prettify(response));

			parseResponseandPersist(response, documents);

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
