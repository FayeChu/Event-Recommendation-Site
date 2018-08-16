package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	// Write a JSONArray to HTTP response
	public static void writeJSONArray(HttpServletResponse response, JSONArray array) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		out.print(array);
		out.close();			
	}
	
	// Write a JSONObject to HTTP response
	public static void writeJSONObject(HttpServletResponse response, JSONObject obj) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		out.print(obj);
		out.close();			
	}
	
	// Parse a JSONObject from HTTP request
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JSONObject();
	}

}
