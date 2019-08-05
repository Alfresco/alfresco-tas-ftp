package org.alfresco.ftp.dsl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.ftp.FTPWrapper;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;

public class FTPUtil
{
    private FTPWrapper ftpWrapper;

    public FTPUtil(FTPWrapper ftpWrapper)
    {
        this.ftpWrapper = ftpWrapper;
    }

    /**
     * Return the content of <filPath though FTP protocol
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public String getContent(String filePath) throws IOException
    {
        InputStream inputStream = ftpWrapper.getClient().retrieveFileStream(filePath);
        String content="";
        if(inputStream != null)
        {
            ftpWrapper.getClient().completePendingCommand();
            content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);
        }
        return content;
    }

    /**
     * Verify if a file or folder exists in FTP
     * 
     * @return
     * @throws Exception
     */
    public boolean contentExists() throws Exception
    {
        String lastResource = ftpWrapper.getLastResource();
        boolean change = ftpWrapper.getClient().changeWorkingDirectory(ftpWrapper.getLastResource());
        if (change)
        {
            // folder exists
            return true;
        }
        else
        {
            // verify if file exists
            File file = new File(lastResource);
            FTPFile[] files = ftpWrapper.getClient().listFiles(file.getParent());
            for (FTPFile searchFile : files)
            {
                if (searchFile.isDirectory())
                {
                    continue;
                }
                if (searchFile.getName().equals(file.getName()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isFolderInList(FolderModel folderModel, List<FolderModel> folders)
    {
        for (FolderModel folder : folders)
        {
            if (folderModel.getName().equals(folder.getName()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isFileInList(FileModel fileModel, List<FileModel> files)
    {
        for (FileModel file : files)
        {
            String name = new File(fileModel.getName()).getName();
            if (name.equals(file.getName()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isContentInList(ContentModel contentModel, List<ContentModel> contents)
    {
        for (ContentModel content : contents)
        {
            if (content.getName().equals(content.getName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a list of folders from current location
     * 
     * @return List<FolderModel>
     * @throws Exception
     */
    public List<FolderModel> getFolders() throws Exception
    {
        List<FolderModel> folders = new ArrayList<FolderModel>();
        FTPFile[] directories = ftpWrapper.getClient().listDirectories(ftpWrapper.getLastResource());
        for (FTPFile file : directories)
        {
            if (file.isDirectory())
            {
                folders.add(new FolderModel(file.getName()));
            }
        }
        return folders;
    }

    /**
     * Get a list of files from current location
     * 
     * @return List<FileModel>
     * @throws Exception
     */
    public List<FileModel> getFiles() throws Exception
    {
        List<FileModel> files = new ArrayList<FileModel>();
        FTPFile[] fileList = ftpWrapper.getClient().listFiles(ftpWrapper.getLastResource());
        for (FTPFile file : fileList)
        {
            if (file.isDirectory())
            {
                continue;
            }
            files.add(new FileModel(file.getName()));
        }
        return files;
    }

    /**
     * Get a list of contents (file and folders) from current location
     * 
     * @return List<FolderModel>
     * @throws Exception
     */
    public List<ContentModel> getChildren() throws Exception
    {
        List<ContentModel> children = new ArrayList<ContentModel>();
        FTPFile[] fileList = ftpWrapper.getClient().listFiles(ftpWrapper.getLastResource());
        for (FTPFile file : fileList)
        {
            children.add(new ContentModel(file.getName()));
        }
        return children;
    }
    
    public File setNewFile(FileModel fileModel) throws Exception
    {
        File newFile = new File(fileModel.getName());
        newFile.createNewFile();
        if (!StringUtils.isEmpty(fileModel.getContent()))
        {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(newFile), Charset.forName("UTF-8").newEncoder());
            writer.write(fileModel.getContent());
            writer.flush();
            writer.close();
        }
        newFile.deleteOnExit();
        return newFile;
    }
    
    /**
     * Get file size. Return 0 if resource is a folder
     * @return file size
     * @throws Exception
     */
    public int getFileSize() throws Exception
    {
        File content = new File(ftpWrapper.getLastResource());
        FTPFile[] fileList = ftpWrapper.getClient().listFiles(content.getParent());
        for (FTPFile file : fileList)
        {
            if(content.getName().equals(file.getName()))
            {
                if(file.isDirectory())
                {
                    return 0;
                }
                String strSize = FileUtils.byteCountToDisplaySize(file.getSize()).replace(" MB", "");
                return Integer.parseInt(strSize);
            }
        }
        return 0;
    }
    
    protected synchronized int getReplyCode()
    {
        return ftpWrapper.replyCode;
    }
}

