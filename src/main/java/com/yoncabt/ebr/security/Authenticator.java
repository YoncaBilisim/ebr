/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.security;

import com.yoncabt.ebr.security.exceptions.SecurityException;

/**
 *
 * @author myururdurmaz
 */
public interface Authenticator {

    boolean check(String user, String pass) throws SecurityException;
}
