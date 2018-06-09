/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsftpcred;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;

public class Jsftpcred {
    private static final String copyright = "Jsftpcred v1.0 - 09/06/2018 - Copyright rcamargo";
    /**
     * Path where public key file can be found most likely
     * ${HOME}/.ssh/id_rsa.pub
     */
    private String publicKeyPath;

    /**
     * Path where public key file can be found most likely ${HOME}/.ssh/id_rsa
     */
    private static String privateKeyPath = "";

    private static String privateKeyPassword = "";
    private static String sshLogin = "";
    private static String sshPassword = "";
    private static String localDir = "";
    private static String localFile = "";
    private static String remoteDir = "";
    private static String remoteFile = "";
    private static String sftpHost = "";
    private static int sshPort = 22;
    private static String sshType = "";
    private static String sshCommand = "";
    private static boolean _debug = false;

    /**
     * Constructor.
     *
     * @param args arguments array passed from command line.
     */
    public Jsftpcred() {
    }
    
    /**
     * Lists directory files on remote server.
     *
     * @throws JSchException
     * @throws SftpException
     */
    private void putFiles() throws JSchException, SftpException {
        
        if(!FileExist(localDir + "/" + localFile))
        {
            System.out.printf("File not Found [%s]\n",localDir + "/" + localFile);
            return;
        }
        
        JSch jsch = new JSch();
        JSch.setLogger(new jsftpcredLogger());
        setupSftpIdentity(jsch);

        Session session = jsch.getSession(sshLogin, sftpHost, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(sshPassword);
        session.connect();
        System.out.println("Connected to SFTP server on " + sftpHost );

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.lcd(localDir);
        System.out.printf("Local PWD : %s\n",sftpChannel.lpwd());
        sftpChannel.cd(remoteDir);
        System.out.printf("Remote PWD : %s\n", sftpChannel.pwd());
        try {
            sftpChannel.put(localFile,remoteFile);
        } catch (Exception exception) {
            System.out.println("Error on Put Command");
            exception.printStackTrace();
        }
        
        sftpChannel.exit();
        session.disconnect();
    }
    
    /**
     * Lists directory files on remote server.
     *
    * @throws JSchException
     * @throws SftpException
     */
    private void getFiles() throws JSchException, SftpException {
        JSch jsch = new JSch();
        JSch.setLogger(new jsftpcredLogger());
        setupSftpIdentity(jsch);
        
        if( _debug )
            System.out.printf("Remote Dir %s, Remote File %s, LocalDir %s, LocalFile %s, Type %s\n",remoteDir, remoteFile, localDir, localFile, sshType);

        Session session = jsch.getSession(sshLogin, sftpHost, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(sshPassword);
        session.connect();
        System.out.println("Connected to SFTP server on " + sftpHost );

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.lcd(localDir);
        System.out.printf("Local PWD : %s\n",sftpChannel.lpwd());
        sftpChannel.cd(remoteDir);
        System.out.printf("Remote PWD : %s\n", sftpChannel.pwd());
        try {
            sftpChannel.get(remoteFile,localFile);
        } catch (Exception exception) {
            System.out.println("Error on Get Command");
            exception.printStackTrace();
        }           
        sftpChannel.exit();
        session.disconnect();
        
        if(!FileExist(localDir + "/" + localFile))
        {
            System.out.printf("File not Found [%s]\n",localDir + "/" + localFile);
            return;
        }
        
    }    

    /**
     * Lists directory files on remote server.
     *
     * @throws JSchException
     * @throws SftpException
     */
    private void listFiles() throws JSchException, SftpException {
        JSch jsch = new JSch();
        JSch.setLogger(new jsftpcredLogger());
        setupSftpIdentity(jsch);

        Session session = jsch.getSession(sshLogin, sftpHost, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(sshPassword);
        session.connect();
        System.out.println("Connected to SFTP server on " + sftpHost );

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        Vector<LsEntry> directoryEntries = sftpChannel.ls(remoteDir);
        for (LsEntry file : directoryEntries) {
            System.out.println(String.format("File %-40.40s => %s", file.getFilename(), file.getLongname()));
        }
        sftpChannel.exit();
        session.disconnect();
    }

    private boolean FileExist( String fileName )
    {
        if( fileName == null || fileName.length() == 0) return false;
        File fileDescriptor = new File(fileName);    
        return fileDescriptor.exists();
    }
    
    private void setupSftpIdentity(JSch jsch) throws JSchException {
        byte[] privateKey = null;
        byte[] publicKey = null;
        byte[] passphrase = null;
        try {
            if(FileExist(privateKeyPath))
                privateKey = IOUtils.toByteArray(new FileInputStream(privateKeyPath));
            if(FileExist(publicKeyPath))
                publicKey = IOUtils.toByteArray(new FileInputStream(publicKeyPath));
            if(!privateKeyPassword.equals(""))
                passphrase = privateKeyPassword.getBytes();
//            jsch.addIdentity(sshLogin, privateKey, publicKey, passphrase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean JsftpcredOptions(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i].charAt(0)) {
                    case '-': {
                        switch (args[i].charAt(1)) {
                            case 'u': {
                                sshLogin = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'p': {
                                sshPassword = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'L': {
                                localDir = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'R': {
                                remoteDir = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'l': {
                                localFile = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'r': {
                                remoteFile = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'I': {
                                sftpHost = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'P': {
                                sshPort = Integer.parseInt(args[i].substring(2, args[i].length()));
                                break;
                            }
                            case 't': {
                                sshType = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'c': {
                                sshCommand = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'a': {
                                publicKeyPath = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'b': {
                                privateKeyPath = args[i].substring(2, args[i].length());
                                break;
                                }
                            case 'k': {
                                privateKeyPassword = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'v': {
                                _debug = true;
                                break;
                            }
                            default: {
                                System.err.print("Unknown Parameter !!!\n");
                                return false;
                            }
                        }
                    }
                    if (_debug) {
                        System.err.println("Parameters [" + i + "] " + args[i]);
                    }
                }
            }
        } else {
            return false;
        }
        
        if(_debug)
            System.out.printf("Login %s, Password %s, Host %s:%s, RemoteDir %s\n",sshLogin,sshPassword,sftpHost,sshPort,remoteDir );

        if (args.length == 0 || sshLogin.equals("") || sftpHost.equals("") || sshCommand.equals("") || remoteDir.equals(""))
            return false;
        
        if ( !sshCommand.equals("put") && !sshCommand.equals("get") && !sshCommand.equals("list"))
            return false;
        
        if ( !sshType.equals("ascii") && !sshType.equals("binary") && !sshType.equals(""))
            return false;
        
        if (( sshCommand.equals("put") || sshCommand.equals("get")) &&
            (remoteDir.equals("") || remoteFile.equals("") || localDir.equals("") || localFile.equals("") || sshType.equals("")))
            return false;

        return true;
    }

    public static void Sintaxe() {
        System.err.print("Sintaxe : JsftpCred -u<User> -p<password> -L<LocalDir> -l<localfile> -R<RemoteDir> -r<remotefile> -I<IP> -P<Port> -t<Type> -c<Command> -a<PublicKeyPath> -b<privateKeyPath> -k<privateKeyPassword> -v\n");
        System.err.print("  -u<User>               - Remote User\n");
        System.err.print("  -p<password>           - Remote User Password\n");
        System.err.print("  -L<LocalDir>           - Local Directory\n");
        System.err.print("  -l<localfile>          - Local File\n");
        System.err.print("  -R<RemoteDir>          - Remote Directory\n");
        System.err.print("  -r<remotefile>         - Remote File\n");
        System.err.print("  -I<IP>                 - Remote IP Address\n");
        System.err.print("  -P<Port>               - Remote Port (default 22)\n");
        System.err.print("  -t<Type>               - Type of File Transfer (ascii or binary)\n");
        System.err.print("  -c<Command>            - Command (put, get or list)\n");
        System.err.print("  -a<PublicKeyPath>      - Public Key File\n");
        System.err.print("  -b<privateKeyPath>     - Private Key File\n");
        System.err.print("  -k<privateKeyPassword> - PrivateKeyPassword\n");
        System.err.print("  -V                     - Verbose (debug info)\n");
    }
    
    /**
     * Main method
     *
     * @param args arguments passed through commandline.
     */
    public static void main(String[] args) throws Exception {

        System.out.println(copyright);
        Jsftpcred main = new Jsftpcred();
        if (main.JsftpcredOptions(args)) {
            if (sshCommand.equals("list")) 
                main.listFiles();
            if (sshCommand.equals("get")) 
                main.getFiles();  
            if (sshCommand.equals("put")) 
                main.putFiles();
        } else {
            main.Sintaxe();
            System.exit(1);
        }
    }
}
