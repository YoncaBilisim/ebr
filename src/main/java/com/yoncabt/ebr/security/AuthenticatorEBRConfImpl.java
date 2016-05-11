/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.security;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.ebr.security.exceptions.SecurityException;

public class AuthenticatorEBRConfImpl implements Authenticator {

    @Override
    public boolean check(String user, String pass) throws SecurityException {
        boolean ret = EBRConf.INSTANCE.hasValue("authenticator.user." + user)
                && EBRConf.INSTANCE.getValue("authenticator.user." + user, /*not match to default*/ pass + "-").equals(pass);
        if (!ret) {
            try {
                Thread.sleep(1000);//wait for 1 second for password try
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return ret;
    }

}
