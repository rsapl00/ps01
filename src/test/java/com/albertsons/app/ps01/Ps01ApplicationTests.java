package com.albertsons.app.ps01;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Ps01ApplicationTests {

	@Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                ContextResource resource = new ContextResource();           
                //resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
                resource.setName("jdbc/ps01");
                resource.setType(DataSource.class.getName());
                resource.setProperty("driverClassName", "com.ibm.db2.jcc.DB2Driver");
                resource.setProperty("url", "jdbc:db2://DBU3:483/DBU3");
                resource.setProperty("username", "ps01");
                resource.setProperty("password", "ps01");
                context.getNamingResources().addResource(resource);
            }
        };
    }


	@Test
	public void contextLoads() {
	}



}
