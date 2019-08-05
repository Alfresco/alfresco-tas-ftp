package org.alfresco.ftp.dsl;

import org.alfresco.ftp.FTPWrapper;
import org.alfresco.utility.network.Jmx;
import org.alfresco.utility.network.JmxClient;
import org.alfresco.utility.network.JmxJolokiaProxyClient;

/**
 * DSL for interacting with JMX (using direct JMX call see {@link JmxClient} or {@link JmxJolokiaProxyClient}
 */
public class JmxUtil
{
    @SuppressWarnings("unused")
    private FTPWrapper ftpProtocol;

    private Jmx jmx;

    public JmxUtil(FTPWrapper ftpProtocol, Jmx jmx)
    {
        this.ftpProtocol = ftpProtocol;
        this.jmx = jmx;
    }

    /**
     * @return the JMX value of the server configuration related to FTP
     * @throws Exception
     */
    public String getFTPServerConfigurationStatus() throws Exception
    {
        return jmx.readProperty("Alfresco:Name=FileServerConfig", "FTPServerEnabled").toString();
    }

}
