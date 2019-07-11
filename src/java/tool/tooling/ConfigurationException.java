package tool.tooling;

public class ConfigurationException extends ToolingException {
	private static final long serialVersionUID = 1L;

	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(String msg, Throwable t) {
		super(msg, t);
	}
}