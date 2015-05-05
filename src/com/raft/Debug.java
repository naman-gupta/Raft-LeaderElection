package com.raft;

import java.io.PrintStream;
import java.sql.Timestamp;


public class Debug {
    private String name;
    private int level;
    private PrintStream ps_output;
    public static final int DEBUG = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;


    public Debug(String name, int level, PrintStream ps_output) {
        this.name = name;
        this.level = level;
        this.ps_output = ps_output;
    }

    private void treu(String msg) {

        java.util.Date date = new java.util.Date();
        String s_date = new Timestamp(date.getTime()).toString();
        try {
            ps_output.println(this.name + ':' + s_date + ':' + msg);
        } catch (Exception e) {
            System.out.print("Exception while printing to output "+e.getMessage());
        }
    }

    public void debug(String msg) {
        if (level >= this.DEBUG) {
            this.treu("DEBUG: " + msg);
        }
    }

    public void warning(String msg) {
        if (level >= this.WARNING) {
            this.treu("WARNING: " + msg);
        }
    }

    public void error(String msg) {
        if (level >= this.ERROR) {
            this.treu("ERROR: " + msg);
        }
    }

    public void msg(String msg) {
        this.treu(msg);
    }


}
