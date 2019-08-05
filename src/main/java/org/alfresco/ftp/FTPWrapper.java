package org.alfresco.ftp;

import static org.alfresco.utility.Utility.checkObjectIsInitialized;
import static org.alfresco.utility.Utility.getParentPath;
import static org.alfresco.utility.Utility.removeLastSlash;
import static org.alfresco.utility.report.log.Step.STEP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.alfresco.ftp.dsl.FTPAssertion;
import org.alfresco.ftp.dsl.FTPUtil;
import org.alfresco.ftp.dsl.JmxUtil;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.Utility;
import org.alfresco.utility.dsl.DSLContentModelAction;
import org.alfresco.utility.dsl.DSLFile;
import org.alfresco.utility.dsl.DSLFolder;
import org.alfresco.utility.dsl.DSLProtocol;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "prototype")
public class FTPWrapper extends DSLProtocol<FTPWrapper> implements DSLContentModelAction<FTPWrapper>, DSLFolder<FTPWrapper>, DSLFile<FTPWrapper>
{
    @Autowired
    protected FTPProperties fTPProperties;

    @Autowired
    protected TasProperties tasProperties;

    public static String STEP_PREFIX = "FTP:";
    public int replyCode;

    private FTPClient ftpClient;
    public File lastDownloadedFile;

    public FTPClient getClient()
    {
        if (ftpClient == null)
        {
        	LOG.info("New ftp client");
        	ftpClient = new FTPClient();
        }
        return ftpClient;
    }

    private void setFTPMode()
    {
        LOG.info("Using passive mode: {} (you can change this on default.properties file) ", fTPProperties.isPassiveModel());
        if (fTPProperties.isPassiveModel())
            getClient().enterLocalPassiveMode();
        else
            getClient().enterLocalActiveMode();
    }

    @Override
    public FTPWrapper authenticateUser(UserModel userModel) throws Exception
    {
        STEP(String.format("FTP: Connect with user %s/%s on %s:%d", userModel.getUsername(), userModel.getPassword(), tasProperties.getServer(),
                fTPProperties.getFtpPort()));
        getClient().setDefaultPort(fTPProperties.getFtpPort());
        getClient().setDataTimeout(fTPProperties.getftpTimeout());
        getClient().setControlEncoding("UTF-8");
        getClient().connect(tasProperties.getServer());
        setFTPMode();
        getClient().login(userModel.getUsername(), userModel.getPassword());
        replyCode = getClient().getReplyCode();
        getClient().setFileType(FTP.BINARY_FILE_TYPE);
        setTestUser(userModel);
        return this;
    }
    
    public FTPWrapper authenticateUser(UserModel userModel, String server, int port) throws Exception
    {
        STEP(String.format("FTP: Connect with user %s/%s on %s:%d", userModel.getUsername(), userModel.getPassword(), port, port));
        getClient().setDefaultPort(port);
        getClient().setDataTimeout(fTPProperties.getftpTimeout());
        getClient().setControlEncoding("UTF-8");
        getClient().connect(server);
        setFTPMode();
        getClient().login(userModel.getUsername(), userModel.getPassword());
        replyCode = getClient().getReplyCode();
        getClient().setFileType(FTP.BINARY_FILE_TYPE);
        setTestUser(userModel);
        return this;
    }

    /**
     * Authenticate user and set the file transfer mode (use {@link FTP})
     * codes: 10 - FTP.STREAM_TRANSFER_MODE, 11 - FTP.BLOCK_TRANSFER_MODE, 12 - FTP.COMPRESSED_TRANSFER_MODE)
     * 
     * @param userModel
     * @param fileTransferMode
     * @return
     * @throws Exception
     */
    public FTPWrapper authenticateUser(UserModel userModel, int fileTransferMode) throws Exception
    {
        authenticateUser(userModel);
        STEP(String.format("Set File Transfer Mode to %d", fileTransferMode));
        getClient().setFileTransferMode(fileTransferMode);
        return this;
    }

    @Override
    public FTPWrapper createFolder(FolderModel folderModel) throws Exception
    {
        STEP(String.format("FTP: Create folder '%s'", folderModel.getName()));
        checkObjectIsInitialized(folderModel, "newFolder");
        String ftpFolder = buildPath(getCurrentSpace(), folderModel.getName());
        getClient().makeDirectory(ftpFolder);
        replyCode = ftpClient.getReplyCode();
        setLastResource(ftpFolder);
        folderModel.setProtocolLocation(ftpFolder);
        folderModel.setCmisLocation(getLastResourceWithoutPrefix());
        folderModel.setNodeRef(contentService.getNodeRefByPath(getTestUser().getUsername(), getTestUser().getPassword(), getLastResourceWithoutPrefix()));
        return this;
    }

    @Override
    public String getPrefixSpace()
    {
        return String.format("Alfresco/");
    }

    @Override
    protected String getProtocolJMXConfigurationStatus() throws Exception
    {
        return withJMX().getFTPServerConfigurationStatus();
    }

    @Override
    public FTPWrapper disconnect() throws Exception
    {
        STEP("FTP: Disconnect");
        if (getClient().isConnected())
        {
            getClient().logout();
            getClient().disconnect();
            replyCode = getClient().getReplyCode();
        }
        return this;
    }

    public FTPWrapper changeWorkingDirectory(String newFolder) throws Exception
    {
        checkObjectIsInitialized(newFolder, "newFolder");
        LOG.info("FTP: Change working directory {}", newFolder);
        getClient().changeWorkingDirectory(newFolder);
        replyCode = getClient().getReplyCode();
        if (replyCode == 530)
        {
            throw new FTPConnectionClosedException(String.format("Cannot connect with user: %s", getCurrentUser().getUsername()));
        }
        else if (replyCode != 550)
        {
            String currentSpace = getCurrentDirectory();
            setCurrentSpace(currentSpace);
        }
        else
        {
            setCurrentSpace(buildPath(newFolder));
        }
        return this;
    }

    public String getCurrentDirectory() throws Exception
    {
        STEP(String.format("FTP: List working folder: '%s'", getClient().printWorkingDirectory()));
        return getClient().printWorkingDirectory().replace("//", "/");
    }

    @Override
    public String getRootPath() throws TestConfigurationException
    {
        return String.format("/Alfresco");
    }

    @Override
    public FTPWrapper usingRoot() throws TestConfigurationException, Exception
    {
        return changeWorkingDirectory(getRootPath());
    }

    @Override
    public FTPWrapper usingUserHome() throws TestConfigurationException, Exception
    {
        return changeWorkingDirectory(getUserHomesPath());
    }

    @Override
    public List<FileModel> getFiles() throws Exception
    {
        STEP(String.format("FTP: Get files under directory '%s'", getLastResource()));
        return withFtpUtil().getFiles();
    }

    @Override
    public List<FolderModel> getFolders() throws Exception
    {
        STEP(String.format("FTP: Get folders under directory '%s'", getLastResource()));
        return withFtpUtil().getFolders();
    }

    @Override
    public String getSitesPath() throws TestConfigurationException
    {
        return String.format("%s%s%s", "/", getPrefixSpace(), "Sites");
    }

    @Override
    public String getUserHomesPath() throws TestConfigurationException
    {
        return String.format("%s%s%s", "/", getPrefixSpace(), "User Homes");
    }

    @Override
    public String getDataDictionaryPath() throws TestConfigurationException
    {
        return String.format("%s%s%s", "/", getPrefixSpace(), "Data Dictionary");
    }

    @Override
    public FTPWrapper usingSite(String siteId) throws Exception
    {
        STEP(String.format("FTP: Navigate to site '%s/documentLibrary/'", siteId));
        return changeWorkingDirectory(buildSiteDocumentLibraryPath(siteId));
    }

    @Override
    public FTPWrapper usingSite(SiteModel siteModel) throws Exception
    {
        STEP(String.format("FTP: Navigate to site '%s/documentLibrary/'", siteModel.getId()));
        return changeWorkingDirectory(buildSiteDocumentLibraryPath(siteModel.getId()));
    }

    @Override
    public FTPWrapper usingUserHome(String username) throws Exception
    {
        STEP(String.format("FTP: Navigate to 'UserHomes/%s/'", username));
        return changeWorkingDirectory(buildUserHomePath(username, ""));
    }

    @Override
    public FTPWrapper usingResource(ContentModel model) throws Exception
    {
        setLastContentModel(model);
        STEP(String.format("FTP: Navigate to '%s'", model.getName()));
        if (model.getCmisLocation().equals(model.getName()))
        {
            return changeWorkingDirectory(buildPath(getCurrentSpace(), model.getName()));
        }
        return changeWorkingDirectory(buildPath(getPrefixSpace(), model.getCmisLocation()));
    }

    @Override
    public FTPWrapper rename(String newName) throws Exception
    {
        String ftpContent = getLastResource();
        String renamedFile = buildPath(removeLastSlash(getParentPath(getLastResource())), newName);
        STEP(String.format("FTP: Rename content '%s' to '%s'", ftpContent, renamedFile));
        getClient().rename(ftpContent, renamedFile);
        replyCode = ftpClient.getReplyCode();
        setLastResource(renamedFile);
        getLastContentModel().setName(newName);
        getLastContentModel().setCmisLocation(getLastResourceWithoutPrefix());
        getLastContentModel().setProtocolLocation(getLastResource());
        return this;
    }

    @Override
    public FTPWrapper update(String newContent) throws Exception
    {
        String ftpFile = getLastResource();
        STEP(String.format("FTP: Set new content to file '%s'", ftpFile));
        OutputStream outputStream;
        outputStream = ftpClient.storeFileStream(ftpFile);
        replyCode = ftpClient.getReplyCode();
        if (outputStream != null)
        {
            outputStream.write(newContent.getBytes());
            outputStream.close();
            ftpClient.completePendingCommand();
        }
        return this;
    }

    /**
     * Append content to file
     * 
     * @param content
     * @return
     * @throws Exception
     */
    public FTPWrapper appendContent(String content) throws Exception
    {
        String ftpFile = getLastResource();
        STEP(String.format("FTP: Append content to file '%s'", ftpFile));
        InputStream input = new ByteArrayInputStream(content.getBytes());
        getClient().appendFile(new File(ftpFile).getName(), input);
        replyCode = ftpClient.getReplyCode();
        IOUtils.closeQuietly(input);
        return this;
    }

    @Override
    public FTPWrapper delete() throws Exception
    {
        delete(getLastResource());
        return this;
    }

    public FTPWrapper delete(String path) throws Exception
    {
        checkObjectIsInitialized(path, "existingFolder");
        getClient().changeWorkingDirectory(path);
        int returnCode = getClient().getReplyCode();
        if (returnCode != 550)
        {
            FTPFile[] subFiles = getClient().listFiles();
            if(subFiles.length !=0)
            {
                Comparator<FTPFile> sorter = (e1, e2) -> e1.getName().compareTo(e2.getName());
                Arrays.sort(subFiles, sorter);
                for (FTPFile subFile: subFiles)
                {
                    delete(Utility.buildPath(path, subFile.getName()));
                }
            }
            STEP(String.format("FTP: Delete folder '%s'", path));
            getClient().removeDirectory(path);
        }
        else
        {
            STEP(String.format("FTP: Delete file '%s'", path));
            getClient().deleteFile(path);
        }
        replyCode = ftpClient.getReplyCode();
        return this;
    }

    @Override
    public FTPWrapper copyTo(ContentModel destination) throws Exception
    {
        String lastResource = getLastResource();
        changeWorkingDirectory(lastResource);
        int code = getClient().getReplyCode();
        if(!withFtpUtil().contentExists())
        {
            // source does not exist
            return this;
        }
        String name = new File(lastResource).getName();
        changeWorkingDirectory(destination.getProtocolLocation());
        if (code == 550)
        {
            // is file
            InputStream content = new ByteArrayInputStream(withFtpUtil().getContent(lastResource).getBytes(StandardCharsets.UTF_8));
            getClient().storeFile(name, content);
            replyCode = getClient().getReplyCode();
            content.close();
        }
        else
        {
            // is folder
            String parentFolderDestination = buildPath(destination.getProtocolLocation(), name);
            copyFolder(lastResource, parentFolderDestination);
        }
        setLastResource(buildPath(destination.getProtocolLocation(), name));
        return this;
    }

    private void copyFolder(String sourcePath, String destinationPath) throws Exception
    {
        getClient().makeDirectory(destinationPath);

        // copy children
        changeWorkingDirectory(sourcePath);
        FTPFile[] children = getClient().listFiles(sourcePath);
        for (FTPFile child : children)
        {
            changeWorkingDirectory(destinationPath);
            String contentPath = buildPath(sourcePath, child.getName());
            String contentName = new File(contentPath).getName();
            if (child.isFile())
            {
                InputStream content = new ByteArrayInputStream(withFtpUtil().getContent(contentPath).getBytes(StandardCharsets.UTF_8));
                getClient().storeFile(contentName, content);
                content.close();
            }
            else
            {
                String folderPath = buildPath(destinationPath, contentName);
                copyFolder(contentPath, folderPath);
            }
        }
    }

    @Override
    public FTPWrapper moveTo(ContentModel destination) throws Exception
    {
        String ftpContent = getLastResource();
        String relocatedContent = buildPath("/Alfresco", destination.getCmisLocation(), new File(ftpContent).getName());
        STEP(String.format("FTP: Move content from '%s' to '%s'", ftpContent, relocatedContent));

        getClient().rename(ftpContent, relocatedContent);
        setLastResource(relocatedContent);
        return this;
    }

    @Override
    public FTPWrapper createFile(FileModel fileModel) throws Exception
    {
        STEP(String.format("FTP: Creating file '%s' in repository", fileModel.getName()));
        checkObjectIsInitialized(fileModel, "upload content");
        FileInputStream fis = new FileInputStream(withFtpUtil().setNewFile(fileModel));
        getClient().storeFile(fileModel.toFile().getName(), fis);
        replyCode = ftpClient.getReplyCode();
        String ftpLocation = buildPath(getCurrentSpace(), fileModel.toFile().getName());
        setLastResource(ftpLocation);
        fileModel.setProtocolLocation(ftpLocation);
        fileModel.setCmisLocation(getLastResourceWithoutPrefix());
        fileModel.setNodeRef(contentService.getNodeRefByPath(getTestUser().getUsername(), getTestUser().getPassword(), getLastResourceWithoutPrefix()));
        IOUtils.closeQuietly(fis);
        return this;
    }

    public FTPWrapper createFile(FileModel fileModel, int fileSize) throws Exception
    {
        File file = Utility.getFileWithSize(fileModel.getName(), fileSize);
        file.deleteOnExit();
        String content = FileUtils.readFileToString(file);
        STEP(String.format("FTP: Creating file '%s' with size of %d MB", fileModel.getName(), fileSize));
        checkObjectIsInitialized(fileModel, "upload content");
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        getClient().storeFile(fileModel.toFile().getName(), stream);
        String ftpLocation = buildPath(getCurrentSpace(), fileModel.toFile().getName());
        setLastResource(ftpLocation);
        fileModel.setProtocolLocation(ftpLocation);
        fileModel.setCmisLocation(getLastResourceWithoutPrefix());
        fileModel.setNodeRef(contentService.getNodeRefByPath(getTestUser().getUsername(), getTestUser().getPassword(), getLastResourceWithoutPrefix()));
        stream.close();
        return this;
    }
    
    /**
     * Create Office file which will be used in Integration tests by AOS 
     * 
     * @param fileModel
     * @return
     * @throws Exception
     */
    public FTPWrapper createOfficeFile(FileModel fileModel) throws Exception
    {
        STEP(String.format("FTP: Creating file '%s' in repository", fileModel.getName()));
        checkObjectIsInitialized(fileModel, "upload content");
        FileInputStream fis = new FileInputStream(fileModel.getName());
        getClient().setFileType(FTP.BINARY_FILE_TYPE);
        getClient().storeFile(fileModel.toFile().getName(), fis);
        String ftpLocation = buildPath(getCurrentSpace(), fileModel.toFile().getName());
        setLastResource(ftpLocation);
        fileModel.setProtocolLocation(ftpLocation);
        fileModel.setCmisLocation(getLastResourceWithoutPrefix());
        fileModel.setNodeRef(contentService.getNodeRefByPath(getTestUser().getUsername(), getTestUser().getPassword(), getLastResourceWithoutPrefix()));
        fis.close();
        return this;
    }

    /**
     * Upload file from disk via FTP protocol
     * 
     * @param pathToFile
     * @return
     * @throws Exception
     */
    public FTPWrapper uploadFile(String pathToFile) throws Exception
    {
        return uploadFile(new File(pathToFile));
    }
    
    /**
     * Upload file from disk via FTP protocol
     * 
     * @param file {@link File}
     * @return
     * @throws Exception
     */
    public FTPWrapper uploadFile(File file) throws Exception
    {
        STEP(String.format("FTP: Upload file from '%s' in repository", file.getAbsolutePath()));
        checkObjectIsInitialized(file, "upload content");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        FileInputStream fis = new FileInputStream(file);
        getClient().storeFile(file.getName(), fis);
        replyCode = ftpClient.getReplyCode();
        String ftpLocation = buildPath(getCurrentSpace(), file.getName());
        setLastResource(ftpLocation);
        fis.close();
        return this;
    }

    /**
     * @return JMX DSL for this wrapper
     */
    public JmxUtil withJMX()
    {
        return new JmxUtil(this, jmxBuilder.getJmxClient());
    }

    public FTPUtil withFtpUtil()
    {
        return new FTPUtil(this);
    }

    @Override
    public FTPAssertion assertThat()
    {
        return new FTPAssertion(this);
    }

    public String getDocumentLibraryPath(SiteModel siteModel) throws TestConfigurationException, Exception
    {
        STEP(String.format("FTP: Get current Site Document Library path '%s/documentLibrary/'", siteModel.getId()));
        return buildSiteDocumentLibraryPath(siteModel.getId());
    }

    /**
     * Download file locally, returning the new location
     * 
     * @param destination
     * @return
     * @throws Exception
     */
    public FTPWrapper downloadFileTo(FolderModel destination) throws Exception
    {
        lastDownloadedFile = new File(destination.getProtocolLocation() + new File(getLastResource()).getName());
        OutputStream outputStream = new FileOutputStream(lastDownloadedFile);
        getClient().retrieveFile(getLastResource(), outputStream);
        replyCode = ftpClient.getReplyCode();
        outputStream.close();
        return this;
    }
    
    /**
     * Get modification time of last resource
     * 
     * @return
     * @throws Exception
     */
    public String getModificationTime() throws Exception
    {
        STEP(String.format("FTP: Get current Modification Time of ", getLastResource()));
        return ftpClient.getModificationTime(getLastResource());
    }
}