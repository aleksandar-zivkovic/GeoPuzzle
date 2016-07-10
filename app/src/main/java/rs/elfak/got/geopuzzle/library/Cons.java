package rs.elfak.got.geopuzzle.library;

/**
 * Created by Aleksandar on 15.5.2016..
 */
public class Cons {
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    public static final String KEY_UID = "uid";
    public static final String KEY_USERNAME = "uname";
    public static final String KEY_USER = "user";
    public static final String KEY_FIRSTNAME = "fname";
    public static final String KEY_LASTNAME = "lname";
    public static final String KEY_FULLNAME = "fullname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE_NUMBER = "phonenumber";
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_FRIENDS_NUM = "friendsNum";
    public static final String KEY_PUZZLE_NUM = "puzzleNum";
    public static final String KEY_PUZZLE_CHUNK_NUM = "puzzleChunkNum";
    public static final String KEY_PUZZLE_TITLE = "title";
    public static final String KEY_CHUNK_TITLE = "chunkTitle";
    public static final String KEY_PUZZLE_CHUNKS= "chunks";

    public static final String KEY_PUZZLE_CHUNK_TITLE= "puzzle_chunk_title";
    public static final String KEY_PUZZLE_CHUNK_LATITUDE =  "puzzle_chunk_latitude";
    public static final String KEY_PUZZLE_CHUNK_LONGITUDE= "puzzle_chunk_longitude";

    // used for key-value pairs in values table
    public static final String KEY_KEY_NAME = "key";
    public static final String KEY_KEY_VALUE = "value";
    public static final String KEY_LOGGED_IN = "loggedIn";
    public static final String KEY_KEEP_LOGGED_IN = "keepLoggedIn";
    public static final String KEY_REG_ID = "regId";

    public static final int KEY_RESULT_LOAD_IMAGE = 11;

    public static final int KEY_MAX_WIDTH = 600;

    public static final String SERVER_URL = "http://vasic.ddns.net/geopuzzle_login_api/";
    // http://vasic.ddns.net/ -> vasic server
    //192.168.0.102/

    public static final String KEY_UPLOADS_URL = SERVER_URL + "/uploads/";
    public static final String KEY_PUZZLES_URL = SERVER_URL + "/puzzles/";
    public static final String KEY_CHUNKS_URL = SERVER_URL + "/chunks/";

    public static final int REQUEST_ENABLE_BT = 100;
    public static final int SELECT_SERVER = 200;
    public static final int DATA_RECEIVED = 500;
    public static final int DATA_RESPONSE = 600;
    public static final int DATA_ACCEPTED = 700;
    public static final int DATA_DECLINED = 800;


}
