package com.pushtorefresh.storio.sqlite.impl.auto_parcel;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.operation.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operation.put.PutResult;
import com.pushtorefresh.storio.sqlite.query.DeleteQuery;
import com.pushtorefresh.storio.sqlite.query.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AutoParcelTest {

    @NonNull // Initialized in @Before
    private StorIOSQLite storIOSQLite;

    @Before
    public void setUp() {
        storIOSQLite = new DefaultStorIOSQLite.Builder()
                .sqliteOpenHelper(new OpenHelper(InstrumentationRegistry.getContext()))
                .addTypeMapping(Book.class, new SQLiteTypeMapping.Builder<Book>()
                        .putResolver(BookTableMeta.PUT_RESOLVER)
                        .getResolver(BookTableMeta.GET_RESOLVER)
                        .deleteResolver(BookTableMeta.DELETE_RESOLVER)
                        .build())
                .build();

        // Clearing books table before each test case
        storIOSQLite
                .delete()
                .byQuery(new DeleteQuery.Builder()
                        .table(BookTableMeta.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @Test
    public void insertObject() {
        final Book book = Book.builder()
                .id(1)
                .title("What a great book")
                .author("Somebody")
                .build();

        final PutResult putResult = storIOSQLite
                .put()
                .object(book)
                .prepare()
                .executeAsBlocking();

        assertTrue(putResult.wasInserted());

        final List<Book> storedBooks = storIOSQLite
                .get()
                .listOfObjects(Book.class)
                .withQuery(new Query.Builder()
                        .table(BookTableMeta.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();

        assertEquals(1, storedBooks.size());

        assertEquals(book, storedBooks.get(0));
    }

    @Test
    public void updateObject() {
        final Book book = Book.builder()
                .id(1)
                .title("What a great book")
                .author("Somebody")
                .build();

        final PutResult putResult1 = storIOSQLite
                .put()
                .object(book)
                .prepare()
                .executeAsBlocking();

        assertTrue(putResult1.wasInserted());

        final Book bookWithUpdatedInfo = Book.builder()
                .id(1) // Same id, should be updated
                .title("Corrected title")
                .author("Corrected author")
                .build();

        final PutResult putResult2 = storIOSQLite
                .put()
                .object(bookWithUpdatedInfo)
                .prepare()
                .executeAsBlocking();

        assertTrue(putResult2.wasUpdated());

        final List<Book> storedBooks = storIOSQLite
                .get()
                .listOfObjects(Book.class)
                .withQuery(new Query.Builder()
                        .table(BookTableMeta.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();

        assertEquals(1, storedBooks.size());

        assertEquals(bookWithUpdatedInfo, storedBooks.get(0));
    }

    @Test
    public void deleteObject() {
        final Book book = Book.builder()
                .id(1)
                .title("What a great book")
                .author("Somebody")
                .build();

        final PutResult putResult = storIOSQLite
                .put()
                .object(book)
                .prepare()
                .executeAsBlocking();

        assertTrue(putResult.wasInserted());

        final DeleteResult deleteResult = storIOSQLite
                .delete()
                .object(book)
                .prepare()
                .executeAsBlocking();

        assertEquals(1, deleteResult.numberOfRowsDeleted());

        final List<Book> storedBooks = storIOSQLite
                .get()
                .listOfObjects(Book.class)
                .withQuery(new Query.Builder()
                        .table(BookTableMeta.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();

        assertEquals(0, storedBooks.size());
    }
}
