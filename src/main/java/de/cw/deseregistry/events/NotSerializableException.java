package de.cw.deseregistry.events;

import java.io.IOException;

public class NotSerializableException extends IOException
{
	public NotSerializableException (String exc)
	{
		super (exc);
	}

}
