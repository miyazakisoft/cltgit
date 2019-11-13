package com.example.cltgit;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.example.cltgit.ninerules.Json;
import com.example.cltgit.ninerules.NineRulesChecker;
import com.example.cltgit.ninerules.tool.Utility;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.example.cltgit.ninerules.Json;

import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.io.IOException;

@Path("/")
public class MyResource {

	public String getDataBaseToken(String ownerName) throws IOException {
		System.out.println("データベースからトークンを取得する");
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

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

	void removeDataBaseOwner(String installationId) throws IOException {
		System.out.println("ownerとトークンの削除");
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

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

	public Boolean isToken(String installationId) throws IOException {
		System.out.println("トークンがあるかどうかチェック");
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

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

	void insertDataBaseToken(String installationId, String accessToken) throws IOException {

		if (!isToken(installationId)) {
			System.out.println("tokenの登録");
			// MongoDBサーバに接続
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			// 利用するDBを取得
			DB db = mongoClient.getDB("9rules");

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

	void insertDataBaseOwner(String installationId, String ownerName) throws IOException {
		System.out.println("ownerの登録");
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

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
				// System.out.println("DBCursor - > " + cursor.next());
				// cursor.next();
			}
		} finally {
			cursor.close();
		}
	}

	@GET
	@Path("test1/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test1Get() throws IOException {

		String token = getDataBaseToken("miyazakisoft");

		System.out.println("access token: " + token);

		// new
		// Notification().callVarnish("http://3.113.31.107/cltgit/miyazakisoft/Putu/badge.svg");

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@POST
	@Path("restore/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response restore() throws IOException {

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		// DBObject query = new BasicDBObject("owner", ownerName).append("repository",
		// repositoryName);

		BasicDBObject sort = new BasicDBObject();
		sort.put("date", -1);

		Map<String, List<String>> ownerMap = new HashMap<String, List<String>>();

		// カーソルを使って全件取得
		DBCursor cursor = coll.find();
		try {
			while (cursor.hasNext()) {
				// System.out.println("DBCursor - > " + cursor.next());
				Map<String, Object> resultMap = (Map<String, Object>) cursor.next();
				String ownerName = (String) resultMap.get("owner");
				String repositoryName = (String) resultMap.get("repository");

				if (!ownerMap.containsKey(ownerName)) {
					List<String> repositoryList = new ArrayList<String>();
					repositoryList.add(repositoryName);
					ownerMap.put(ownerName, repositoryList);
				} else {
					List<String> repositoryList = ownerMap.get(ownerName);
					if (!repositoryList.contains(repositoryName)) {
						repositoryList.add(repositoryName);
					}
				}
			}
		} finally {
			cursor.close();
		}

		String currentPath = new File(".").getAbsoluteFile().getParent();
		System.out.println("currentPath= " + currentPath);

		String webBaseDirectory = getWebBaseDirectory();
		System.out.println("webBaseDirectory= " + webBaseDirectory);

		for (String ownerName : ownerMap.keySet()) {
			for (String repositoryName : ownerMap.get(ownerName)) {

				Integer correntNumber = getDataBaseCount(ownerName, repositoryName, null, null);

				System.out.println("correntNumber: " + correntNumber);

				DBCursor cursor2 = getDataBase(ownerName, repositoryName, "master", null, correntNumber);

				Map<String, Object> map = new HashMap<>();
				try {
					while (cursor2.hasNext()) {
						map = (Map<String, Object>) cursor2.next();
						// System.out.println("DBCursor - > " + cursor.next());
					}
				} finally {
					cursor2.close();
				}

				Integer totalNumberOfViolations = (Integer) map.get("total_number_of_violations");
				Long lineOfCode = (Long) map.get("line_of_code");
				setPersonalDirectory(currentPath, webBaseDirectory, ownerName, repositoryName);
				setToWebPage(currentPath, webBaseDirectory, totalNumberOfViolations, lineOfCode, ownerName,
						repositoryName);
			}
		}

		System.out.println(ownerMap);

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("authenticate/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authenticate(@QueryParam(value = "code") String code,
			@QueryParam(value = "installation_id") String installationId,
			@QueryParam(value = "setup_action") String setupAction) throws Exception {

		System.out.println("リダイレクトされました");
		System.out.println("code: " + code);
		System.out.println("installation_id: " + installationId);
		System.out.println("setup_action: " + setupAction);

		String accessToken = "";
		if (setupAction.equals("install")) {
			
			accessToken = new Notification().getAccessToken(code, "XXXXXXX","YYYYYY");
			
		}

		System.out.println("accessToken: " + accessToken);

		try {
			System.out.println("5秒停止します");
			Thread.sleep(5000);
			System.out.println("一時停止を解除しました。");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		insertDataBaseToken(installationId, accessToken);

		Map<String, String> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	public Double getRateOfChange(Double currentRate, String ownerName, String repositoryName, String branchName)
			throws IOException {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("owner", ownerName).append("repository", repositoryName);

		query.put("branch", branchName);

		BasicDBObject sort = new BasicDBObject();
		sort.put("date", -1);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find(query).sort(sort).limit(1);

		if (cursor.hasNext()) {
			Map<String, Object> resultMap = (Map<String, Object>) cursor.next();

			String previousRate = (String) resultMap.get("compliance_rate");

			return currentRate - Double.parseDouble(previousRate);

		} else {
			return null;
		}

	}

	public Response verifyJson(String targetPath, Map<String, Object> inputJsonObj) throws Exception {

		String ownerName = "";
		String repositoryName = "";
		String sha = "";

		String branchName = "";

		if (inputJsonObj.containsKey("action")) {
			if (inputJsonObj.get("action").equals("created")) {
				if (inputJsonObj.containsKey("installation")) {
					Map<String, Object> installationMap = (Map<String, Object>) inputJsonObj.get("installation");
					java.math.BigDecimal installationId = (java.math.BigDecimal) installationMap.get("id");
					System.out.println("installationId: " + installationId);

					Map<String, Object> senderMap = (Map<String, Object>) inputJsonObj.get("sender");
					ownerName = (String) senderMap.get("login");
					insertDataBaseOwner(installationId.toString(), ownerName);

				}
			} else if (inputJsonObj.get("action").equals("deleted")) {
				if (inputJsonObj.containsKey("installation")) {
					Map<String, Object> installationMap = (Map<String, Object>) inputJsonObj.get("installation");
					java.math.BigDecimal installationId = (java.math.BigDecimal) installationMap.get("id");
					System.out.println("installationId: " + installationId);
					removeDataBaseOwner(installationId.toString());
				}
			}
		}

		if (inputJsonObj.containsKey("pull_request")) {
			Map<String, Object> pullRequestMap = (Map<String, Object>) inputJsonObj.get("pull_request");
			if (pullRequestMap.containsKey("head")) {
				Map<String, Object> headMap = (Map<String, Object>) pullRequestMap.get("head");
				branchName = (String) headMap.get("ref");
				sha = (String) headMap.get("sha");
			}
		}

		if (inputJsonObj.containsKey("commits")) {
			if (inputJsonObj.containsKey("ref")) {
				branchName = (String) inputJsonObj.get("ref").toString().replaceAll("refs/heads/", "");
			}
		}

		if (inputJsonObj.containsKey("repository")) {
			Map<String, Object> repositoryMap = (Map<String, Object>) inputJsonObj.get("repository");
			if (repositoryMap.containsKey("owner")) {
				Map<String, Object> ownerMap = (Map<String, Object>) repositoryMap.get("owner");
				if (ownerMap.containsKey("login")) {
					ownerName = (String) ownerMap.get("login");
				}
			}
			if (repositoryMap.containsKey("name")) {
				repositoryName = (String) repositoryMap.get("name");
			}
		}

		if (targetPath == null)
			targetPath = ".";

		if (inputJsonObj.containsKey("commits")) {
			if (((List<String>) inputJsonObj.get("commits")).size() > 0) {

				if (ownerName != "" && repositoryName != "" && branchName != "") {
					verify(ownerName, repositoryName, branchName, targetPath);

				} else {
					System.out.println("ownerName: " + ownerName + ", repositoryName: " + repositoryName
							+ ", branchName: " + branchName);
				}
			}
			System.out.println("List Size = " + ((List<String>) inputJsonObj.get("commits")).size());
		}
		if (inputJsonObj.containsKey("pull_request")) {
			if (!inputJsonObj.get("action").equals("closed")) {
				try {
					System.out.println("5秒停止します");
					Thread.sleep(5000);
					System.out.println("一時停止を解除しました。");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Integer correntNumber = getDataBaseCount(ownerName, repositoryName, null, null);

				System.out.println("correntNumber: " + correntNumber);

				DBCursor cursor = getDataBase(ownerName, repositoryName, branchName, null, correntNumber);

				Map<String, Object> map = new HashMap<>();
				try {
					while (cursor.hasNext()) {
						map = (Map<String, Object>) cursor.next();
						// System.out.println("DBCursor - > " + cursor.next());
					}
				} finally {
					cursor.close();
				}

				Integer totalNumberOfViolations = (Integer) map.get("total_number_of_violations");
				Long lineOfCode = (Long) map.get("line_of_code");

				Double complianceRate;
				if (totalNumberOfViolations == 0) {
					complianceRate = 100.0;
				} else {
					complianceRate = (100 - (((double) totalNumberOfViolations / lineOfCode) * 100));
				}

				System.out.println("complianceRate: " + complianceRate);

				JSONObject jsonObject = new JSONObject();
				if (complianceRate < 85.0) {
					jsonObject.put("state", "failure");
					jsonObject.put("target_url",
							"http://3.113.31.107/cltgit/" + ownerName + "/" + repositoryName + "/");
					jsonObject.put("description", "Compliance rate is less than 85%. Compliance rate = "
							+ String.format("%.1f", complianceRate) + "%.");
					jsonObject.put("context", "9rules");// Compliance rate is less than 85%.
				} else {
					jsonObject.put("state", "success");
					jsonObject.put("target_url",
							"http://3.113.31.107/cltgit/" + ownerName + "/" + repositoryName + "/");
					jsonObject.put("description", "Compliance rate is over 85%. Compliance rate = "
							+ String.format("%.1f", complianceRate) + "%.");
					jsonObject.put("context", "9rules");
				}
				new Notification().postJson(jsonObject.toString(), "repos/miyazakisoft/Putu/statuses/" + sha,
						getDataBaseToken(ownerName));
			}
		}

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("ok", "false");
		return Response.status(200).entity(responseMap).header("Access-Control-Allow-Origin", "*").build();
	}

	public Response verify(@PathParam("owner") String ownerName, @PathParam("repository") String repositoryName,
			@QueryParam(value = "branch") String branchName, @QueryParam(value = "target") String targetPath)
			throws Exception {

		System.out.println("PathParam owner = " + ownerName + ", repository = " + repositoryName);
		System.out.println("QueryParam branch = " + branchName + ", targetPath = " + targetPath);

		if (branchName == null)
			branchName = "master";

		if (targetPath == null)
			targetPath = ".";

		Random rnd = new Random();

		// 現在日時の取得
		Date d = new Date();

		// 書式の作成
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

		String randamDirectory = sdf.format(d) + "-" + rnd.nextInt();

		// 指定書式に変換して表示
		System.out.println(sdf.format(d) + "-" + rnd.nextInt());

		new GitOperation().perform(ownerName, repositoryName, branchName, randamDirectory);

		List<Map<String, Object>> fileContentList = new ArrayList<Map<String, Object>>();

		File dir = new File("./git_project/" + randamDirectory + "/" + ownerName + "/" + repositoryName, targetPath);

		nineRulesRun(dir, new Json(fileContentList));

		Integer totalNumberOfViolations = getTotalNumberOfViolations(fileContentList);
		Long lineOfCode = getLineOfCode(dir, fileContentList);
		Integer numberOfVerificationFiles = getNumberOfVerifications(fileContentList);

		System.out.println("numberOfVerificationFiles: " + numberOfVerificationFiles);

		deleteDirectory("./git_project/" + randamDirectory);

		insertDataBaseResult(ownerName, repositoryName, branchName, numberOfVerificationFiles, totalNumberOfViolations,
				lineOfCode, fileContentList, targetPath);

		String currentPath = new File(".").getAbsoluteFile().getParent();
		System.out.println("currentPath= " + currentPath);

		String webBaseDirectory = getWebBaseDirectory();
		System.out.println("webBaseDirectory= " + webBaseDirectory);

		setPersonalDirectory(currentPath, webBaseDirectory, ownerName, repositoryName);

		setToWebPage(currentPath, webBaseDirectory, totalNumberOfViolations, lineOfCode, ownerName, repositoryName);

		return null;
	}

	@POST
	// @Path("/9rules/{owner}/{repository}")
	@Path("/9rules")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response entryVerify(@QueryParam(value = "branch") String branchName,
			@QueryParam(value = "target") String targetPath, Map<String, Object> inputJsonObj) throws Exception {
		// public Response entryVerify(@PathParam("owner") String ownerName,
		// @PathParam("repository") String repositoryName,
		// @QueryParam(value = "branch") String branchName, @QueryParam(value =
		// "target") String targetPath,
		// Map<String, Object> inputJsonObj) throws Exception {

		System.out.println("アクセスがありました");
		if (inputJsonObj != null) {
			verifyJson(targetPath, inputJsonObj);
		} else {
			System.out.println("Jsonフォーマットではありませんでした．");
			// verify(ownerName, repositoryName, branchName, targetPath);
		}

		Map<String, String> map = new HashMap<>();
		map.put("ok", "true");

		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("test2/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test2() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("ok", "false");

		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	Integer getDataBaseCount(String ownerName, String repositoryName, String branchName, String targetPath)
			throws UnknownHostException {
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("owner", ownerName).append("repository", repositoryName);

		if (branchName != null) {
			query.put("branch", branchName);
		}

		if (targetPath != null) {
			// query.put("targetPath", targetPath);
		}

		return coll.find(query).count();
	}

	DBCursor getDataBase(String ownerName, String repositoryName, String branchName, String targetPath, Integer id)
			throws UnknownHostException {
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("owner", ownerName).append("repository", repositoryName);

		if (id != null) {
			query.put("id", id);
		}

		if (branchName != null) {
			query.put("branch", branchName);
		}

		BasicDBObject sort = new BasicDBObject();
		sort.put("id", -1);

		// カーソルを使って全件取得
		return coll.find(query).sort(sort);
	}

	@GET
	@Path("9rules/{owner}/{repository}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResult(@PathParam("owner") String ownerName, @PathParam("repository") String repositoryName,
			@QueryParam(value = "branch") String branchName, @QueryParam(value = "target") String targetPath,
			@QueryParam(value = "id") Integer id) throws Exception {
		List<DBObject> list = new ArrayList<>();

		DBCursor cursor = getDataBase(ownerName, repositoryName, branchName, targetPath, id);

		try {
			while (cursor.hasNext()) {
				list.add(cursor.next());
				// System.out.println("DBCursor - > " + cursor.next());
			}
		} finally {
			cursor.close();
		}

		Map<String, Object> map = new HashMap<>();
		if (list.size() == 0) {
			map.put("ok", "false");
		} else {
			map.put("ok", "true");
			map.put("info", list);
		}

		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	void nineRulesRun(File dir, Json json) throws Exception {

		List<File> fileList = new ArrayList<File>();
		Utility.readAllFolder(dir, fileList);

		System.out.println("dir path: " + dir.getPath());

		for (File aCheckFile : fileList) {
			List<String> resultList = new NineRulesChecker().performs(this.getFilePathArray(aCheckFile.getPath()));
			json.create(resultList, aCheckFile.getPath().replace(dir.getPath(), ""));
		}
	}

	Integer getNumberOfVerifications(List<Map<String, Object>> fileContentList) {

		return fileContentList.size();
	}

	Long getLineOfCode(File dir, List<Map<String, Object>> fileContentList) {
		Long lineOfCode = new Long(0);
		for (Map<String, Object> fileContentMap : fileContentList) {
			String path = (String) fileContentMap.get("path");
			lineOfCode = lineOfCode + new LOC().CountNumberOfTextLines(dir.getPath() + path);
		}
		return lineOfCode;
	}

	Integer getTotalNumberOfViolations(List<Map<String, Object>> fileContentList) {

		Integer totalNumberOfViolations = new Integer(0);
		for (Map<String, Object> fileContentMap : fileContentList) {

			Map<String, Object> violationContentMap = (Map<String, Object>) fileContentMap.get("violations");

			Set<String> keys = violationContentMap.keySet();
			for (int i = 0; i < keys.size(); i++) {
				String violationType = keys.toArray(new String[0])[i];
				List<Map<String, Object>> violationTypeList = (List<Map<String, Object>>) violationContentMap
						.get(violationType);
				totalNumberOfViolations += violationTypeList.size();
			}
		}
		return totalNumberOfViolations;
	}

	Double getComplianceRate(Integer totalNumberOfViolations, Long lineOfCode) {
		Double complianceRate;
		if (totalNumberOfViolations == 0) {
			complianceRate = 100.0;
		} else {
			complianceRate = (100 - (((double) totalNumberOfViolations / lineOfCode) * 100));
		}
		return complianceRate;
	}

	String getComplianceRank(Integer totalNumberOfViolations, Long lineOfCode) {
		Double complianceRate = getComplianceRate(totalNumberOfViolations, lineOfCode);
		String rank = "F";
		if (complianceRate == 100.0) {
			rank = "A";
		} else if (complianceRate >= 98.0) {

			rank = "B";
		} else if (complianceRate >= 95.0) {

			rank = "C";
		} else if (complianceRate >= 90.0) {

			rank = "D";
		} else if (complianceRate >= 85.0) {
			rank = "E";
		}
		return rank;
	}

	void insertDataBaseResult(String ownerName, String repositoryName, String branchName,
			Integer numberOfVerificationFiles, Integer totalNumberOfViolations, Long lineOfCode,
			List<Map<String, Object>> fileContentList, String targetPath) throws IOException {
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		coll.createIndex(new BasicDBObject("id", 1).append("owner", 1).append("repository", 1),
				new BasicDBObject("unique", true));

		DBObject query = new BasicDBObject("owner", ownerName).append("repository", repositoryName);
		Integer id = coll.find(query).count() + 1;

		// DBObject query2 = new BasicDBObject("owner", ownerName).append("repository",
		// repositoryName);

		Double rateOfChange = getRateOfChange(getComplianceRate(totalNumberOfViolations, lineOfCode), ownerName,
				repositoryName, branchName);

		Boolean loop = true;
		while (loop) {
			try {
				BasicDBObject doc = new BasicDBObject("owner", ownerName).append("repository", repositoryName)
						.append("branch", branchName).append("id", id).append("date", new Date())
						.append("target_path", targetPath)
						.append("compliance_rank", getComplianceRank(totalNumberOfViolations, lineOfCode))
						.append("compliance_rate",
								String.format("%.1f", getComplianceRate(totalNumberOfViolations, lineOfCode)))
						.append("number_of_verification_files", numberOfVerificationFiles)
						.append("total_number_of_violations", totalNumberOfViolations)
						.append("line_of_code", lineOfCode).append("concrete", fileContentList);
				if (rateOfChange != null) {
					doc.put("rate_of_change", String.format("%.1f", rateOfChange));
				}
				coll.insert(doc);

				loop = false;
			} catch (com.mongodb.MongoException e) {
				System.out.println("重複エラー id = " + id);
				id++;
			}
		}

		// カーソルを使って全件取得
		DBCursor cursor = coll.find();
		try {
			while (cursor.hasNext()) {
				// System.out.println("DBCursor - > " + cursor.next());
				cursor.next();
			}
		} finally {
			cursor.close();
		}
	}

	void setPersonalDirectory(String currentPath, String webBaseDirectory, String ownerName, String repositoryName) {
		File file = new File(currentPath + "/" + webBaseDirectory + "/webapp/" + ownerName + "/" + repositoryName);
		file.mkdirs();
	}

	void setToWebPage(String currentPath, String webBaseDirectory, Integer totalNumberOfViolations, Long lineOfCode,
			String ownerName, String repositoryName) throws IOException {
		String setFilePath = currentPath + "/" + webBaseDirectory + "/webapp/" + ownerName + "/" + repositoryName;

		// System.out.println("setFilePath: " + setFilePath);
		// System.out.println("currentPath: " + currentPath);

		Utility.fileMove(currentPath + "/view_file/webpage/index.html", setFilePath + "/index.html");
		Utility.fileMove(currentPath + "/view_file/webpage/jquery.json2html.js", setFilePath + "/jquery.json2html.js");
		Utility.fileMove(currentPath + "/view_file/webpage/json2html.js", setFilePath + "/json2html.js");
		Utility.fileMove(currentPath + "/view_file/webpage/myscript.js", setFilePath + "/myscript.js");
		Utility.fileMove(currentPath + "/view_file/webpage/style.css", setFilePath + "/style.css");
		Utility.fileMove(currentPath + "/view_file/webpage/9rules.png", setFilePath + "/9rules.png");
		setToBadge(currentPath, webBaseDirectory, totalNumberOfViolations, lineOfCode, ownerName, repositoryName);
	}

	void setToBadge(String currentPath, String webBaseDirectory, Integer totalNumberOfViolations, Long lineOfCode,
			String ownerName, String repositoryName) throws IOException {
		String setToBadgePath = currentPath + "/" + webBaseDirectory + "/webapp/" + ownerName + "/" + repositoryName
				+ "/badge.svg";

		new DownloadAndSave().perform(totalNumberOfViolations, lineOfCode, setToBadgePath);
	}

	String getWebBaseDirectory() {
		List<String> fList = new ArrayList<String>();
		Utility.readFolder("./temp", fList);
		String webBaseDirectory = "";
		for (String aWebFile : fList) {
			if (aWebFile.indexOf("jetty-0.0.0.0-8080-cltgit.war") != -1) {
				webBaseDirectory = aWebFile.substring(2, aWebFile.length());
				break;
			}
		}
		return webBaseDirectory;
	}

	public void removeFilePath(List<String> resultList) {
		resultList.remove(0);
	}

	/**
	 * 対象パスのディレクトリの削除を行う.<BR>
	 * ディレクトリ配下のファイル等が存在する場合は<BR>
	 * 配下のファイルをすべて削除します.
	 *
	 * @param dirPath 削除対象ディレクトリパス
	 * @throws Exception
	 */
	public static void deleteDirectory(final String dirPath) throws Exception {
		File file = new File(dirPath);
		recursiveDeleteFile(file);
	}

	/**
	 * 対象のファイルオブジェクトの削除を行う.<BR>
	 * ディレクトリの場合は再帰処理を行い、削除する。
	 *
	 * @param file ファイルオブジェクト
	 * @throws Exception
	 */
	private static void recursiveDeleteFile(final File file) throws Exception {
		// 存在しない場合は処理終了
		if (!file.exists()) {
			return;
		}
		// 対象がディレクトリの場合は再帰処理
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				recursiveDeleteFile(child);
			}
		}
		// 対象がファイルもしくは配下が空のディレクトリの場合は削除する
		file.delete();
	}

	public String[] getFilePathArray(String pathName) {
		String[] targetPathArray = new String[1];
		targetPathArray[0] = pathName;
		return targetPathArray;
	}

}