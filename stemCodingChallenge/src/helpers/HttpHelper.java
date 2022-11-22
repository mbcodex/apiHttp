package helpers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpHelper {
	private String requestUrl = "";
	private String responseContents="";
	private HttpURLConnection cHttpConnection=null;

	//This class makes the http connection and gets the response
	public HttpHelper(String url) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		requestUrl = url;
		getConnection();
	}

	public HttpURLConnection getConnection()
			throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException {
		URL url = new URL(requestUrl);
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		cHttpConnection=con;
		return con;
	}


	public String getResponse() {
		try {
			Reader reader = getInputReader(cHttpConnection);
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

}


