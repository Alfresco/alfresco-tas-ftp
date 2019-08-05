package org.alfresco.ftp.dsl;

import static org.alfresco.utility.report.log.Step.STEP;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.alfresco.ftp.FTPWrapper;
import org.alfresco.utility.dsl.DSLAssertion;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.apache.commons.net.ftp.FTPReply;
import org.testng.Assert;

/**
 * DSL with all assertion available for {@link FTPWrapper}
 */
public class FTPAssertion extends DSLAssertion<FTPWrapper>
{
    public FTPAssertion(FTPWrapper ftpProtocol)
    {
        super(ftpProtocol);
    }

    public FTPWrapper userIsLoggedIn()
    {
        STEP("FTP: Assert user is logged in");
        Assert.assertEquals(getProtocol().replyCode, FTPReply.USER_LOGGED_IN);
        return getProtocol();
    }

    public FTPWrapper userIsNotLoggedIn()
    {
        STEP("FTP: Assert user is not connected");
        Assert.assertFalse(getProtocol().getClient().isConnected());
        return getProtocol();
    }

    public FTPWrapper currentFolderIs(String folder) throws Exception
    {
        STEP(String.format("%s Assert user current folder is '%s'", FTPWrapper.STEP_PREFIX, folder));
        String currentFolder = getProtocol().getCurrentDirectory();
        Assert.assertEquals(currentFolder, folder);
        return getProtocol();
    }

    public FTPWrapper contentIs(String expectedContent) throws IOException
    {
        STEP(String.format("%s Assert current content is '%s'", FTPWrapper.STEP_PREFIX, expectedContent));
        String actualContent = getProtocol().withFtpUtil().getContent(getProtocol().getLastResource());

        Assert.assertEquals(actualContent, expectedContent, String.format("Content for file %s is not the expected one.", getProtocol().getLastResource()));
        return getProtocol();
    }

    /**
     * Verify if file is downloaded in project root.
     * 
     * @return
     */
    public FTPWrapper isDownloaded()
    {
        STEP(String.format("FTP: Assert that file is downloaded:  %s ", getProtocol().lastDownloadedFile.getPath()));
        Assert.assertTrue(getProtocol().lastDownloadedFile.exists(), String.format("%s is downloaded", getProtocol().lastDownloadedFile.getPath()));
        return getProtocol();
    }

    public FTPWrapper existsInFtp() throws Exception
    {
        String contentName = new File(getProtocol().getLastResource()).getName();
        STEP(String.format("%s Verify that content '%s' exists in FTP", FTPWrapper.STEP_PREFIX, contentName));
        Assert.assertTrue(getProtocol().withFtpUtil().contentExists(), String.format("Content %s exists in FTP", contentName));
        return getProtocol();
    }

    public FTPWrapper doesNotExistInFtp() throws Exception
    {
        String contentName = new File(getProtocol().getLastResource()).getName();
        STEP(String.format("%s Verify that content '%s' does not exist in FTP", FTPWrapper.STEP_PREFIX, contentName));
        Assert.assertFalse(getProtocol().withFtpUtil().contentExists(), String.format("Content %s exists in FTP", contentName));
        return getProtocol();
    }

    /**
     * Verify if folder children exist in parent folder
     * 
     * @param fileModel children files
     * @return
     * @throws Exception
     */
    public FTPWrapper hasFolders(FolderModel... folderModel) throws Exception
    {
        String currentSpace = getProtocol().getCurrentSpace();
        List<FolderModel> folders = getProtocol().getFolders();
        for (FolderModel folder : folderModel)
        {
            STEP(String.format("%s Verify that folder %s is in %s", FTPWrapper.STEP_PREFIX, folder.getName(), currentSpace));
            Assert.assertTrue(getProtocol().withFtpUtil().isFolderInList(folder, folders), String.format("Folder %s is in %s", folder.getName(), currentSpace));
        }
        return getProtocol();
    }

    /**
     * Verify if file children exist in parent folder
     * 
     * @param fileModel children files
     * @return
     * @throws Exception
     */
    public FTPWrapper hasFiles(FileModel... fileModel) throws Exception
    {
        String currentSpace = getProtocol().getCurrentSpace();
        List<FileModel> files = getProtocol().getFiles();
        for (FileModel file : fileModel)
        {
            STEP(String.format("%s Verify that file %s is in %s", FTPWrapper.STEP_PREFIX, file.getName(), currentSpace));
            Assert.assertTrue(getProtocol().withFtpUtil().isFileInList(file, files), String.format("File %s is in %s", file.getName(), currentSpace));
        }
        return getProtocol();
    }

    /**
     * Verify the children(files and folders) from a parent folder
     * 
     * @param contentModel children
     * @return
     * @throws Exception
     */
    public FTPWrapper hasChildren(ContentModel... contentModel) throws Exception
    {
        String currentSpace = getProtocol().getCurrentSpace();
        List<ContentModel> contents = getProtocol().withFtpUtil().getChildren();
        for (ContentModel content : contentModel)
        {
            STEP(String.format("%s Verify that '%s' is in %s", FTPWrapper.STEP_PREFIX, content.getName(), currentSpace));
            Assert.assertTrue(getProtocol().withFtpUtil().isContentInList(content, contents),
                    String.format("Content %s is in %s", content.getName(), currentSpace));
        }
        return getProtocol();
    }
    
    /**
     * Verify the size of a file
     * @param sizeInMb size in MB
     * @return
     * @throws Exception 
     */
    public FTPWrapper hasSize(int sizeInMb) throws Exception
    {
        STEP(String.format("%s Verify that file '%s' size is %s Mb", FTPWrapper.STEP_PREFIX, getProtocol().getLastResource(), sizeInMb));
        Assert.assertEquals(getProtocol().withFtpUtil().getFileSize(), sizeInMb, String.format("File size is %d Mb", sizeInMb));
        return getProtocol();
    }
    
    /**
     * Verify the status for a specific FTP action
     * 
     * @param replyCode code to verify
     * @return
     */
    public FTPWrapper hasReplyCode(int replyCode)
    {
        STEP(String.format("%s Verify that reply code is %s", FTPWrapper.STEP_PREFIX, replyCode));
        Assert.assertEquals(getProtocol().withFtpUtil().getReplyCode(), replyCode, String.format("Verify FTP reply code"));
        return getProtocol();
    }
}