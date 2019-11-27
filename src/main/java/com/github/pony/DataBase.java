package com.github.pony;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class DataBase {

	public static void insertDataBase(String projectName) throws JsonProcessingException, IOException {
		System.out.println("データベースに測定結果の情報を登録します．");
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);

		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("result");

		ObjectMapper mapper = new ObjectMapper();

		JsonNode root = mapper.readTree(new File(Paths.get("github_project", projectName, "result.json").toString()));

		DBObject json = (DBObject) JSON.parse(root.toString());

		coll.insert(json);

	}

	public static String getRecentContent(String ownerName, String repositoryName, String branchName, String key)
			throws UnknownHostException {

		Integer recentId = DataBase.getRecentId(Paths.get(ownerName, repositoryName).toString(), branchName);
		System.out.println(branchName + "の最近のIDは" + recentId + "です");

		List<DBObject> list = new ArrayList<>();

		DataBase.getContents(Paths.get(ownerName, repositoryName).toString(), recentId.toString(), null, list);

		DBObject json = list.get(0);

		BasicDBList evaluationsList = (BasicDBList) json.get("evaluations");
		BasicDBObject evaluations = (BasicDBObject) evaluationsList.get(0);

		return evaluations.get(key).toString();
	}

	public static Integer getRecentId(String projectName, String branchName) throws UnknownHostException {
		Integer id = 0;

		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);

		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("project", projectName);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find(query);

		while (cursor.hasNext()) {
			// 下の2行はいずれか
			// System.out.println("DBCursor - > " + cursor.next());
			// cursor.next();

			DBObject json = cursor.next();

			BasicDBList evaluationsList = (BasicDBList) json.get("evaluations");
			BasicDBObject evaluations = (BasicDBObject) evaluationsList.get(0);

			if (evaluations.get("branch").equals(branchName)) {
				if (id < Integer.parseInt(evaluations.get("id").toString())) {
					id = Integer.parseInt(evaluations.get("id").toString());
				}
			}
		}

		cursor.close();

		return id;
	}

	public static Integer getDataBaseCount(String projectName) throws UnknownHostException {
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);

		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("project", projectName);

		return coll.find(query).count();
	}

	public static void getContents(String projectName, String id, String branchName, List<DBObject> list)
			throws UnknownHostException {
		
		if(id == null) {
			id = "";
		}
		
		if(branchName == null) {
			branchName = "";
		}
		
		
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);

		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("result");

		DBCursor cursor;
		if (projectName == null) {
			cursor = coll.find();
		} else {
			DBObject query = new BasicDBObject("project", projectName);
			cursor = coll.find(query);
		}

		while (cursor.hasNext()) {
			// 下の2行はいずれか
			// System.out.println("DBCursor - > " + cursor.next());
			// cursor.next();

			DBObject json = cursor.next();

			BasicDBList evaluationsList = (BasicDBList) json.get("evaluations");
			BasicDBObject evaluations = (BasicDBObject) evaluationsList.get(0);

			if (id.equals("") && branchName.equals("")) {
				// System.out.println(json.toString());
				list.add(json);
			} else if (!id.equals("")) {
				if (evaluations.get("id").equals(id)) {
					// System.out.println(json.get("evaluations").toString());
					// System.out.println(json.toString());
					list.add(json);
				}
			} else if (evaluations.get("branch").equals(branchName)) {
				// System.out.println(json.toString());
				list.add(json);
			}
		}

		cursor.close();
	}

	public static void insertDataBaseToken(String installationId, String accessToken) throws IOException {

		if (!isToken(installationId)) {
			System.out.println("tokenの登録");
			// MongoDBサーバに接続
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			// 利用するDBを取得
			DB db = mongoClient.getDB("pony");

			DBCollection coll = db.getCollection("token_info");

			coll.createIndex(new BasicDBObject("owner", 1), new BasicDBObject("unique", true));

			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", new BasicDBObject().append("access_token", accessToken));

			BasicDBObject searchQuery = new BasicDBObject().append("installation_id", installationId);

			coll.update(searchQuery, newDocument);

			// カーソルを使って全件取得
			DBCursor cursor = coll.find();
			try {
				while (cursor.hasNext()) {
					// 下の2行はいずれか
					System.out.println("DBCursor - > " + cursor.next());
					// cursor.next();
				}
			} finally {
				cursor.close();
			}
		}

	}

	public static Boolean isToken(String installationId) throws IOException {
		System.out.println("トークンがあるかどうかチェック");
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("token_info");

		DBObject query = new BasicDBObject("owner", installationId);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find(query);

		if (cursor.hasNext()) {
			Map<String, Object> resultMap = (Map<String, Object>) cursor.next();
			return resultMap.containsKey("access_token");
		} else {
			return false;
		}
	}

	public static void insertDataBaseOwner(String installationId, String ownerName) throws IOException {
		System.out.println("ownerの登録");
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("token_info");

		coll.createIndex(new BasicDBObject("owner", 1), new BasicDBObject("unique", true));

		try {
			BasicDBObject doc = new BasicDBObject("installation_id", installationId).append("owner", ownerName);
			coll.insert(doc);
		} catch (com.mongodb.MongoException e) {
			System.out.println("すでに登録されています");
		}

		// カーソルを使って全件取得
		DBCursor cursor = coll.find();
		try {
			while (cursor.hasNext()) {
				// 下の2行はいずれか
				System.out.println("DBCursor - > " + cursor.next());
				// cursor.next();
			}
		} finally {
			cursor.close();
		}
	}

	public static void removeDataBaseOwner(String installationId) throws IOException {
		System.out.println("ownerとトークンの削除");
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("token_info");

		coll.createIndex(new BasicDBObject("owner", 1), new BasicDBObject("unique", true));

		BasicDBObject targetDocument = new BasicDBObject().append("installation_id", installationId);

		coll.remove(targetDocument);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find();
		try {
			while (cursor.hasNext()) {
				// 下の2行はいずれか
				System.out.println("DBCursor - > " + cursor.next());
				// cursor.next();
			}
		} finally {
			cursor.close();
		}
	}

	public static String getDataBaseToken(String ownerName) throws IOException {
		System.out.println("データベースからトークンを取得する");
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("pony");

		DBCollection coll = db.getCollection("token_info");

		DBObject query = new BasicDBObject("owner", ownerName);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find(query);

		if (cursor.hasNext()) {
			Map<String, Object> resultMap = (Map<String, Object>) cursor.next();
			String accessToken = (String) resultMap.get("access_token");
			return accessToken;
		} else {
			return null;
		}
	}
}
