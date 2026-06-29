package com.rateLimiter.distributedratelimiter.key;

import jakarta.servlet.http.HttpServletRequest;

public class IpKeyExtractor implements KeyExtractor{

    @Override
    public String extract(HttpServletRequest request){
        String forwardedFor=request.getHeader("X-Forwarded-For");
        String ip;
        if(forwardedFor!=null && !forwardedFor.isBlank()){
            ip=forwardedFor.split(",")[0]
                    .trim();
        }else{
            ip=request.getRemoteAddr();
        }
        return prefix()+":"+ip;
    }

    @Override
    public String prefix(){
        return "ip";
    }
}
