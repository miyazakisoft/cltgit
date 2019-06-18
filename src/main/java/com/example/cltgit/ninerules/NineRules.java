package com.example.cltgit.ninerules;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.IOException;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.cltgit.ninerules.tool.Utility;

/**
 * 9rulesをするプログラムである.
 * @author Miyazaki
 * @since 2017/07/25
 */

@Path("/validators/")
public class NineRules {

	
	
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String,Object>> test(@FormDataParam("file")List<FormDataBodyPart> bodyParts) throws IOException{

        List<Map<String,Object>> fileContentList = new ArrayList<Map<String,Object>>();

        Json json = new Json(fileContentList);

        if(bodyParts.get(0).getFormDataContentDisposition().getFileName().equals("")){
            return fileContentList;
        }else{
            for (FormDataBodyPart part : bodyParts) {
                String dataName = part.getFormDataContentDisposition().getFileName();

                File tempFile = part.getValueAs(File.class);

                this.fileRename(tempFile.getParent(),tempFile.getName(),dataName);

                tempFile.mkdir();

                moveTempFile(tempFile,dataName);

                	
                if(Utility.getSuffix(dataName).equals("zip")){
                    zipFilePerforms(tempFile,json,dataName);
                }else{
                    noZipFilePerforms(tempFile,json,dataName);
                }
                Utility.removeDirectory(tempFile);
            }
        }
        return fileContentList;
    }

    public void noZipFilePerforms(File file,Json json,String dataName) throws IOException{
        List<String> resultList = new NineRulesChecker()
                                      .performs(
                                        this.getFilePathArray(file.getPath(),dataName)
                                      );
        this.removeFilePath(resultList);
        if(Utility.getSuffix(dataName).equals("java")){
            json.create(resultList,dataName);
        }
    }

    public void zipFilePerforms(File file,Json json,String dataName) throws IOException{
        ZipUnCompressUtils.unzip(file.getPath() + "/" + dataName, file.getPath());

        System.out.println("U ->" + Utility.getPreffix(dataName));
        
        File dir = new File(file.getPath() + "/" + Utility.getPreffix(dataName));
        List<File> fileList = new ArrayList<File>();
        Utility.readAllFolder(dir,fileList);

        //System.out.println("テスト");
        for(File aFile : fileList){
        	System.out.println("Path: " + aFile.getPath());
            List<String> resultList = new NineRulesChecker().performs(this.getFilePathArray(aFile.getPath()));
            for(String s:resultList) {
            	//System.out.print(s + ", ");
            }
            //System.out.println("");
            System.out.println("aaaa");
            this.removeFilePath(resultList);
            json.create(resultList,aFile.getPath().replace(dir.getPath(), ""));
        }
    }

    public void removeFilePath(List<String> resultList){
        resultList.remove(0);
    }

    public void fileRename(String parent,String oldFileName,String newFileName){
        File fOld = new File(Paths.get(parent,oldFileName).toString());
        File fNew = new File(Paths.get(parent,newFileName).toString());
        this.fileRename(fOld,fNew);
    }

    public void fileRename(File fOld,File fNew){
        fOld.renameTo(fNew);
    }

    public String[] getFilePathArray(String parent,String newFileName){
        String pathName = new StringBuilder(parent).append("/").append(newFileName).toString();
        return getFilePathArray(pathName);
    }

    public String[] getFilePathArray(String pathName){
        String[] targetPathArray = new String[1];
        targetPathArray[0] = pathName;
        return targetPathArray;
    }

    public void moveTempFile(File tempFile,String dataName){
        File sourceDataFile = new File(tempFile.getParent() + "/" + dataName);
        File targetDataFile = new File(tempFile.getPath() + "/" + dataName);
        sourceDataFile.renameTo(targetDataFile);
    }
}
