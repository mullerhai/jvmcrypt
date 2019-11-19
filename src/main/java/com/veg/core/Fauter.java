package com.veg.core;

public class Fauter {

    public static void main(String[] args){

        var name ="world ";
        System.out.println("hello"+name);

        var license =new LicenseFile();
        String code ="AAABKA0ODAoPeJxtkEFvwiAYhu/8CpKdMaVq1CUkqy0Ho2111WQ7su7rSkLBQGl0v35o52XZDfje9\n" +
                "8nD95QbjQszYLrEEX2Oo+d4htPqiOOIrlAGrrby3EujWWp0ozzoGlDhuw+wZXNyYB0jFKUWxC2Ui\n" +
                "R7YrUkoJXSJQqcXdV+IDljnlQKL6oCZhDc5AOuth0eG50Iq1gr50phLF86T2nSID0L5O5o1QjkY6\n" +
                "ztZg3ZwvJ7hjk7LPOev6SbZoUDRPWgRNPnlLO11VJpOF4TGJJ6PgMcHUuVdD7Ywn+BYhCpesPfyh\n" +
                "PNky3HOcYKrJMP7pMiSCSrtl9DSjTJt+40qsAPYTcbWi9mWxLNDQVbr8kDeksMc/SqG6W6TPW7/G\n" +
                "+29rVvh4M/yfgBV14TBMCwCFDeRpub6yENYhTHtVewsKwz1BmcEAhQPEJftfVbeGvaeqrfzE437Q\n" +
                "95q+g==X02eu";
        String er=license.genLicense("muller","hai@foxmail.com","hhz","B74K-24QN-9BOQ-XAQ5");
        System.out.println(er);
    }
}
