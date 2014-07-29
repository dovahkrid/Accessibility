/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/litan/workspace/Accessibility/src/com/litan/accessibilitytest/IAccessTestService.aidl
 */

package com.litan.accessibilitytest;

import android.os.RemoteException;

/**
 * Interface a client of the IAccessibilityManager implements to receive
 * information about changes in the manager state.
 * 
 * @hide
 */
public interface IAccessTestService extends android.os.IInterface
{
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements
            com.litan.accessibilitytest.IAccessTestService
    {
        private static final java.lang.String DESCRIPTOR = "com.litan.accessibilitytest.IAccessTestService";

        /** Construct the stub at attach it to the interface. */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an
         * com.litan.accessibilitytest.IAccessTestService interface, generating
         * a proxy if needed.
         */
        public static com.litan.accessibilitytest.IAccessTestService asInterface(
                android.os.IBinder obj)
        {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof com.litan.accessibilitytest.IAccessTestService))) {
                return ((com.litan.accessibilitytest.IAccessTestService) iin);
            }
            return new com.litan.accessibilitytest.IAccessTestService.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder()
        {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply,
                int flags) throws android.os.RemoteException
        {
            switch (code)
            {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_startRecord: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.startRecord(_arg0);
                    return true;
                }
                case TRANSACTION_getRecordedPkg: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<java.lang.String> _result = this.getRecordedPkg();
                    reply.writeStringList(_result);
                    return true;
                }
                case TRANSACTION_startPerform: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.startPerform(_arg0);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements com.litan.accessibilitytest.IAccessTestService
        {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote)
            {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder()
            {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor()
            {
                return DESCRIPTOR;
            }

            @Override
            public void startRecord(java.lang.String pkg) throws android.os.RemoteException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(pkg);
                    mRemote.transact(Stub.TRANSACTION_startRecord, _data, null,
                            android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }

            @Override
            public java.util.List<java.lang.String> getRecordedPkg()
                    throws android.os.RemoteException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                java.util.List<java.lang.String> _result = null;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getRecordedPkg, _data, null,
                            android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void startPerform(String pkg) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(pkg);
                    mRemote.transact(Stub.TRANSACTION_startPerform, _data, null,
                            android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_startRecord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getRecordedPkg = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_startPerform = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    }

    public void startRecord(java.lang.String pkg) throws android.os.RemoteException;

    public java.util.List<java.lang.String> getRecordedPkg() throws android.os.RemoteException;
    
    public void startPerform(String pkg) throws android.os.RemoteException;
}
