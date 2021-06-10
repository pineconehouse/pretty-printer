/*                                                                             
 * Copyright (C) 2019 Rison Han                                     
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");           
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,         
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */                                                                            
package com.example.application;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Print records in formatted for readability
 * 
 * @author sohan
 */
public class PrettyRecordPrinter {
	private class ColumnLayout {
		private int maxWidth = 0;
		// public int right = 0;
	}

	public static class Layout {
		private String padChar = " ";
		private String columnSeparator = " | ";
		private char cuttingLineChar = '-';
		private char[] topBorder = new char[] { '+', '-', '+' };
		private String leftBorder = "| ";
		private String rightBorder = " |";
		private char[] bottomBorder = new char[] { '+', '-', '+' };

		private int marginLeft = 0;

		private Align alignMode = Align.Left;
		private int tabLength = 8;
		private String nullString = "";

		public String getColumnSeparator() {
			return columnSeparator;
		}

		public void setColumnSeparator(String columnSeparator) {
			this.columnSeparator = columnSeparator;
		}

		public char getPadChar() {
			return padChar.charAt(0);
		}

		public void setPadChar(char padChar) {
			if (padChar != '\t') {
				this.padChar = String.valueOf(padChar);
			} else {
				setPadTab(8);
			}
		}

		public void setPadTab(int tabLength) {
			if (tabLength <= 0) {
				tabLength = 1;
			}
			this.tabLength = tabLength;
			this.padChar = String.valueOf('\t');
		}

		public void setNullAs(String nullString) {
			if (nullString == null) {
				nullString = "";
			}

			this.nullString = nullString;
		}

		public char getCuttingLineChar() {
			return cuttingLineChar;
		}

		public void setCuttingLineChar(char cuttingLineChar) {
			this.cuttingLineChar = cuttingLineChar;
		}

		public int getMarginLeft() {
			return marginLeft;
		}

		public void setMarginLeft(int marginLeft) {
			this.marginLeft = marginLeft;
		}

		public Align getAlign() {
			return alignMode;
		}

		public void setAlign(Align alignMode) {
			this.alignMode = alignMode;
		}

		public void enableTopBorder(char topLeftCorner, char topBorder, char topRightCorner) {
			this.topBorder = new char[3];
			this.topBorder[0] = topLeftCorner;
			this.topBorder[1] = topBorder;
			this.topBorder[2] = topRightCorner;
		}

		public void enableBottomBorder(char bottomLeftCorner, char bottomBorder, char bottomRightCorner) {
			this.bottomBorder = new char[3];
			this.bottomBorder[0] = bottomLeftCorner;
			this.bottomBorder[1] = bottomBorder;
			this.bottomBorder[2] = bottomRightCorner;
		}

		public void disableTopBorder() {
			this.topBorder = null;
		}

		public void disableBottomBorder() {
			this.bottomBorder = null;
		}

		public void disableLeftBorder() {
			this.leftBorder = "";
		}

		public void disableRightBorder() {
			this.rightBorder = "";
		}

		public String getLeftBorder() {
			return leftBorder;
		}

		public void setLeftBorder(String leftBorder) {
			this.leftBorder = leftBorder;
		}

		public String getRightBorder() {
			return rightBorder;
		}

		public void setRightBorder(String rightBorder) {
			this.rightBorder = rightBorder;
		}
	}

	private class Record {
		public String[] record = null;
		public RecordFormatter recordFormatter = null;
		public RecordDesc filler = null;

		public Record(String[] record, RecordFormatter recordFormatter, RecordDesc filler) {
			this.record = record;
			this.recordFormatter = recordFormatter;
			this.filler = filler;
		}

		public String toString() {
			return recordFormatter.toLine(record, filler);
		}
	}

	private interface RecordFormatter {
		public String toLine(String[] record, RecordDesc filler);
	}

	private interface RecordDesc {
		public String fillChar(String str, String padChar, int maxLength, Align align);

		public String fillSp(String padChar, int maxLength);

		public int getLineLength();

		public int getLineLengthWithBorder();
	}

	// private interface SepararorDesc {
	// }

	public static enum PrintMode {
		ELLIPSIS, 
//		TAB,
		CLASSIC
	}

	public static enum Align {
		Right, Left
	}

	private Layout layout = new Layout();

	private final Map<String, String> cachePadStringMap = new HashMap<String, String>();
	private final Map<Integer, ColumnLayout> cacheColumnLayoutMap = new HashMap<Integer, ColumnLayout>();
	private final List<Record> recordList = new ArrayList<Record>();
	private PrintStream out = System.out;

	private PrintMode printMode = PrintMode.CLASSIC;

	private RecordFormatter defaultRecordFormatter;
	private RecordDesc defaultLineDesc;

	private final RecordFormatter ellipsisRecordFormatter = new RecordFormatter() {
		StringBuilder buf = new StringBuilder();

		public String toLine(String[] record, RecordDesc filler) {
			buf.setLength(0);
			if (layout.marginLeft > 0) {
				buf.append(getPadString(" ", layout.marginLeft));
			}
			buf.append(filler.fillChar(layout.leftBorder + record[0], layout.padChar, cacheColumnLayoutMap.get(0).maxWidth, layout.alignMode));
			for (int i = 1; i < record.length; i++) {
				buf.append(filler.fillChar(layout.columnSeparator + record[i], layout.padChar, cacheColumnLayoutMap.get(i).maxWidth, layout.alignMode));
			}
			buf.append(layout.rightBorder);

			return buf.toString();
		}
	};

	private final RecordFormatter classicRecordFormatter = new RecordFormatter() {
		StringBuilder buf = new StringBuilder();

		public String toLine(String[] record, RecordDesc filler) {
			buf.setLength(0);
			if (layout.marginLeft > 0) {
				buf.append(getPadString(" ", layout.marginLeft));
			}
			buf.append(layout.leftBorder);
			buf.append(filler.fillChar(record[0], layout.padChar, cacheColumnLayoutMap.get(0).maxWidth, layout.alignMode));
			for (int i = 1; i < record.length; i++) {
				buf.append(layout.columnSeparator);
				buf.append(filler.fillChar(record[i], layout.padChar, cacheColumnLayoutMap.get(i).maxWidth, layout.alignMode));
			}
			int size = cacheColumnLayoutMap.size();
			for (int i = record.length; i < size; i++) {
				buf.append(layout.columnSeparator);
				buf.append(filler.fillChar("", layout.padChar, cacheColumnLayoutMap.get(i).maxWidth, layout.alignMode));
			}
			buf.append(layout.rightBorder);

			return buf.toString();
		}
	};

	private final RecordFormatter classicTabRecordFormatter = new RecordFormatter() {
		StringBuffer buf = new StringBuffer();

		public String toLine(String[] record, RecordDesc filler) {
			buf.setLength(0);
			if (layout.marginLeft > 0) {
				buf.append(getPadString(" ", layout.marginLeft));
			}
			buf.append(filler.fillChar(layout.leftBorder + record[0], layout.padChar, cacheColumnLayoutMap.get(0).maxWidth + layout.leftBorder.length(), layout.alignMode));
			int spLen = layout.columnSeparator.length();
			for (int i = 1; i < record.length; i++) {
				String value = (record[i] == null ? layout.nullString : record[i]);
				buf.append(filler.fillChar(layout.columnSeparator + value, layout.padChar, cacheColumnLayoutMap.get(i).maxWidth + spLen, layout.alignMode));
			}
			int size = cacheColumnLayoutMap.size();
			for (int i = record.length; i < size; i++) {
				buf.append(layout.columnSeparator);
				buf.append(filler.fillChar("", layout.padChar, cacheColumnLayoutMap.get(i).maxWidth + spLen, layout.alignMode));
			}
			buf.append(layout.rightBorder);

			return buf.toString();
		}
	};

	private final RecordFormatter cuttingLineFormatter = new RecordFormatter() {
		public String toLine(String[] record, RecordDesc desc) {
			int recordLength = desc.getLineLength();
			StringBuffer buf = new StringBuffer();
			if (layout.marginLeft > 0) {
				buf.append(getPadString(" ", layout.marginLeft));
			}
			buf.append(layout.leftBorder);
			buf.append(desc.fillSp(record[0], recordLength));
			buf.append(layout.rightBorder);
			return buf.toString();
		}
	};

	private final RecordFormatter tbBorderPrinter = new RecordFormatter() {
		public String toLine(String[] record, RecordDesc desc) {
			int recordLength = desc.getLineLengthWithBorder() - 2;
			StringBuffer buf = new StringBuffer();
			if (layout.marginLeft > 0) {
				buf.append(getPadString(" ", layout.marginLeft));
			}
			buf.append(record[0]);
			buf.append(desc.fillSp(record[1], recordLength));
			buf.append(record[2]);
			return buf.toString();
		}
	};

	private final RecordDesc charPadDesc = new RecordDesc() {
		public String fillChar(String itemValue, String padChar, int maxLength, Align align) {

			int padLen = (maxLength - itemValue.length());
			String temp = getPadString(padChar, padLen);

			if (align == Align.Right) {
				return temp + itemValue;
			} else {
				return itemValue + temp;
			}
		}

		public String fillSp(String padChar, int maxLength) {
			return getPadString(padChar, maxLength);
		}

		public int getLineLength() {
			int recordLength = 0;
			Collection<ColumnLayout> all = cacheColumnLayoutMap.values();
			for (ColumnLayout fmt : all) {
				recordLength += fmt.maxWidth;
			}
			recordLength += (layout.columnSeparator.length() * all.size()) - layout.columnSeparator.length();

			return recordLength;
		}

		public int getLineLengthWithBorder() {
			int recordLength = getLineLength();
			recordLength += layout.leftBorder.length();
			recordLength += layout.rightBorder.length();

			return recordLength;
		}
	};

	private final RecordDesc tabPadDesc = new RecordDesc() {
		public String fillChar(String itemValue, String padChar, int maxLength, Align align) {

			if (maxLength <= 0 || layout.tabLength <= 0) {
				return itemValue;
			}

			// length += columnSeparator.length();
			int strlength = itemValue.length(); // +columnSeparator.length();
			int totalTabCnt = ((int) (maxLength / layout.tabLength)) + (maxLength % layout.tabLength == 0 ? 0 : 1);
			int strTabCnt = ((int) (strlength / layout.tabLength));
			int padLen = totalTabCnt - strTabCnt;
			String temp = getPadString(padChar, padLen);

			if (align == Align.Right) {
				return temp + itemValue;
			} else {
				return itemValue + temp;
			}
		}

		public String fillSp(String padChar, int maxLength) {
			return getPadString(padChar, maxLength);
		}

		public int getLineLength() {
			int recordLength = 0;

			ColumnLayout fmt = cacheColumnLayoutMap.get(0);
			int maxWidth = 0;
			if (fmt != null) {
				maxWidth = fmt.maxWidth;
			}
			int maxLen = maxWidth + layout.leftBorder.length();
			recordLength += (((int) (maxLen / layout.tabLength)) + (maxLen % layout.tabLength == 0 ? 0 : 1)) * layout.tabLength;

			int maxcol = cacheColumnLayoutMap.size();
			for (int i = 1; i < maxcol; i++) {
				fmt = cacheColumnLayoutMap.get(i);
				maxLen = fmt.maxWidth + layout.columnSeparator.length();
				recordLength += (((int) (maxLen / layout.tabLength)) + (maxLen % layout.tabLength == 0 ? 0 : 1)) * layout.tabLength;
			}

			return recordLength - layout.leftBorder.length();
		}

		public int getLineLengthWithBorder() {
			int recordLength = getLineLength();
			recordLength += layout.leftBorder.length();
			recordLength += layout.rightBorder.length();

			return recordLength;
		}
	};

	public PrettyRecordPrinter() {
		this.defaultLineDesc = charPadDesc;
		this.defaultRecordFormatter = classicRecordFormatter;

//				this.defaultLineDesc = tabPadDesc;
//		this.defaultRecordFormatter = classicRecordFormatter;

		this.setPrintMode(PrintMode.CLASSIC);
//		this.getLayout().setPadTab(8);
	}

	private String getPadString(String padChar, int length) {
		String padString = cachePadStringMap.get(padChar + length);
		if (padString == null) {
			String temp = "";
			for (int i = 0; i < length; i++) {
				temp += padChar;
			}

			padString = temp;
			cachePadStringMap.put(padChar + length, padString);
		}

		return padString;
	}

	// private float getStringWidth(String str) {
	// static final Font TEXT_FONT = new Font("MS Sans Serif", java.awt.Font.PLAIN, 12);
	// FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(TEXT_FONT);
	// fm.stringWidth(str);
	//
	// return size.Width;
	// }

	public void appendRecord(Object... objs) {
		String[] strs = new String[objs.length];
		for (int i = 0; i < strs.length; i++) {
			if (objs[i] != null) {
				strs[i] = objs[i].toString();
			}
		}

		appendRecord(strs);
	}

	public void appendTitle(String... titles) {
		appendRecord(titles);
	}

	public void appendRecord(String... items) {
		if (items == null) {
			items = new String[] { layout.nullString };
		}

		recordList.add(new Record(items, defaultRecordFormatter, defaultLineDesc));

		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			ColumnLayout fmt = cacheColumnLayoutMap.get(i);
			if (fmt == null) {
				fmt = new ColumnLayout();
				cacheColumnLayoutMap.put(i, fmt);
			}

			if (item == null) {
				items[i] = layout.nullString;
				item = layout.nullString;
			}

			int len = item.length();
			if (fmt.maxWidth < (len)) {
				fmt.maxWidth = (len);
			}
			// if (fmt.maxWidth < (len + columnSeparator.length())) {
			// fmt.maxWidth = (len + columnSeparator.length());
			// }
		}
	}

	public void appendBlank() {
		this.appendRecord(new String[] { "" });
	}

	public void appendBlanks(int count) {
		for (int i = 0; i < count; i++) {
			this.appendBlank();
		}
	}

	public void appendCuttingLine() {
		appendCuttingLine(layout.cuttingLineChar);
	}

	public void appendCuttingLine(char separatorChar) {
		recordList.add(new Record(new String[] { String.valueOf(separatorChar) }, cuttingLineFormatter, defaultLineDesc));
	}

	public void printout() {
		if (layout.topBorder != null) {
			String[] record = new String[] { String.valueOf(layout.topBorder[0]), String.valueOf(layout.topBorder[1]), String.valueOf(layout.topBorder[2]) };
			out.println(new Record(record, tbBorderPrinter, defaultLineDesc));
		}

		for (Record record : recordList) {
			out.println(record);
		}

		if (layout.bottomBorder != null) {
			String[] record = new String[] { String.valueOf(layout.bottomBorder[0]), String.valueOf(layout.bottomBorder[1]), String.valueOf(layout.bottomBorder[2]) };
			out.println(new Record(record, tbBorderPrinter, defaultLineDesc));
		}

		recordList.clear();
		cacheColumnLayoutMap.clear();
	}

	public void println() {
		out.println();
	}

	public void println(String string) {
		out.println(string);
	}

	public void print(String string) {
		out.print(string);
	}

	public void setOutputStream(OutputStream out) {
		this.out = new PrintStream(out);
	}

	public void setPrintStream(PrintStream out) {
		this.out = out;
	}

	public PrintStream getPrintStream() {
		return out;
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public PrintMode getPrintMode() {
		return printMode;
	}

	public void setPrintMode(PrintMode printMode) {
		this.printMode = printMode;
		if (printMode == PrintMode.ELLIPSIS) {
			this.defaultRecordFormatter = ellipsisRecordFormatter;
		} else {
			this.defaultRecordFormatter = classicRecordFormatter;
		}
	}

	public static void main(String[] args) {
		PrettyRecordPrinter print = new PrettyRecordPrinter();
//		print.printout();

		print.appendRecord("d", null, 123, 10.33, "KB");
		print.appendRecord(new Date(), "Author:Rison han");
		print.printout();

		print.println();

		Layout layout = new Layout();
//		layout.setPadTab(8); //Bug
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

}
