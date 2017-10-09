package util;

public class SystemPrinter {
	public static final void print(String print) {
		System.out.print(print);
	}
	public static final void println(String print) {
		SystemPrinter.print(print + "\n");
	}
}
