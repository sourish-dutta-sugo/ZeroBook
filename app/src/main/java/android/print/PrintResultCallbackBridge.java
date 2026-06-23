package android.print;

import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;

/**
 * Bridge class to allow instantiation of {@link LayoutResultCallback} and {@link WriteResultCallback}
 * which have package-private constructors in newer Android SDKs.
 */
public abstract class PrintResultCallbackBridge {
    public static abstract class Layout extends LayoutResultCallback {
        public Layout() {
            super();
        }
    }

    public static abstract class Write extends WriteResultCallback {
        public Write() {
            super();
        }
    }
}
