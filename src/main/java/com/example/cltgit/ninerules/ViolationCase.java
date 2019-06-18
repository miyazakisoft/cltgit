package com.example.cltgit.ninerules;
/**
 * 違反ケースを列挙している.
 * @author Miyazaki
 * @since 2017/11/28
 */

public enum ViolationCase{
    CASE01("type1",0,"indentation[\\s]level[\\s]is[\\s]too[\\s]much[\\s][\\(]more[\\s]than[\\s][0-9]{1,}[\\s]indent[\\s]level[\\)][\\.]"),
    CASE02("type2",0,"else[\\s]statement[\\s]found[\\.]"),
    CASE03("type3",0,"no[\\s]primitives[\\.]"),
    CASE04("type4",0,"many[\\s]dots[\\s]per[\\s]line[\\s][\\(]more[\\s]than[\\s][0-9]{1,}[\\s]dots[\\)][\\.]"),
    //CASE05("ViolationType5",0,""),
    CASE06("type6",1,"source[\\s]code[\\s]is[\\s]too[\\s]long[\\s][\\(]over[\\s][0-9]{1,}[\\s]lines[\\)][\\.]"),
    CASE07("type6",2,"method[\\s]is[\\s]too[\\s]long[\\s][\\(]over[\\s][0-9]{1,}[\\s]lines[\\)][\\.]"),
    //CASE08("ViolationType6",3,""),
    CASE09("type7",0,"field[\\s]count[\\s]is[\\s]more[\\s]than[\\s][0-9]{1,}[\\.]"),
    CASE10("type8",0,"not[\\s]first[\\s]class[\\s]collection[\\.]"),
    CASE11("type9",1,"getter[\\s]method[\\s]found[\\.]"),
    CASE12("type9",2,"setter[\\s]method[\\s]found[\\.]");

    private final String typeName;
    private final Integer subTypeNumber;
    private final String messageFormat;

    ViolationCase(String typeName,Integer subTypeNumber,String messageFormat){
        this.typeName = typeName;
        this.subTypeNumber = subTypeNumber;
        this.messageFormat = messageFormat;
    }

    String getTypeName(){
        return this.typeName;
    }

    Integer getSubTypeNumber(){
        return this.subTypeNumber;
    }

    String getMessageFormat(){
        return this.messageFormat;
    }

    public static ViolationCase getCaseByTypeName(String typeName) {
        for(ViolationCase vc : values()) {
            if (typeName.equals(vc.getTypeName())){
                return vc;
            }
        }
        throw new IllegalArgumentException("undefined : " + typeName);
    }

    public static ViolationCase getCaseBySubTypeNumber(Integer subTypeNumber) {
        for(ViolationCase vc : values()) {
            if(subTypeNumber == vc.getSubTypeNumber()){
                return vc;
            }
        }
        throw new IllegalArgumentException("undefined : " + subTypeNumber);
    }

    public static ViolationCase getCaseByMessage(String message){
        for(ViolationCase vc : values()) {
            if(message.matches(vc.getMessageFormat())){
                return vc;
            }
        }
        throw new IllegalArgumentException("undefined : " + message);
    }
}
