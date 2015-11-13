package com.goodgame.profiling.commons.systems.net.socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocketServer;

public class UnixServerSocket implements ServerSocketHolder  {
    private static final Logger log = LogManager.getLogger();

	UnixDomainSocketServer unixServerSocket;
	File unixSocketFile;
	String unixSocketPath;
	
	public UnixServerSocket( String unixSocketPath, int connectionBacklog, String judsLibraryPath, JSONObject permissions ) throws IOException {
		this.unixSocketPath = unixSocketPath;
		this.unixSocketFile = new File(unixSocketPath);
		if ( unixSocketFile.exists() ){
			log.info("UnixSocketPath " + unixSocketPath + " already exists. Deleted that file and create a new one!");
			unixSocketFile.delete();
		}
		if (judsLibraryPath.equals(null) || judsLibraryPath.equals("")){
			log.error("Tried to create unix server socket but got illegal value of judsLibraryPath: " + judsLibraryPath);
		}
		unixServerSocket = new UnixDomainSocketServer(unixSocketPath, JUDS.SOCK_STREAM, connectionBacklog, judsLibraryPath);
		
		setPermissions( permissions );
	}
	
	public UnixServerSocket( String unixSocketPath, String judsLibraryPath, JSONObject permissions  ) throws IOException {
		this.unixSocketPath = unixSocketPath;
		this.unixSocketFile = new File(unixSocketPath);
		if ( unixSocketFile.exists() ){
			log.info("UnixSocketPath " + unixSocketPath + " already exists. Deleted that file and create a new one!");
			unixSocketFile.delete();
		}
		if (judsLibraryPath.equals(null) || judsLibraryPath.equals("")){
			log.error("Tried to create unix server socket but got illegal value of judsLibraryPath: " + judsLibraryPath);
		}
		unixServerSocket = new UnixDomainSocketServer(unixSocketPath, JUDS.SOCK_STREAM, judsLibraryPath);

		setPermissions( permissions );
	}
	
	private void setPermissions( JSONObject permissions ) {
        log.info( "Set permissions for unixSocket '{}'", unixSocketPath );
        String user = ( permissions.isNull("user") ? null : permissions.getString("user") );
        String group = ( permissions.isNull("group") ? null : permissions.getString("group") );
        String rwx = ( permissions.isNull("rwx") ? null : permissions.getString("rwx") );
        log.info( "New permissions: user = '{}', group = '{}', rwx = '{}'", Objects.toString(user), Objects.toString(group), Objects.toString(rwx) );
        
        Path path = null;
        PosixFileAttributeView view = null;
        UserPrincipalLookupService lookup = null;
        try {
        	path = FileSystems.getDefault().getPath(unixSocketPath);
   		    view = Files.getFileAttributeView( path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS );
   		    lookup = path.getFileSystem().getUserPrincipalLookupService();
        } catch ( InvalidPathException e ) {
			log.error("The given unixSocketPath '" + unixSocketPath + "' is invalid and throws exception: ", e);
        } catch ( UnsupportedOperationException e ) {
        	log.error("Failed to get UserPrincipalLookupService from unixSocketPath '" + unixSocketPath + "'. Got exception: ", e);
        }
		
        if ( view != null && lookup != null ) {
        	if ( user != null && !user.equals("")) {
    			try{
    				log.debug( "Set user to: " + user );
    				view.setOwner( lookup.lookupPrincipalByName( user ) );
    			} catch ( IOException e ) {
    				log.error("Couldn't set owner to '" + user + "'. Got exception: ", e);
    			}
    		}
        	if ( group != null && !group.equals("") ) {
    			try{
    				log.debug("Set group to: " + group );
    				view.setGroup( lookup.lookupPrincipalByGroupName( group ) );
    			} catch ( IOException e ) {
    				log.error("Couldn't set group to '" + group + "'. Got exception: ", e);
    			}
    		}
        	if ( rwx != null && !rwx.equals("")  ) {
        		try{
    				log.debug("Set read/write/executable-Permissions to: " + rwx );
    				Set<PosixFilePermission> perms = PosixFilePermissions.fromString(rwx);
    				Files.setPosixFilePermissions( path, perms );
    			} catch ( IOException e ) {
    				log.error("Couldn't set read/write/executable-Permissions to '" + rwx + "'. Got exception: ", e);
    			}
        	}
        }
	}
	
	@Override
	public ReadWriteableClientSocket accept() throws IOException {
		UnixDomainSocket client = unixServerSocket.accept();
		return new UnixClientSocketWrapper( client );
	}

	@Override
	public void close() throws IOException {
		unixServerSocket.close();
		if ( unixSocketFile.exists() ){
			unixSocketFile.delete();
		}
	}
	
	private class UnixClientSocketWrapper implements ReadWriteableClientSocket {
		UnixDomainSocket client;
		boolean isClosed;

		public UnixClientSocketWrapper( UnixDomainSocket client ) {
			this.client = client;
			this.isClosed = false;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return client.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return client.getOutputStream();
		}

		@Override
		public void close() throws IOException {
			client.close();
			isClosed = true;
		}

		@Override
		public String getTypeWithInfo() {
			return "UnixClientSocket";
		}

		@Override
		public boolean isClosed() {
			return isClosed;
		}
		
	}

}
