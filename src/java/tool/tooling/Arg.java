package tool.tooling;

public class Arg {
	private String name;
	private String value;

	public Arg(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}