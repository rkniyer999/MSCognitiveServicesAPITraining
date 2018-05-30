package com.ms.computervision;

//This sample uses the following libraries:
//- Apache HTTP client(org.apache.httpcomponents:httpclient:4.5.5)
//- Apache HTTP core(org.apache.httpcomponents:httpccore:4.4.9)
//- JSON library (org.json:json:20180130).

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ImageAnalysis {
	// **********************************************
	// *** Update or verify the following values. ***
	// **********************************************

	// Replace <Subscription Key> & <URI Base> with your valid subscription key.
	private static final String subscriptionKey = "valid subscription key";
	private static final String uriBase = "https://southeastasia.api.cognitive.microsoft.com/vision/v2.0/analyze";

	// Provide path of Blob
	private static final String imageToAnalyze = "Image URL";

	public static void main(String[] args) {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		System.out.println(imageToAnalyze);
		try {
			URIBuilder builder = new URIBuilder(uriBase);

			// Request parameters. All of them are optional.
			builder.setParameter("visualFeatures", "Categories,Description,Color");
			builder.setParameter("language", "en");

			// Prepare the URI for the REST API call.
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);

			// Request headers.
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			// Request body.
			StringEntity requestEntity = new StringEntity("{\"url\":\"" + imageToAnalyze + "\"}");
			request.setEntity(requestEntity);

			// Make the REST API call and get the response entity.
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				long startTime = System.currentTimeMillis();
				// Format and display the JSON response.
				String jsonString = EntityUtils.toString(entity);
				JSONObject json = new JSONObject(jsonString);
				System.out.println("REST Response:\n");
				System.out.println(json.toString(2));
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println(totalTime);
			}
		} catch (Exception e) {
			// Display error message.
			System.out.println(e.getMessage());
		}
	}
}
