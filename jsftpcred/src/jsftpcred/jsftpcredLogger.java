package jsftpcred;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jcraft.jsch.Logger;

/**
 *
 * @author renat
 */
public class jsftpcredLogger implements Logger {

	public boolean isEnabled(int arg0) {
		return true;
	}

	public void log(int arg0, String arg1) {
		System.out.println(String.format("[SFTP/SSH -> %s]", arg1));
	}
}

