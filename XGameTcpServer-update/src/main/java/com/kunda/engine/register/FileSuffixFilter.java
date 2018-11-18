package com.kunda.engine.register;


import java.io.File;
import java.io.FilenameFilter;


public class FileSuffixFilter implements FilenameFilter {


    private String type;

    public FileSuffixFilter(String tp){
        this.type = tp;
    }




    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(type);
    }







}
