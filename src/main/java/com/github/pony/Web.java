package com.github.pony;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Web {

	String ownerName;
	String repositoryName;
	String systemName = "pony";

	Web(String ownerName, String repositoryName) {
		this.ownerName = ownerName;
		this.repositoryName = repositoryName;
	}

	void setToWebPage() throws IOException {
		setPersonalDirectory();
		fileMove(currentPath() + "/view_file/webpage/index.html", targetPath() + "/index.html");
		fileMove(currentPath() + "/view_file/webpage/jquery.json2html.js", targetPath() + "/jquery.json2html.js");
		fileMove(currentPath() + "/view_file/webpage/json2html.js", targetPath() + "/json2html.js");
		fileMove(currentPath() + "/view_file/webpage/myscript.js", targetPath() + "/myscript.js");
		fileMove(currentPath() + "/view_file/webpage/style.css", targetPath() + "/style.css");
		fileMove(currentPath() + "/view_file/webpage/9rules.png", targetPath() + "/9rules.png");
		// setToBadge(currentPath, webBaseDirectory, totalNumberOfViolations,
		// lineOfCode, ownerName, repositoryName);
	}

	void setPersonalDirectory() {
		File file = new File(
				Paths.get(currentPath(), webBaseDirectory(), "webapp", ownerName, repositoryName).toString());
		file.mkdirs();
	}

	String currentPath() {
		return new File(".").getAbsoluteFile().getParent();
	}

	String targetPath() {
		return Paths.get(currentPath(), webBaseDirectory(), "webapp", ownerName, repositoryName).toString();
	}

	String webBaseDirectory() {
		List<String> fList = new ArrayList<String>();
		readFolder("./temp", fList);
		String webBaseDirectory = "";
		for (String aWebFile : fList) {
			if (aWebFile.indexOf("jetty-0.0.0.0-8080-" + systemName + ".war") != -1) {
				webBaseDirectory = aWebFile.substring(2, aWebFile.length());
				break;
			}
		}
		return webBaseDirectory;
	}

	public static void readFolder(String dir, List<String> filePathList) {
		Path path = Paths.get(dir);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
			for (Path p : ds) {
				filePathList.add(p.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void fileMove(String sourcePath, String targetPath) {
		try {
			// Files.move(Paths.get(sourcePath), Paths.get(targetPath));
			Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
