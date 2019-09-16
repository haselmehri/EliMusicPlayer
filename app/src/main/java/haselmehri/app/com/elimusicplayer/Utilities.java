package haselmehri.app.com.elimusicplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Utilities {
    public static final int PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE = 123;
    public static final int PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE_FOR_FAVORITE_MUSIC = 124;
    public static final int PERMISSIONS_REQUEST_CODE_RECORD_AUDIO = 125;
    public static final int PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE_FOR_SERVICE_CONNECTED = 126;

    public static void applyFontToMenuItem(Context context, MenuItem mi, Typeface typeface) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan(context, "", typeface), 0, mNewTitle.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }


    public static String getVersionName(@NonNull Context context) {
        String version = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static void setTypefaceToolbar(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if (toolbar.getChildAt(i) instanceof TextView) {
                ((TextView) toolbar.getChildAt(i)).setTypeface(BaseApplication.getIranianSansFont());
            }
        }
    }

    public static Bitmap getCoverPictureBySong(String songPath) {
        Bitmap art = null;
        if (songPath != null && !TextUtils.isEmpty(songPath)) {
            try {
                File coverPictureFile = new File(songPath);
                if (coverPictureFile.exists()) {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    byte[] rawArt;
                    BitmapFactory.Options bfo = new BitmapFactory.Options();

                    mmr.setDataSource(songPath);
                    rawArt = mmr.getEmbeddedPicture();

                    // if rawArt is null then no cover art is embedded in the file or is not
                    // recognized as such.
                    if (null != rawArt)
                        art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);

                    return art;
                }
            } catch (Exception exp) {
                return null;
            }
        }

        return null;
    }

    public static String getUriRealPath(Context ctx, Uri uri) {
        String ret = "";
        if (isAboveKitKat()) {
            // Android OS above sdk version 19.
            ret = getUriRealPathAboveKitkat(ctx, uri);

        } else {
            // Android OS below sdk version 19
            ret = getRealPath(ctx.getContentResolver(), uri, null);
        }

        return ret;
    }

    /* Check whether current android os version is bigger than kitkat or not. */
    private static boolean isAboveKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getUriRealPathAboveKitkat(Context ctx, Uri uri) {
        String ret = "";

        if (ctx != null && uri != null) {

            if (isDocumentUri(ctx, uri)) {

                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(uri);

                // Get uri authority.
                String uriAuthority = uri.getAuthority();

                if (isMediaDoc(uriAuthority)) {
                    String[] idArr = documentId.split(":");
                    if (idArr.length == 2) {
                        // First item is document type.
                        String docType = idArr[0];

                        // Second item is document real id.
                        String realDocId = idArr[1];

                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if ("image".equals(docType)) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(docType)) {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(docType)) {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                        ret = getRealPath(ctx.getContentResolver(), mediaContentUri, whereClause);
                    }

                } else if (isDownloadDoc(uriAuthority)) {
                    // Build download uri.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");

                    // Append download document id at uri end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

                    ret = getRealPath(ctx.getContentResolver(), downloadUriAppendId, null);

                } else if (isExternalStoreDoc(uriAuthority)) {
                    String[] idArr = documentId.split(":");
                    if (idArr.length == 2) {
                        String type = idArr[0];
                        String realDocId = idArr[1];

                        if ("primary".equalsIgnoreCase(type)) {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
            if (ret.equals("") && isContentUri(uri)) {
                if (isGooglePhotoDoc(uri.getAuthority())) {
                    ret = uri.getLastPathSegment();
                } else {
                    ret = getRealPath(ctx.getContentResolver(), uri, null);
                }
            }
            if (ret.equals("") && isFileUri(uri)) {
                ret = uri.getPath();
            }
        }

        return ret;
    }

    /* Return uri represented document file real local path.*/
    private static String getRealPath(ContentResolver contentResolver, Uri uri, String whereClause) {
        String ret = "";
        Cursor cursor = null;
        try {
            // Query the uri with condition.
            cursor = contentResolver.query(uri, null, whereClause, null, null);

            if (cursor != null) {
                boolean moveToFirst = cursor.moveToFirst();
                if (moveToFirst) {

                    // Get columns name by uri type.
                    String columnName = MediaStore.Images.Media.DATA;

                    /*if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Images.Media.DATA;
                    } else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Audio.Media.DATA;
                    } else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Video.Media.DATA;
                    }*/

                    // Get column index.
                    int columnIndex = cursor.getColumnIndex(columnName);

                    if (columnIndex >= 0) {
                        // Get column value which is the uri related file local path.
                        ret = cursor.getString(columnIndex);
                    } else if (isExternalStoreDoc(uri.getAuthority())) {
                        columnIndex = cursor.getColumnIndex("document_id");
                        if (columnIndex >= 0) {
                            String path = cursor.getString(columnIndex);
                            if (path.split("/").length > 0 && path.split("/")[0].contains(":")) {
                                ret = TextUtils.concat("/storage/", path.replace(":", "/")).toString();
                            }
                        }
                    }

                    if (ret != null && !ret.equals("") && new File(ret).exists())
                        return ret;
                }
            }
            return ret;
        } catch (Exception e) {
            ret = "";
            return ret;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /* Check whether this uri represent a document or not. */
    private static boolean isDocumentUri(Context ctx, Uri uri) {
        boolean ret = false;
        if (ctx != null && uri != null) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ret = DocumentsContract.isDocumentUri(ctx, uri);
            }
        }
        return ret;
    }

    /* Check whether this uri is a content uri or not.
     *  content uri like content://media/external/images/media/1302716
     *  */
    private static boolean isContentUri(Uri uri) {
        boolean ret = false;
        if (uri != null) {
            String uriSchema = uri.getScheme();
            if ("content".equalsIgnoreCase(uriSchema)) {
                ret = true;
            }
        }
        return ret;
    }

    /* Check whether this uri is a file uri or not.
     *  file uri like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
     * */
    private static boolean isFileUri(Uri uri) {
        boolean ret = false;
        if (uri != null) {
            String uriSchema = uri.getScheme();
            if ("file".equalsIgnoreCase(uriSchema)) {
                ret = true;
            }
        }
        return ret;
    }

    /* Check whether this document is provided by DownloadsProvider. */
    private static boolean isDownloadDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.providers.downloads.documents".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by MediaProvider. */
    private static boolean isMediaDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.providers.media.documents".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by google photos. */
    private static boolean isGooglePhotoDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.google.android.apps.photos.content".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by ExternalStorageProvider. */
    private static boolean isExternalStoreDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.externalstorage.documents".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    static boolean checkPermission(final Context context, final String permission, final int requestCode,
                                   String message, String dialogTitle, String yesText, String noText) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

/*    static boolean checkPermission(final Context context, final String permission, final int requestCode,
                                   String message, String dialogTitle, String yesText, String noText) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(dialogTitle);
                    alertBuilder.setMessage(message);
                    alertBuilder.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
                        }
                    });
                    alertBuilder.setNegativeButton(noText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }*/

}
