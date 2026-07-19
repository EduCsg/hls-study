package hls_study.api.enums;

public enum VideoQuality {
	P480("480p"), P1080("1080p");

	private final String value;

	VideoQuality(String s) {
		this.value = s;
	}

	public String getValue() {
		return value;
	}
}