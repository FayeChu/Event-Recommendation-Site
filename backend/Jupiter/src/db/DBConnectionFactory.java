package db;

import db.mysql.MySQLConnection;

public class DBConnectionFactory {
	// This should change based on the pipeline.
		private static final String DEFAULT_DB = "mysql";
		
		public static DBConnection getConnection(String db) {
			switch (db) {
			case "mysql":
				// Implemented DBConnection Interface
				
				return new MySQLConnection();
			case "mongodb":
				// Implemented DBConnection Interface
				// return new MongoDBConnection();
				return null;
			default:
				throw new IllegalArgumentException("Invalid db:" + db);
			}

		}

		public static DBConnection getConnection() {
			return getConnection(DEFAULT_DB);
		}
}
