package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkFirebaseMessagingService extends FirebaseMessagingService {


    private static final int NOTIFICATION_MAX_CHARACTERS = 30;

    private static final String JSON_KEY_AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String JSON_KEY_AUTHOR_KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String JSON_KEY_MESSAGE = SquawkContract.COLUMN_MESSAGE;
    private static final String JSON_KEY_DATE = SquawkContract.COLUMN_DATE;

    // COMPLETED (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343
    // COMPLETED (3) As part of the new Service - If there is message data, get the data using
    // the keys and do two things with it :
    // 1. Display a notification with the first 30 character of the message
    // 2. Use the content provider to insert a new message into the local database
    // Hint: You shouldn't be doing content provider operations on the main thread.
    // If you don't know how to make notifications or interact with a content provider
    // look at the notes in the classroom for help.


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {

            displayNotification(data);
            insertSquawk(data);

            // InsertSquawk insertSquawk = new InsertSquawk(this, data);
        }

    }

    private void displayNotification(Map<String, String> data) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        
        String author = data.get(JSON_KEY_AUTHOR);
        String message = data.get(JSON_KEY_MESSAGE);
        
        if (message.length() > NOTIFICATION_MAX_CHARACTERS) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTERS) +
                    "\u2026"; // unicode character for ellipsis
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    private void insertSquawk(final Map<String, String> data) {

        AsyncTask<Void, Void, Void> inertSquawkTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SquawkContract.COLUMN_AUTHOR, data.get(JSON_KEY_AUTHOR));
                contentValues.put(SquawkContract.COLUMN_MESSAGE, data.get(JSON_KEY_MESSAGE));
                contentValues.put(SquawkContract.COLUMN_DATE, data.get(JSON_KEY_DATE));
                contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(JSON_KEY_AUTHOR_KEY));

                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI,
                        contentValues);
                return null;
            }
        };

        inertSquawkTask.execute();
    }


    /*public static class InsertSquawk extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Context> mContext;
        private final WeakReference<Map<String, String>> mData;


        private InsertSquawk(Context mContext, Map<String, String> mData) {
            this.mContext = new WeakReference<>(mContext);
            this.mData = new WeakReference<>(mData);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SquawkContract.COLUMN_AUTHOR, mData.get().containsKey(JSON_KEY_AUTHOR));
            contentValues.put(SquawkContract.COLUMN_MESSAGE, mData.get().containsKey(JSON_KEY_MESSAGE));
            contentValues.put(SquawkContract.COLUMN_DATE, mData.get().containsKey(JSON_KEY_DATE));
            contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, mData.get().containsKey(JSON_KEY_AUTHOR_KEY));

            Context context = mContext.get();

            context.getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI,
                    contentValues);

            return null;
        }
    }*/
}
