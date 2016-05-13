package com.yoncabt.ebr;

import com.yoncabt.ebr.util.VersionUtil;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        VersionUtil.print();
        return application.sources(ReportServerApplication.class);
    }

}
