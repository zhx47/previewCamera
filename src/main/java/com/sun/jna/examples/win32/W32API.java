package com.sun.jna.examples.win32;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

public interface W32API {
    HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(-1L));
    HWND HWND_BROADCAST = new HWND(Pointer.createConstant(65535L));

    class HANDLE extends PointerType {
        private boolean immutable;

        public HANDLE() {
        }

        public HANDLE(Pointer p) {
            this.setPointer(p);
            this.immutable = true;
        }

        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            return W32API.INVALID_HANDLE_VALUE.equals(o) ? W32API.INVALID_HANDLE_VALUE : o;
        }

        public void setPointer(Pointer p) {
            if (this.immutable) {
                throw new UnsupportedOperationException("immutable reference");
            } else {
                super.setPointer(p);
            }
        }
    }

    class HWND extends HANDLE {
        public HWND() {
        }

        public HWND(Pointer p) {
            super(p);
        }
    }
}
