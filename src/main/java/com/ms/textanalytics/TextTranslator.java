package com.ms.textanalytics;

import java.io.*;
import java.net.*;
import java.util.*;
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

/**
 * This class is used to translate a list of texts from one language to another. Once the
 * data is translated the translated output is stored in COSMOS DB for
 * persistent storage.
 * 
 * @author raiy
 *
 */

public class TextTranslator {

	// Replace the subscriptionKey string value with your valid subscription key.
	static String subscriptionKey = "Enter valid key";
	// Validate the URL
	static String host = "https://api.cognitive.microsofttranslator.com";
	static String path = "/translate?api-version=3.0";
	// Replace user name
	static String userName="RK";
	
	// Translate to German and Italian.
	static String params = "&to=de&to=it";
	static String textToBeTranslated = "Hello world!";

	/**
	 * Class for represent request for translator API
	 * 
	 * @author raiy
	 *
	 */
	public static class RequestBody {
		String Text;

		public RequestBody(String text) {
			this.Text = text;
		}
	}

	/**
	 * This method is used to POST a request to Translator Text API.
	 * 
	 * @param url
	 *            url to be passed
	 * @param content
	 *            json content of request
	 * @return response from Translator Text API.
	 * @throws Exception
	 */
	public static String Post(URL url, String content) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Content-Length", content.length() + "");
		connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
		connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
		connection.setDoOutput(true);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		byte[] encoded_content = content.getBytes("UTF-8");
		wr.write(encoded_content, 0, encoded_content.length);
		wr.flush();
		wr.close();

		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		String line;
		while ((line = in.readLine()) != null) {
			response.append(line);
		}
		in.close();

		return response.toString();
	}

	/**
	 * This method creates a JSON to be posted to Translator Text API.
	 * 
	 * @return Return response from Translator Text API.
	 * @throws Exception
	 *             Exception in case of any issue.
	 */
	public static String translateTexts(List<RequestBody> objList) throws Exception {
		URL url = new URL(host + path + params);
		
		String content = new Gson().toJson(objList);
		System.out.println("Text to be translated : " + content);
		return Post(url, content);
	}

	/**
	 * This method is used to pretty print the JSON response from Translator Text
	 * API.
	 * 
	 * @param json_text
	 *            Json to be pretty printed
	 * @return Pretty printed JSON
	 */
	public static String prettify(String json_text) {
		JsonParser parser = new JsonParser();
		JsonElement json = parser.parse(json_text);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(json);
	}

	/**
	 * This method is used to parse the response from Translator Text API & store
	 * the same in COSMOS DB.
	 * 
	 * @param json_text
	 *            The response JSON to be stored in COSMOS DB.
	 */
	public static void parseResponseandPersist(String json_text,List<RequestBody> objList) {
		// Replce COSMOS URL and Key
		DocumentClient client = new DocumentClient("{URL}",
				"{Key}",
				new ConnectionPolicy(), ConsistencyLevel.Session);
		
		// create a document in the collection
		String collectionLink = String.format("/dbs/%s/colls/%s", "cognitiveservicesdb", "TextTranslationOutput");
	      
		  JsonElement jelement = new JsonParser().parse(json_text); 
		  JsonArray jarray =jelement.getAsJsonArray(); 
		  int i=0;
		  while (i< jarray.size()) {
			  TextTranslationOutput objTextTranslationOutput = new TextTranslationOutput();
		      objTextTranslationOutput.setId(objTextTranslationOutput.getId());
		      objTextTranslationOutput.setUser(userName);
		      objTextTranslationOutput.setInputtext(objList.get(i).Text);
		      
			  JsonObject jobject =jarray.get(i).getAsJsonObject(); 
			  JsonArray jarraytranslations = jobject.getAsJsonArray("translations");
			  
			  int j=0;
			  while (j< jarraytranslations.size()) {
				  JsonObject jinnerobject =jarraytranslations.get(j).getAsJsonObject(); 
				  
				  if(jinnerobject.get("to").getAsString().equals("de")) {
					  String result = jinnerobject.get("text").getAsString();
					  objTextTranslationOutput.setTranslatedtext_german(result);
				  }else if(jinnerobject.get("to").getAsString().equals("it")){
					  String result = jinnerobject.get("text").getAsString();
					  objTextTranslationOutput.setTranslatedtext_italian(result);
				  }
				  j++;
			  }
			  i++;
			  
			  try {
					client.createDocument(collectionLink, objTextTranslationOutput, new RequestOptions(), true);
				} catch (DocumentClientException e) {
					System.out.println("Issue while creating document in Cosmos DB" + e);
				}
	}
	}

	public static void main(String[] args) {
		try {
			// List of text to be translated
			List<RequestBody> objList = new ArrayList<RequestBody>();
			objList.add(new RequestBody(textToBeTranslated));
			objList.add(new RequestBody("How are you!!!"));
			
			// Translate text using Translator Text API
			String response = translateTexts(objList);
			
			// Print the response on console
			System.out.println(prettify(response));
			
			// Persist data in Cosmos DB
			parseResponseandPersist(response,objList);
			
			//System.out.println(prettify(response));
		} catch (Exception e) {
			System.out.println("There is a problem during translation of text and storing in COSMOS DB" + e);
		}
	}
}
