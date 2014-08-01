package com.litan.accessibilitytest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Rect;
import android.os.Environment;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Xml;
import android.view.accessibility.AccessibilityEvent;

public class AccessRecordXmlWriter {
	public static final String TAG_RECORDS = "records";
	public static final String ATTR_PKG = "records-pkg";

	public static final String TAG_RECORD = "record";
	public static final String ATTR_RES = "res";
	public static final String ATTR_TEXT = "txt";
	public static final String ATTR_W_INDEX = "w-index";
	public static final String ATTR_RECT = "rect";

	public static void init(Map<String, LinkedList<AccessRecord>> map) {
		File file = new File(Environment.getExternalStorageDirectory(),
				"Accessibility");
		if (!file.exists()) {
			return;
		}
		File[] files = file.listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		XmlPullParser parser = Xml.newPullParser();
		try {
			final SimpleStringSplitter splitter = new SimpleStringSplitter(',');
			for (File f : files) {
				String pkg = "";
				parser.setInput(new FileInputStream(f), "utf-8");
				AccessRecordImpl a = null;
				LinkedList<AccessRecord> list = null;
				int eventType = parser.next();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						String name = parser.getName();
						if (TAG_RECORDS.equals(name)) {
							pkg = parser.getAttributeValue(null, ATTR_PKG);
							list = new LinkedList<AccessRecord>();
						} else if (TAG_RECORD.equals(name)) {
							a = new AccessRecordImpl();
							a.mPkgName = pkg;
							a.mEventType = AccessibilityEvent.TYPE_VIEW_CLICKED;
							a.mText = parser.getAttributeValue(null, ATTR_TEXT);
							a.mViewResName = parser.getAttributeValue(null,
									ATTR_RES);
							a.mWindowIndex = Integer.valueOf(parser
									.getAttributeValue(null, ATTR_W_INDEX));
							String rect = parser.getAttributeValue(null,
									ATTR_RECT);
							splitter.setString(rect);
							int index = 0;
							Rect bounds = new Rect();
							while (splitter.hasNext()) {
								if (index == 0) {
									bounds.left = Integer.valueOf(splitter
											.next());
								} else if (index == 1) {
									bounds.top = Integer.valueOf(splitter
											.next());
								} else if (index == 2) {
									bounds.right = Integer.valueOf(splitter
											.next());
								} else if (index == 3) {
									bounds.bottom = Integer.valueOf(splitter
											.next());
								}
								index++;
							}
							a.mBoundsInScreen = bounds;
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						if (TAG_RECORD.equals(parser.getName())) {
							if (a != null) {
								list.add(a);
							}
						} else if (TAG_RECORDS.equals(parser.getName())) {
							map.put(pkg, list);
						}
					}

					eventType = parser.next();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void build(String pkgName, List<AccessRecord> records) {
		if (pkgName == null || records == null || records.isEmpty()) {
			AccessibilityTestService.loge("build:Invalid args");
			return;
		}
		XmlSerializer serializer = Xml.newSerializer();
		File file = new File(Environment.getExternalStorageDirectory(),
				"Accessibility");
		if (!file.exists()) {
			file.mkdirs();
		}
		try {
			File writerfile = new File(file, pkgName);
			FileWriter fileWriter = new FileWriter(writerfile);
			serializer.setOutput(fileWriter);
			serializer.startDocument("utf-8", true);
			serializer.startTag(null, TAG_RECORDS);
			serializer.attribute(null, ATTR_PKG, pkgName);
			for (AccessRecord record : records) {
				serializer.startTag(null, TAG_RECORD);
				String res = record.getViewResName();
				CharSequence text = record.getText();
				int wIndex = record.getWindowIndex();
				Rect rect = record.getBoundsInScreen();
				if (res != null) {
					serializer.attribute(null, ATTR_RES, res);
				}
				if (text != null) {
					serializer.attribute(null, ATTR_TEXT, text.toString());
				}
				serializer
						.attribute(null, ATTR_W_INDEX, String.valueOf(wIndex));
				StringBuilder sb = new StringBuilder();
				sb.append(rect.left).append(",").append(rect.top).append(",")
						.append(rect.right).append(",").append(rect.bottom);
				serializer.attribute(null, ATTR_RECT, sb.toString());
				serializer.endTag(null, TAG_RECORD);
			}
			serializer.endTag(null, TAG_RECORDS);
			serializer.endDocument();
			fileWriter.close();
			AccessibilityTestService.logv("writerFile:" + writerfile + " "
					+ writerfile.length());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
