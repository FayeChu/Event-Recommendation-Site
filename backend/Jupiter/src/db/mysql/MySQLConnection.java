package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.xerces.internal.dom.PSVIDOMImplementationImpl;

import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	
	private Connection conn;
	
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// Call tickerMasterAPI to get events data
		TicketMasterAPI ticketMasterAPI = new TicketMasterAPI();
		List<Item> items = ticketMasterAPI.search(lat, lon, term);
		
		// save to database
		for (Item item : items) {
			saveItem(item);
		}
		
		// return to front end
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// Error handling
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		// sql injection security problem
		// can not use string.format in sql statment
		
	   	// select * from users where username = '' AND password = '';
	   	// username: fakeuser ' OR 1 = 1; DROP  -- // -- ignore 后面语句
	   	// select * from users where username = 'fakeuser ' OR 1 = 1 --' AND password = '';

		try {
			// IGNORE 去重 
			// Use preparedStatement to avoid sql injection
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getImageUrl());
			ps.setString(6, item.getUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();
			
			// Category
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for (String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

}
