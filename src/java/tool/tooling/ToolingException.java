package tool.tooling;

public class ToolingException extends Exception {
	private static final long serialVersionUID = 1L;

	public ToolingException() {
		super();
	}

	public ToolingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ToolingException(String message) {
		super(message);
	}

	public ToolingException(Throwable cause) {
		super(cause);
	}

}
