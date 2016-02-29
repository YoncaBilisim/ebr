/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ws;

import com.yoncabt.ebr.ReportServerApplication;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.ws.ReportWS;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 *
 * @author myururdurmaz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ReportServerApplication.class)
@WebAppConfiguration
public class ReportWSTest {

    @Autowired
    private ReportWS reportWS;

    public ReportWSTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of request method, of class ReportWS.
     */
    @Test
    public void testRequest() throws Exception {
        System.out.println("request");
        ReportRequest req = new ReportRequest();
        req.setAsync(false);
        req.setDatasourceName("maski-test");
        req.setExtension("pdf");
        req.setLocale("tr_TR");
        req.setReport("olmayan rapor.jrxml");
        Map<String, Object> params = new HashMap<>();
        params.put("CORP_NAME", "DENEM A.Ş");
        params.put("LANG", 1);
        req.setReportParams(params);
        MockMvcBuilders.standaloneSetup(reportWS)
                .build()
                .perform(post("/request").accept(MediaType.APPLICATION_JSON).content(new JSONObject(req).toString()))
                .andExpect(status().isNotFound());

    }

}
