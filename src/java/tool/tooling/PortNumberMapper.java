package tool.tooling;

import java.io.IOException;
import java.net.ServerSocket;


public class PortNumberMapper implements TypeMapper {
	public static final String AUTO = "auto";
	public static final String NONE = "none";

	private static int findFreePort() throws IOException {
		// http://chaoticjava.com/posts/retrieving-a-free-port-for-socket-binding/
	  ServerSocket server = new ServerSocket(0);
	  int port = server.getLocalPort();
	  server.close();
	  return port;
	}


	@Override
	public Object map(String str, Class clazz) throws TypeMappingException {
		if (NONE.equals(str)) {
			return null;
		} else if (AUTO.equals(str)) {
			try {
				return findFreePort();
			} catch (IOException exc) {
				throw new TypeMappingException(exc);
			}
		} else {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException exc) {
				throw new TypeMappingException(clazz, str);
			}
		}
	}


	@Override
	public String supportedValues(Class clazz) {
		return String.format("Число, либо '%s', либо '%s'", AUTO, NONE);
	}

}
