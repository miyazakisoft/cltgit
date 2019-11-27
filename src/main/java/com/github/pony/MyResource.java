package com.github.pony;

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
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.io.IOException;

@Path("/")
public class MyResource {
	@GET
	@Path("test1/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test1Get() throws IOException {

		GitHubRequest.perform("miyazakisoft", "Putu", "develop15", "");

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("test2/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test2Get() throws IOException {

		new Remover().remove(new File(Paths.get("github_project", "miyazakisoft/Putu").toString()));

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("test3/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test3Get() throws IOException {
		String token = DataBase.getDataBaseToken("miyazakisoft");

		System.out.println("token: " + token);

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("test4/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test4Get() throws IOException {

		new Web("miyazakisoft", "Putu").setToWebPage();

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("test5/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test5Get() throws IOException {


		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@POST
	@Path("restore/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response restore() throws IOException {

		List<String> projectNameList = new ArrayList<String>();

		List<DBObject> list = new ArrayList<DBObject>();
		DataBase.getContents(null, null, null, list);

		for (DBObject o : list) {
			if (!projectNameList.contains(o.get("project"))) {
				projectNameList.add(o.get("project").toString());
			}
		}

		for (String projectName : projectNameList) {
			String ownerName = projectName.split("/")[0];
			String repositoryName = projectName.split("/")[1];

			new Web(ownerName, repositoryName).setToWebPage();
			new Badge(ownerName, repositoryName, "master").perform();
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	@Path("9rules/{owner}/{repository}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getContents(@PathParam("owner") String ownerName, @PathParam("repository") String repositoryName,
			@QueryParam(value = "branch") String branchName, @QueryParam(value = "target") String targetPath,
			@QueryParam(value = "id") String id) throws Exception {

		System.out.println("GET: " + Paths.get("9rules", ownerName, repositoryName).toString());
		System.out.println("id: " + id + ", branchName: " + branchName + ", targetPath: " + targetPath);

		String projectName = Paths.get(ownerName, repositoryName).toString();
		List<DBObject> list = new ArrayList<>();

		if (id == null) {
			id = "";
		}
		if (branchName == null) {
			branchName = "";
		}

		DataBase.getContents(projectName, id, branchName, list);

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
	@Path("/9rules")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response verify(@QueryParam(value = "branch") String branchName,
			@QueryParam(value = "target") String targetPath, Map<String, Object> inputJsonObj) throws Exception {

		System.out.println("アクセスがありました");
		if (inputJsonObj != null) {
			GitHubRequest.verifyJson(targetPath, inputJsonObj);
		} else {
			System.out.println("Jsonフォーマットではありませんでした．");
			// verify(ownerName, repositoryName, branchName, targetPath);
		}

		Map<String, String> map = new HashMap<>();
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
			accessToken = new Notification().getAccessToken(code, "XXXXX",
					"YYYYY");
		}

		System.out.println("accessToken: " + accessToken);

		try {
			System.out.println("5秒停止します");
			Thread.sleep(5000);
			System.out.println("一時停止を解除しました。");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		DataBase.insertDataBaseToken(installationId, accessToken);

		Map<String, String> map = new HashMap<>();
		map.put("ok", "true");
		return Response.status(200).entity(map).header("Access-Control-Allow-Origin", "*").build();
	}

}
