package com.example.cltgit.ninerules.tool;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * 便利なツールをメソッドで定義しているクラスである.
 * @author Miyazaki
 * @since 2017/07/25
 */


public class Utility {

	
	public static void fileMove(String sourcePath,String targetPath) {
		try {
			//Files.move(Paths.get(sourcePath), Paths.get(targetPath));
			Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void readFolder(String dir,List<String> filePathList) {
		Path path = Paths.get(dir);
		try( DirectoryStream<Path> ds = Files.newDirectoryStream(path) ){
			for ( Path p : ds ) {
				filePathList.add(p.toString());
			}
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
	
	
	
    /**
      * ディレクトリを再帰的に読む
      * @param folderPath
      */
     public static void readAllFolder(File dir,List<File> filePathList) {
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File file : files) {
            if(!file.exists()){
                continue;
            }else if(file.isDirectory()){
                if(!file.getName().equals("__MACOSX")){
                    readAllFolder(file,filePathList);
                }
            }else if(file.isFile()){
                if(getSuffix(file.getName()).equals("java")){
                  filePathList.add(file);
                }
            }
        }
     }

    /**
      * ファイル名から拡張子を返します。
      * @param fileName ファイル名
      * @return ファイルの拡張子
    */
    public static String getSuffix(String fileName) {
        if (fileName == null){
            return null;
        }
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(point + 1);
        }
        return fileName;
    }

    /**
     * ファイル名から拡張子を取り除いた名前を返します。
     * @param fileName ファイル名
     * @return ファイル名
     */
    public static String getPreffix(String fileName) {
        if (fileName == null)
            return null;
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(0, point);
        }
        return fileName;
    }

    public static void removeDirectory(File file){
        File[] fileArray = file.listFiles();
        for(int i=0; i<fileArray.length; i++){
            if(!fileArray[i].delete()){
                removeDirectory(fileArray[i]);
            }
        }
        file.delete();
    }
}
