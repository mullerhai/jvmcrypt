package com.veg.core;

public class LicenseException extends Exception {

    public LicenseException(){
        super("file not found");
    }
    public LicenseException(String message){
        super(message);
    }

    public LicenseException(Exception message){
        super(message.getMessage());
    }
    public LicenseException(String message,Throwable cause){
        super(message,cause);
    }

    public LicenseException(Throwable cause){
        super(cause);
    }

    public  LicenseException(String message,Throwable cause,boolean enableSuppression,boolean writeableStatckTrace){
        super(message,cause,enableSuppression,writeableStatckTrace);
    }
}
