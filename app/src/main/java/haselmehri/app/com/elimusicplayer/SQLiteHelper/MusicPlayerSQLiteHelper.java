package haselmehri.app.com.elimusicplayer.SQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import haselmehri.app.com.elimusicplayer.model.Favorite;

public class MusicPlayerSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseOpenHelper";
    private final static String DATABASE_NAME = "db_music_player";
    private static final int DATABASE_VERSION = 2;
    public final static String FAVORITE_TABLE_NAME = "tblFavorite";

    public static final String COL_FILE_PATH = "col_file_path";

    private static final String SQL_COMMAND_CREATE_FAVORITE_TABLE =
            "create table if not exists " + FAVORITE_TABLE_NAME + " (" +
                    COL_FILE_PATH + " TEXT);";

    private Context context;

    public MusicPlayerSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_COMMAND_CREATE_FAVORITE_TABLE);

        } catch (SQLException e) {
            Log.e(TAG, "onCreate: " + e.toString());
        } finally {

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {

        } catch (SQLException e) {

        }
    }

    public boolean deleteDatabase() {
        return context.deleteDatabase(DATABASE_NAME);
    }

    public boolean addFavorite(Favorite favorite) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FILE_PATH, favorite.getFilePath());

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        long isInserted = sqLiteDatabase.insert(FAVORITE_TABLE_NAME, null, cv);
        Log.i(TAG, "addFavorite: " + isInserted);
        if (isInserted != -1)
            return true;

        return false;
    }

    public List<Favorite> getFavorites() {
        List<Favorite> favorites = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from " + FAVORITE_TABLE_NAME, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                Favorite favorite = new Favorite();

                favorite.setFilePath(cursor.getString(0));

                favorites.add(favorite);
                cursor.moveToNext();
            }
            cursor.close();
        }
        sqLiteDatabase.close();
        sqLiteDatabase.releaseReference();
        return favorites;
    }

    public boolean checkFavoriteExists(String filePath) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "
                + FAVORITE_TABLE_NAME
                + " WHERE "
                + COL_FILE_PATH
                + " = ?", new String[]{String.valueOf(filePath)});

        boolean favoriteExist = cursor.moveToFirst();
        cursor.close();

        return favoriteExist;
    }

    public boolean deleteFavorite(String filePath) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int rowAffected = sqLiteDatabase.delete(FAVORITE_TABLE_NAME, COL_FILE_PATH + " = ?", new String[]{String.valueOf(filePath)});
        sqLiteDatabase.close();
        sqLiteDatabase.releaseReference();

        if (rowAffected > 0)
            return true;

        return false;
    }
}
