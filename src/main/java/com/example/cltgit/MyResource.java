package com.example.cltgit;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.cltgit.ninerules.Json;
import com.example.cltgit.ninerules.NineRulesChecker;
import com.example.cltgit.ninerules.tool.Utility;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.cltgit.ninerules.Json;

import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.io.IOException;

//import java.io.tempdirSystem;

//import org.bson.Document;

@Path("/")
public class MyResource {

	@GET
	@Path("test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test() throws IOException {
		
		
		
		Map<String, Object> map = new HashMap<>();
		map.put("ok", "false");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("list/{account}/{repository}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("account") String accountName, @PathParam("repository") String repositoryName)
			throws Exception {
		List<DBObject> list = new ArrayList<>();

		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("repository", accountName + "/" + repositoryName);

		System.out.println("list");

		// カーソルを使って全件取得
		DBCursor cursor = coll.find(query);
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

	@POST
	@Path("/validators/{account}/{repository}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verify(@PathParam("account") String accountName, @PathParam("repository") String repositoryName,
			@QueryParam(value = "branch") String branchName, @QueryParam(value = "target") String targetPath)
			throws Exception {

		System.out.println("PathParam account = " + accountName + ", repository = " + repositoryName);
		System.out.println("QueryParam branch = " + branchName + ", targetPath = " + targetPath);

		if (branchName == null)
			branchName = "master";
		if (targetPath == null)
			targetPath = "";

		new GitOperation().gitClone(accountName, repositoryName);

		List<Map<String, Object>> fileContentList = new ArrayList<Map<String, Object>>();

		Integer violationCount = nineRulesRun(accountName, repositoryName, new Json(fileContentList), targetPath);

		insertDataBase(accountName, repositoryName, violationCount, fileContentList, targetPath);

		String currentPath = new File(".").getAbsoluteFile().getParent();
		System.out.println("currentPath= " + currentPath);

		String webBaseDirectory = getWebBaseDirectory();
		System.out.println("webBaseDirectory= " + webBaseDirectory);

		setPersonalDirectory(currentPath, webBaseDirectory, accountName, repositoryName);
		
		setToWebPage(currentPath, webBaseDirectory, violationCount, accountName, repositoryName);

		Map<String, String> map = new HashMap<>();
		map.put("ok", "true");

		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	Integer nineRulesRun(String accountName, String repositoryName, Json json, String targetPath) throws Exception {
		File dir = new File("./git_project/" + accountName + "/" + repositoryName + targetPath);
		Integer violationCount = 0;

		List<File> fileList = new ArrayList<File>();
		Utility.readAllFolder(dir, fileList);

		for (File aCheckFile : fileList) {
			List<String> resultList = new NineRulesChecker().performs(this.getFilePathArray(aCheckFile.getPath()));
			for (String s : resultList) {
				System.out.print(s + ", ");
			}
			System.out.println("");

			this.removeFilePath(resultList);
			System.out.println("違反数:" + resultList.size());
			violationCount += resultList.size();
			json.create(resultList, aCheckFile.getPath().replace(dir.getPath(), ""));
		}
		System.out.println("違反合計:" + violationCount);

		deleteDirectory("./git_project/" + accountName + "/" + repositoryName);
		return violationCount;
	}

	void insertDataBase(String accountName, String repositoryName, Integer violationCount,
			List<Map<String, Object>> fileContentList, String targetPath) throws UnknownHostException {
		// MongoDBサーバに接続
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 利用するDBを取得
		DB db = mongoClient.getDB("9rules");

		DBCollection coll = db.getCollection("result");

		DBObject query = new BasicDBObject("repository", accountName + "/" + repositoryName);
		BasicDBObject doc = new BasicDBObject("repository", accountName + "/" + repositoryName)
				.append("number", coll.find(query).count() + 1).append("date", "ISODate(" + new Date() + ")")
				.append("target_path", targetPath).append("violation_count", violationCount)
				.append("concrete", fileContentList);
		coll.insert(doc);

		// カーソルを使って全件取得
		DBCursor cursor = coll.find();
		try {
			while (cursor.hasNext()) {
				System.out.println("DBCursor - > " + cursor.next());
			}
		} finally {
			cursor.close();
		}
	}

	void setPersonalDirectory(String currentPath, String webBaseDirectory, String accountName, String repositoryName) {
		File file = new File(currentPath + "/" + webBaseDirectory + "/webapp/" + accountName + "/" + repositoryName);
		file.mkdirs();
	}

	void setToWebPage(String currentPath, String webBaseDirectory, Integer violationCount, String accountName,
			String repositoryName) throws IOException {
		String setFilePath = currentPath + "/" + webBaseDirectory + "/webapp/" + accountName + "/" + repositoryName;

		Utility.fileMove(currentPath + "/view_file/webpage/index.html", setFilePath + "/index.html");
		Utility.fileMove(currentPath + "/view_file/webpage/jquery.json2html.js", setFilePath + "/jquery.json2html.js");
		Utility.fileMove(currentPath + "/view_file/webpage/json2html.js", setFilePath + "/json2html.js");
		Utility.fileMove(currentPath + "/view_file/webpage/myscript.js", setFilePath + "/myscript.js");
		Utility.fileMove(currentPath + "/view_file/webpage/style.css", setFilePath + "/style.css");
		setToBadge(currentPath, webBaseDirectory, violationCount, accountName, repositoryName);
	}

	void setToBadge(String currentPath, String webBaseDirectory, Integer violationCount, String accountName,
			String repositoryName) throws IOException {
		String setToBadgePath = currentPath + "/" + webBaseDirectory + "/webapp/" + accountName + "/" + repositoryName
				+ "/badge.svg";
		
		new DownloadAndSave().perform(violationCount,setToBadgePath);
		
		
		//if (violationCount == 0) {
		//	Utility.fileMove(currentPath + "/view_file/badge/1.png", setToBadgePath);
		//} else {
		//	Utility.fileMove(currentPath + "/view_file/badge/2.png", setToBadgePath);
		//}
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