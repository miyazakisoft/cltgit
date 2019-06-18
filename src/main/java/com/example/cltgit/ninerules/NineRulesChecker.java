package com.example.cltgit.ninerules;

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.github.ninerules.Main;

/**
 * 9rulesを基にチェックするクラスである.
 * @author Miyazaki
 * @since 2017/07/25
 */

public class NineRulesChecker {

    public void setOutPerforms(OutputStream baos){
        PrintStream ps = new PrintStream(baos,true);
        System.setOut(ps);
    }

    public OutputStream byteArrayOutputChange(){
        OutputStream baos = new ByteArrayOutputStream();
        this.setOutPerforms(baos);
        return baos;
    }

    public void standardOutputChange(){
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        this.setOutPerforms(new BufferedOutputStream(fdOut, 128));
    }

    public List<String> performs(String[] targetPathArray) throws IOException{
        OutputStream baos = this.byteArrayOutputChange();

        new Main(targetPathArray,new PrintWriter(System.out));

        String[] nineRulesResult = baos.toString().split(System.getProperty("line.separator"));
        baos.close();

        this.standardOutputChange();
        return new ArrayList(Arrays.asList(nineRulesResult));
    }

}
