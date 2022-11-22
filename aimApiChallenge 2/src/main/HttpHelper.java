package main;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import org.json.simple.JSONObject;

public class HttpHelper {
	private String reportUrl = "";
	private String responseContents="";
	private String cRequestType="";
	private HttpsURLConnection cHttpsConnection=null;
	private String postSku="";
	private String postDescription="";
	private String postPrice="";


	public HttpHelper(String url, String requestType) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		reportUrl = url;
		cRequestType=requestType;
		getConnection();
	}

	public HttpHelper(String url, String requestType, String sku, String description, String price) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		reportUrl = url;
		cRequestType=requestType;
		postSku=sku;
		postDescription=description;
		postPrice=price;
		getConnection();
	}

	public HttpsURLConnection getConnection()
			throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException {
		/*
		 * SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, getTrustingTrustManager(), new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(getAllTrustingHostVerifier());
		 */

		URL url = new URL(reportUrl);
		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) url.openConnection();
			switch(cRequestType) {
			case "GET":
				con.setRequestMethod("GET");
				break;
			case "DELETE":
				con.setRequestMethod("DELETE");
				break;
			case "POST":
				String json=buildJsonString();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("Accept", "application/json");

				try(OutputStream os = con.getOutputStream()) {
					byte[] input = json.getBytes("utf-8");
					os.write(input, 0, input.length);			
				}

				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					System.out.println(response.toString());
				}
				//int status = con.getResponseCode();
		        con.connect();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		cHttpsConnection=con;
		return con;
	}
	private String buildJsonString() {
		
		  String json="{\"sku\":\""
		  +postSku
		  +"\","
		  +"\"description\": \""
		  +postDescription
		  +"\","
		  + "\"price\":\""
		  +postPrice
		  +"\"}";
		return json;
		 
	}

	// Create all-trusting host name verifier
	private HostnameVerifier getAllTrustingHostVerifier() {
		return new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
	}

	/*
	 * fix for Exception in thread "main" javax.net.ssl.SSLHandshakeException:
	 * sun.security.validator.ValidatorException: PKIX path building failed:
	 * sun.security.provider.certpath.SunCertPathBuilderException: unable to find
	 * valid certification path to requested target
	 */
	private TrustManager[] getTrustingTrustManager() {
		return new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };
	}

	public void setResponseContents(String jsonString) {
		responseContents = jsonString;
	}

	public int getResponseCode() {
		int responseCode=-1;
		try {
			responseCode=cHttpsConnection.getResponseCode();

		} catch (Exception e) {
		}
		return responseCode;
	}

	public String getResponse() {
		try {
			Reader reader = getInputReader(cHttpsConnection);
			StringBuilder s = readAll(reader);
			responseContents = s.toString();
			closeReader(reader);
		} catch (Exception e) {
			responseContents = "";
		}
		return responseContents;
	}

	public StringBuilder readAll(Reader reader) {
		StringBuilder s = new StringBuilder();
		while (true) {
			int ch = 0;
			try {
				ch = reader.read();
				if (ch != -1) {
					s.append((char) ch);
				}
			} catch (IOException e) {
				e.printStackTrace();
				closeReader(reader);
				throw new RuntimeException(e);
			}
			// We don't want the end of file character
			if (ch == -1) {
				break;
			}
		}
		return s;
	}

	public void closeReader(Reader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Reader getInputReader(URLConnection con) {
		Reader reader = null;
		try {
			reader = new InputStreamReader(con.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return reader;
	}

	public String getResponseContents() {
		return responseContents;
	}

}


