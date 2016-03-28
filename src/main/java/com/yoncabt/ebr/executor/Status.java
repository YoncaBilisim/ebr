/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import org.springframework.http.HttpStatus;

/**
 *
 * @author myururdurmaz
 */
public enum Status {

    WAIT(HttpStatus.CREATED),
    RUN(HttpStatus.PROCESSING),
    EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR),
    CANCEL(HttpStatus.INTERNAL_SERVER_ERROR),//FIXME
    FINISH(HttpStatus.OK),
    SCHEDULED(HttpStatus.CREATED);

    private HttpStatus httpStatus;

    private Status(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
