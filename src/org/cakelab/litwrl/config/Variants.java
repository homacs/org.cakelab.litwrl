package org.cakelab.litwrl.config;
public enum Variants {
	BASIC("Basic"),
	HUNGRY("Hungry");
	private String name;

	Variants(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public static Variants get(String name) {
		for (Variants v : Variants.values()) {
			if (name.equals(v.toString())) return v;
		}
		throw new IllegalArgumentException("No enum constant found for '" + name +'"');
	}
}
