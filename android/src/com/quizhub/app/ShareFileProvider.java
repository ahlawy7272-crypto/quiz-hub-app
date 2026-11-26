package com.quizhub.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;

/** Serves the rendered share image from the app cache to the system share
 *  sheet. Framework-only (no androidx): only the exact file written by
 *  ShareCard is served, nothing else. */
public final class ShareFileProvider extends ContentProvider {
    public static final String AUTHORITY = "com.quizhub.app.sharefile";

    public static Uri uriFor(android.content.Context c, File f) {
        return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath(f.getName()).build();
    }

    @Override public boolean onCreate() { return true; }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws java.io.FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null || !name.equals(ShareCard.FILE_NAME)) throw new java.io.FileNotFoundException(uri.toString());
        File f = new File(getContext().getCacheDir(), "share/" + name);
        if (!f.exists()) throw new java.io.FileNotFoundException(uri.toString());
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public String getType(Uri uri) { return "image/png"; }
    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { return null; }
    @Override public int delete(Uri u, String s, String[] a) { return 0; }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }
}
