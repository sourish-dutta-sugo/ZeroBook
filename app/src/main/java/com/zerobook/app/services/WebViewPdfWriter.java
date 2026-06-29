package com.zerobook.app.services;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintResultCallbackBridge;
import android.webkit.WebView;

import java.io.File;

final class WebViewPdfWriter {

    interface Callback {
        void onSuccess();
        void onError(Throwable error);
    }

    private WebViewPdfWriter() {
    }

    static void writePdf(
            WebView webView,
            String jobName,
            PrintAttributes attributes,
            File outputFile,
            Callback callback
    ) {
        PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(jobName);
        adapter.onLayout(
                null,
                attributes,
                null,
                new PrintResultCallbackBridge.Layout() {
                    @Override
                    public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                        try {
                            ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(
                                    outputFile,
                                    ParcelFileDescriptor.MODE_CREATE
                                            | ParcelFileDescriptor.MODE_TRUNCATE
                                            | ParcelFileDescriptor.MODE_READ_WRITE
                            );
                            adapter.onWrite(
                                    new PageRange[]{PageRange.ALL_PAGES},
                                    descriptor,
                                    new CancellationSignal(),
                                    new PrintResultCallbackBridge.Write() {
                                        @Override
                                        public void onWriteFinished(PageRange[] pages) {
                                            closeQuietly(descriptor);
                                            callback.onSuccess();
                                        }

                                        @Override
                                        public void onWriteFailed(CharSequence error) {
                                            closeQuietly(descriptor);
                                            callback.onError(new IllegalStateException(
                                                    error != null ? error.toString() : "PDF write failed."
                                            ));
                                        }

                                        @Override
                                        public void onWriteCancelled() {
                                            closeQuietly(descriptor);
                                            callback.onError(new IllegalStateException("PDF write cancelled."));
                                        }
                                    }
                            );
                        } catch (Exception error) {
                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onLayoutFailed(CharSequence error) {
                        callback.onError(new IllegalStateException(
                                error != null ? error.toString() : "PDF layout failed."
                        ));
                    }

                    @Override
                    public void onLayoutCancelled() {
                        callback.onError(new IllegalStateException("PDF layout cancelled."));
                    }
                },
                null
        );
    }

    private static void closeQuietly(ParcelFileDescriptor descriptor) {
        try {
            descriptor.close();
        } catch (Exception ignored) {
        }
    }
}
