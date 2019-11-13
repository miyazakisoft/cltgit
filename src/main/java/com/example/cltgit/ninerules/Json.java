package com.example.cltgit.ninerules;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * jsonを生成するクラスである.
 * @author Miyazaki
 * @since 2017/07/25
 */

public class Json{
    List<Map<String,Object>> fileContentList;

    public Json(List<Map<String,Object>> aFileContentList){
        super();
        this.fileContentList = aFileContentList;
    }
    
    public List<Map<String,Object>> getFileContentList() {
    	return fileContentList;
    }
    

    public void create(List<String> resultList,String filePath){
        this.addFileContentList(resultList,this.fileContentList,filePath);
    }

    public void addFileContentList(List<String> resultList,List<Map<String,Object>> fileContentList,String filePath){
        //if(resultList.size() != 0){
            fileContentList.add(fileContentMapCreate(resultList,filePath));
        //}
    }

    public Map<String,Object> fileContentMapCreate(List<String> resultList,String filePath){
        Map<String,Object> fileContentMap = new LinkedHashMap<String,Object>();
        fileContentMap.put("path",filePath);
        fileContentMap.put("violations",violationContentMapCreate(resultList));
        return fileContentMap;
    }

    public Map<String,Object> violationContentMapCreate(List<String> resultList){
        Map<String,Object> violationContentMap = new LinkedHashMap<String,Object>();


        List<Map<String,Object>> violationTypeList = null;

        for(ViolationCase vc : ViolationCase.values()){
            //System.out.println("[" + vc.ordinal() + "]" + vc.name() + "," + vc.getTypeName() + "," + vc.getSubTypeNumber() + "," + vc.getMessageFormat());

            if(violationContentMap.containsKey(vc.getTypeName())){
                this.addViolationTypeList(violationTypeList,resultList,vc.getSubTypeNumber(),vc.getMessageFormat());
            }else{
                violationTypeList = violationTypeListCreate(resultList,vc.getSubTypeNumber(),vc.getMessageFormat());
                if(violationTypeList.size() != 0){
                    violationContentMap.put(vc.getTypeName(),violationTypeList);
                }
            }
        }
        return violationContentMap;
    }

    public List<Map<String,Object>> violationTypeListCreate(List<String> resultList,Integer subTypeNumber,String messageFormat){
        List<Map<String,Object>> violationTypeList = new ArrayList<Map<String,Object>>();
        addViolationTypeList(violationTypeList,resultList,subTypeNumber,messageFormat);
        return violationTypeList;
    }

    public void addViolationTypeList(List<Map<String,Object>> violationTypeList,List<String> resultList,Integer subTypeNumber,String messageFormat){
        for(String violationContent: resultList){
            String regrex = "line:.(?<LINE>.+),\\s*(?<MESSAGE>.*)";
            Pattern pattern = Pattern.compile(regrex,Pattern.CASE_INSENSITIVE);
            Matcher match = pattern.matcher(violationContent);

            while (match.find()){
                String[] lineNumbers = match.group("LINE").split(",");
                String message = match.group("MESSAGE");

                for(String aLineNumber:lineNumbers){
                    if(message.matches(messageFormat)){
                        violationTypeList.add(violationTypeMapCreate(subTypeNumber,Integer.parseInt(aLineNumber),message));
                    }
                }
            }
        }
    }

    public Map<String,Object> violationTypeMapCreate(Integer subTypeNumber,Integer lineNumber, String message){
        Map<String,Object> violationTypeMap = new LinkedHashMap<String,Object>();

        if(subTypeNumber != 0){
            violationTypeMap.put("sub_type",subTypeNumber);
        }
        violationTypeMap.put("lines",lineNumber);
        violationTypeMap.put("message",message);

        return violationTypeMap;
    }
}
