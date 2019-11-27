package com.github.pony;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class GitHubRequest {
	public static void verifyJson(String targetPath, Map<String, Object> inputJsonObject) throws Exception {

		// クラスの取得
		Class<?> c = GitHubRequest.class;

		// インスタンスの生成
		Object myObject = c.newInstance();

		Method method = c.getMethod(getAction(inputJsonObject), Map.class);

		method.invoke(myObject, inputJsonObject);

	}

	public static String getAction(Map<String, Object> inputJsonObject) {
		String actionMethod = "pushedPerform";

		if (inputJsonObject.containsKey("action")) {
			if (inputJsonObject.get("action").equals("created")) {
				actionMethod = "createdPerform";
			}
			if (inputJsonObject.get("action").equals("deleted")) {
				actionMethod = "deletedPerform";
			}
			if (inputJsonObject.get("action").equals("opened")) {
				actionMethod = "openedPerform";
			}
			if (inputJsonObject.get("action").equals("synchronize")) {
				actionMethod = "synchronizePerform";
			}
			if (inputJsonObject.get("action").equals("closed")) {
				actionMethod = "closedPerform";
			}
		}

		return actionMethod;
	}

	/**
	 * GitHubにpushした時に実行するメソッド
	 * 
	 * @param inputJsonObject
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public static void pushedPerform(Map<String, Object> inputJsonObject) throws JsonProcessingException, IOException {
		System.out.println("pushedPerformが呼ばれた．");

		String ownerName = "";
		String repositoryName = "";
		String branchName = "";

		if (inputJsonObject.containsKey("repository")) {
			Map<String, Object> repositoryMap = (Map<String, Object>) inputJsonObject.get("repository");
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
		if (inputJsonObject.containsKey("ref")) {
			branchName = (String) inputJsonObject.get("ref").toString().replaceAll("refs/heads/", "");
		}

		String targetPath = null;
		if (targetPath == null)
			targetPath = ".";

		if (((List<String>) inputJsonObject.get("commits")).size() > 0) {
			if (!ownerName.equals("") && !repositoryName.equals("") && !branchName.equals("")) {
				System.out.println("ownerName: " + ownerName + ", repositoryName: " + repositoryName + ", branchName: "
						+ branchName);
				perform(ownerName, repositoryName, branchName, targetPath);
			} else {
				System.out.println("ownerName,repositoryName,branchName のいずれも入力がありません");
			}
		} else {
			System.out.println("コミットしていないのになぜか要求がきた．バグ対策？");
		}

	}

	/**
	 * プルリクエストクローズ時に実行するメソッド
	 * 
	 * @param inputJsonObject
	 */
	public static void closedPerform(Map<String, Object> inputJsonObject) {
		System.out.println("closedPerformが呼ばれた．");
	}

	/**
	 * プルリクエスト発行時に実行するメソッド
	 * 
	 * @param inputJsonObject
	 * @throws IOException
	 */
	public static void openedPerform(Map<String, Object> inputJsonObject) throws IOException {
		System.out.println("openedPerformが呼ばれた．");

		String ownerName = "";
		String repositoryName = "";
		String branchName = "";
		String sha = "";

		if (inputJsonObject.containsKey("repository")) {
			Map<String, Object> repositoryMap = (Map<String, Object>) inputJsonObject.get("repository");
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

		if (inputJsonObject.containsKey("pull_request")) {
			Map<String, Object> pullRequestMap = (Map<String, Object>) inputJsonObject.get("pull_request");
			if (pullRequestMap.containsKey("head")) {
				Map<String, Object> headMap = (Map<String, Object>) pullRequestMap.get("head");
				branchName = (String) headMap.get("ref");
				sha = (String) headMap.get("sha");
			}
		}

		String targetPath = null;
		if (targetPath == null)
			targetPath = ".";

		try {
			System.out.println("7秒停止します");
			Thread.sleep(7000);
			System.out.println("一時停止を解除しました。");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!ownerName.equals("") && !repositoryName.equals("") && !branchName.equals("") && !sha.equals("")) {
			System.out.println("ownerName: " + ownerName + ", repositoryName: " + repositoryName + ", branchName: "
					+ branchName + ", sha: " + sha);

			String score = DataBase.getRecentContent(ownerName, repositoryName, branchName, "score");
			String state = DataBase.getRecentContent(ownerName, repositoryName, branchName, "state");

			System.out.println("score: " + score + ", state: " + state);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("state", state);
			jsonObject.put("target_url", Paths.get("http://3.113.31.107/pony/", ownerName, repositoryName).toString());

			if (state.equals("failure")) {
				jsonObject.put("description", "violation rate was less than 85%. ");
			}
			jsonObject.put("context", "9rules");

			new Notification().postJson(jsonObject.toString(),
					Paths.get("repos", ownerName, repositoryName, "statuses", sha).toString(),
					DataBase.getDataBaseToken(ownerName));
		} else {
			System.out.println("ownerName,repositoryName,branchName,sha のいずれも入力がありません");
		}
	}

	/**
	 * すでにプルリクエスト発行している（2回目以降の）場合に実行するメソッド
	 * 
	 * @param inputJsonObject
	 * @throws IOException
	 */
	public static void synchronizePerform(Map<String, Object> inputJsonObject) throws IOException {
		System.out.println("synchronizePerformが呼ばれた．");
		openedPerform(inputJsonObject);
	}

	/**
	 * GitHubAPPをインストールした時に実行するメソッド
	 * 
	 * @param inputJsonObject
	 * @throws IOException
	 */
	public static void createdPerform(Map<String, Object> inputJsonObject) throws IOException {
		System.out.println("createdPerformが呼ばれた．");
		if (inputJsonObject.containsKey("installation")) {
			Map<String, Object> installationMap = (Map<String, Object>) inputJsonObject.get("installation");
			java.math.BigDecimal installationId = (java.math.BigDecimal) installationMap.get("id");
			System.out.println("installationId: " + installationId);

			Map<String, Object> senderMap = (Map<String, Object>) inputJsonObject.get("sender");
			String ownerName = (String) senderMap.get("login");
			DataBase.insertDataBaseOwner(installationId.toString(), ownerName);

		}
	}

	/**
	 * GitHubAPPを削除した時に実行するメソッド
	 * 
	 * @param inputJsonObject
	 * @throws IOException
	 */
	public static void deletedPerform(Map<String, Object> inputJsonObject) throws IOException {
		System.out.println("deletedPerformが呼ばれた．");
		if (inputJsonObject.containsKey("installation")) {
			Map<String, Object> installationMap = (Map<String, Object>) inputJsonObject.get("installation");
			java.math.BigDecimal installationId = (java.math.BigDecimal) installationMap.get("id");
			System.out.println("installationId: " + installationId);
			DataBase.removeDataBaseOwner(installationId.toString());
		}
	}

	public static void perform(String ownerName, String repositoryName, String branchName, String targetPath)
			throws JsonProcessingException, IOException {

		String projectName = Paths.get(ownerName, repositoryName).toString();

		String commitId = "2c4a5c8806647c20052d561b20a0003f4a51ef9a";
		// String branchName = "develop11";

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date());

		// GitHubからクローンする
//		new GitHubOperation().perform(projectName, branchName, "");

		// クローンしたデータを実行する
//		new Execution().exec(projectName);

		List<String> executionCommand = new ArrayList<String>(
				Arrays.asList("sh", Paths.get("convert", "convert.sh").toString(), projectName, commitId, branchName,
						String.valueOf(DataBase.getDataBaseCount(projectName) + 1), timeStamp));

		// ponyが扱いやすい形式に変換する
		new Execution().convert(executionCommand);

//		DataBase.insertDataBase(projectName);

//		new Remover().remove(new File(Paths.get("github_project", projectName).toString()));

//		new Web(ownerName, repositoryName).setToWebPage();
//		new Badge(ownerName, repositoryName, branchName).perform();
	}

	public static Boolean isBranch(String ownerName, String repositoryName, String branchName) throws IOException {
		String branchListString = new Notification().getBranchList(ownerName, repositoryName);
		Boolean branchFind = false;

		BasicDBList json = (BasicDBList) JSON.parse(branchListString.toString());

		for (int i = 0; i < json.size(); i++) {
			BasicDBObject studentObj = (BasicDBObject) json.get(i);
			String targetBranch = studentObj.getString("name");
			if (branchName.equals(targetBranch)) {
				branchFind = true;
			}
		}
		return branchFind;
	}

}
