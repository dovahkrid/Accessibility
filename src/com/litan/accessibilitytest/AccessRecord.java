package com.litan.accessibilitytest;

public interface AccessRecord {
    int getEventType();
    CharSequence getPkgName();
   String getViewResName();
   CharSequence getText();
}
