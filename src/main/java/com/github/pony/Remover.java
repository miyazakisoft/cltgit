package com.github.pony;

import java.io.File;

public class Remover{
    void run(String[] args){
        for(String arg: args){
            this.remove(new File(arg));
        }
    }
    void remove(File target){
        if(target.isDirectory()){
            for(File file: target.listFiles()){
                remove(file);
            }
            target.delete();
        }
        else{
            target.delete();
        }
    }
    public static void main(String[] args){
        Remover remover = new Remover();
        remover.run(args);
    }
}
