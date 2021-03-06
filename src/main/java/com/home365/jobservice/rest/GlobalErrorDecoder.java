package com.home365.jobservice.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.home365.jobservice.exception.GeneralException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalErrorDecoder implements ErrorDecoder {
    private final Gson gson;

    public GlobalErrorDecoder() {
        this.gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    }

    @Override
    public Exception decode(String s, Response response) {
        GeneralException generalException = new GeneralException();
        generalException.setMethodName(s);
        try {
            String reasonBody = Util.toString(response.body().asReader());
            generalException.setMessage(reasonBody);
            generalException.setMethodName(s);
            log.info(generalException.getMessage());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("Exception :  {}", gson.toJson(generalException));
        return generalException;
    }
}
