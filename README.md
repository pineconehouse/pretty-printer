## Pretty record printer
> Using this class to pretty print the records, Rows will be output in column alignment.


```java
	public static void main(String[] args) {
		PrettyRecordPrinter print = new PrettyRecordPrinter();
//		print.printout();

		print.appendRecord("d", null, 123, 10.33, "KB");
		print.appendRecord(new Date(), "Author:Rison han");
		print.printout();

		print.println();

		Layout layout = new Layout();
//		layout.setPadTab(8); 
		layout.enableTopBorder('/', '~', '\\');
		layout.enableBottomBorder('\\', '_', '/');
		print.setLayout(layout);
		
//		print.setPrintMode(PrintMode.ELLIPSIS);
//		print.setPrintMode(PrintMode.CLASSIC);
		print.appendRecord("95683333333333", "poo", "o222222222222i");
		print.appendRecord("95668", "pooiuyttrt", "oi");
		print.appendRecord("www", new Date(), "ddddddddddddddd");
		print.appendCuttingLine('^');
		print.appendRecord("aaas", "sss");
		print.appendBlank();
		print.appendRecord("aaas", "s1111111111111sssssss1111ss");
		print.printout();

		print.println();

		print.getLayout().setAlign(Align.Right);
		print.getLayout().setNullAs("<NULL>");
		print.getLayout().setColumnSeparator(" , ");
		print.getLayout().disableBottomBorder();
		print.getLayout().disableLeftBorder();
		print.getLayout().disableRightBorder();
		print.getLayout().disableTopBorder();
		print.getLayout().setPadChar('.');
		print.appendRecord("95668", "pooiuyttrt", "oi");
		print.appendRecord(null, "95683333333333", "poo", "o222222222222i");
		print.appendBlanks(3);
		print.appendRecord("www", "ddddddddddddddd");
		print.appendCuttingLine();
		print.appendRecord("aaas", null, "s1111111111111sssssss1111ss");
		print.printout();

		print.println();

		print.getLayout().setNullAs("");
		print.appendRecord("ddd", null);
		print.printout();

		print.println();

		print.getLayout().setMarginLeft(20);
		print.getLayout().enableTopBorder('/', '~', '\\');
		print.getLayout().enableBottomBorder('\\', '_', '/');
		print.getLayout().disableLeftBorder();
		print.getLayout().setPadChar(' ');
		print.getLayout().setLeftBorder("(");
		print.getLayout().setRightBorder(")");
		print.getLayout().setColumnSeparator("|");
		print.appendRecord("@", "", "@");
		print.appendRecord(" ", "- -", " ");
		print.appendRecord(" ", " = ", " ");
		print.printout();

		print.println();

	}
```

![Output image!](https://github.com/pineconehouse/pretty/blob/main/output.png "Output")
