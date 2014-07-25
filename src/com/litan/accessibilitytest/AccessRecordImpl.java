package com.litan.accessibilitytest;

public class AccessRecordImpl implements AccessRecord {
    int mEventType;
    String mViewResName;
    CharSequence mText;
    CharSequence mPkgName;
    @Override
    public String getViewResName() {
        return mViewResName;
    }

    @Override
    public CharSequence getText() {
        return mText;
    }

    @Override
    public CharSequence getPkgName() {
        return mPkgName;
    }

    @Override
    public int getEventType() {
        return mEventType;
    }
    
}
