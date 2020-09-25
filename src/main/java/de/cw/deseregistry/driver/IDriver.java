package de.cw.deseregistry.driver;

import java.io.File;
import java.io.IOException;

import de.cw.deseregistry.driver.IBackCaller;

public interface IDriver {

	public void addJarToProcess (File file);
	public void setBackCaller (IBackCaller vif);
	public void go () throws IOException;

}