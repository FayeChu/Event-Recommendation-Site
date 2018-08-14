package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY= "JRfZQGvNL3GBjrJnWknalJCXTGxvoKHV";
	
	private static final String EMBEDDED = "_embedded";
	private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STR = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";
	
	public List<Item> search(double lat, double lon, String keyword) {
		// Encode keyword in URl since it may contain special characters
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Convert lat/lon to geo hash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		// Make URL "apikey=12345&geoPaint=abcd&keyword=music&radius=50"
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", 
				API_KEY, geoHash, keyword, 50);
		
		try {
			// Open a HTTP connection between java application and TicketMaster based URl
			HttpURLConnection connection = 
					(HttpURLConnection) new URL(URL + "?" + query).openConnection();
			
			// Set request method
			connection.setRequestMethod("GET");
			
			// Send request to TicketMaster and get response, response code could be returned directly
			// response body is saved in inputStream of connection
			int responseCode = connection.getResponseCode();
			
			System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Reponse code: " + responseCode);
			
			// Now read response body to get events data
			StringBuilder response = new StringBuilder();
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}
			
			JSONObject obj = new JSONObject(response.toString());
			if (obj.isNull("_embedded")) {
				return new ArrayList<>();
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			
			return getItemList(events);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<>();
	}
	
	/**
	 * Helper methods
	 */

	//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull(EMBEDDED)) {
			JSONObject embedded = event.getJSONObject(EMBEDDED);
			
			if (!embedded.isNull(VENUES)) {
				JSONArray venues = embedded.getJSONArray(VENUES);
				
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder sBuilder = new StringBuilder();
					
					if (!venue.isNull(ADDRESS)) {
						JSONObject address = venue.getJSONObject(ADDRESS);
						
						if (!address.isNull(LINE1)) {
							sBuilder.append(address.getString(LINE1));
						}
						if (!address.isNull(LINE2)) {
							sBuilder.append("\n");
							sBuilder.append(address.getString(LINE2));
						}
						if (!address.isNull(LINE3)) {
							sBuilder.append("\n");
							sBuilder.append(address.getString(LINE3));
						}
					}
					
					if (!venue.isNull(CITY)) {
						JSONObject city = venue.getJSONObject(CITY);
						
						if (!city.isNull(NAME)) {
							sBuilder.append("\n");
							sBuilder.append(city.getString(NAME));
						}
					}
					
					String addr = sBuilder.toString();
					if (addr.length() > 0) {
						return addr;
					}
				}
			}
		}
		return "";
	}
	
	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageURL(JSONObject event) throws JSONException {
		if (!event.isNull(IMAGES)) {
			JSONArray images = event.getJSONArray(IMAGES);
			
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				
				if (!image.isNull(URL_STR)) {
					return image.getString(URL_STR);
				}
			}
		}
		
		return "";
	}
	
	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		
		// HashSet stores unique names
		Set<String> categories = new HashSet<>();
		
		if (!event.isNull(CLASSIFICATIONS)) {
			JSONArray classfications = event.getJSONArray(CLASSIFICATIONS);
			
			for (int i = 0; i < classfications.length(); i++) {
				JSONObject classfication = classfications.getJSONObject(i);
				
				
				if (!classfication.isNull(SEGMENT)) {
					JSONObject segment = classfication.getJSONObject(SEGMENT);
					
					if (!segment.isNull(NAME)) {
						String name = segment.getString(NAME);
						if (!name.isEmpty()) {
							categories.add(name);
						}
					}
				}
			}
		}
		
		return categories;
	}
	
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		
		// use itemBuilder to build Item
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull(NAME)) {
				builder.setName(event.getString(NAME));
			}
			
			if (!event.isNull(ID)) {
				builder.setItemId(event.getString(ID));
			}
			
			if (!event.isNull(RATING)) {
				builder.setRating(event.getDouble(RATING));
			}
			
			if (!event.isNull(URL_STR)) {
				builder.setUrl(event.getString(URL_STR));
			}
			
			if (!event.isNull(DISTANCE)) {
				builder.setDistance(event.getDouble(DISTANCE));
			}
			
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageURL(event));
			
			itemList.add(builder.build());
		}
		
		return itemList;
	}
	
	public void queryAPI(double lat, double lon) {
		List<Item> itemList = search(lat, lon, null);
		try {
			for (Item item : itemList) {
				JSONObject object = item.toJSONObject();
				System.out.println(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Main entry for sample TicketMaster API requests
	 */
	
	public static void main(String[] args) {
//		TicketMasterAPI tmApi = new TicketMasterAPI();
//		tmApi.queryAPI(29.682684, -95.295410);
	}

}
