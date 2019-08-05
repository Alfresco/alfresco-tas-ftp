package org.alfresco.ftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:default.properties")
@PropertySource(value = "classpath:${environment}.properties", ignoreResourceNotFound = true)
public class FTPProperties
{

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${ftp.port}")
    private int ftpPort;
    
    @Value("${ftp.passiveMode}")
    private boolean passiveModel;

    public int getFtpPort()
    {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort)
    {
        this.ftpPort = ftpPort;
    }

    @Value("${ftp.timeout}")
    private int ftpTimeout;

    public int getftpTimeout()
    {
        return ftpTimeout;
    }

    public void setftpTimeout(int timeout)
    {
        this.ftpTimeout = timeout;
    }

    public boolean isPassiveModel()
    {
        return passiveModel;
    }

    public void setPassiveModel(boolean passiveModel)
    {
        this.passiveModel = passiveModel;
    }

}
