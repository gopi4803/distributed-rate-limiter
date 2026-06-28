package com.rateLimiter.distributedratelimiter.exceptions;

public class InvalidLuaScriptException extends RateLimiterException{

    public InvalidLuaScriptException(String message){
        super(message);
    }

}
