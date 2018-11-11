package calypte.stream;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamManager {

	int getBlockSize();
	
	OutputStream createOutputStream();
	
	InputStream getInputStream(long offset);
	
}
