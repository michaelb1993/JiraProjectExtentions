package com.michabond.utils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;


public class FileUtils {

    private static final String SMB_DOMAIN = null; //"myDomain"
    private static final String SMB_USERNAME = null; //"myUserId"
    private static final String SMB_PASSWORD = null; //"myPassword#"
    private static final String SMB_PATH = "smb://MIKEASUS_PC/tshare/";
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);


    public static void dumpRemoteShare(String data) throws IOException {
        String filename = "test.txt";
        SmbFileOutputStream smbfos = openSmbFile(filename);
        try {
            smbfos.write(data.getBytes());
        }
        catch (IOException e) {
            LOGGER.warn("Failed writing to remote file", e);
            throw e;
        }
        finally {
            if (null != smbfos) {
                try {
                    smbfos.close();
                }
                catch (IOException e) {
                    LOGGER.warn("Failed closing remote file", e);
                }
            }
        }
    }

    private static SmbFileOutputStream openSmbFile(String filename) throws IOException {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(SMB_DOMAIN, SMB_USERNAME, SMB_PASSWORD);
        SmbFileOutputStream smbfos = null;
        try {
            SmbFile smbFile = new SmbFile(SMB_PATH + filename, auth);
            smbfos = new SmbFileOutputStream(smbFile);
        }
        catch (MalformedURLException | UnknownHostException | SmbException e) {
            LOGGER.warn("Failed creating remote file", e);
            throw e;
        }
        return smbfos;
    }
}
