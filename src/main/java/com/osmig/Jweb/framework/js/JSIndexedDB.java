package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * IndexedDB API DSL for client-side structured storage.
 *
 * <p>IndexedDB provides a transactional, key-value database in the browser
 * for storing large amounts of structured data, including files and blobs.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSIndexedDB.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Open a database
 * openDB("myApp", 1)
 *     .onUpgrade(callback("db")
 *         .unsafeRaw("db.createObjectStore('users',{keyPath:'id'})"))
 *     .onSuccess(callback("db").log("Database opened"))
 *     .build()
 *
 * // Add a record
 * transaction(variable("db"), "users", "readwrite")
 *     .store("users")
 *     .add(obj("id", 1, "name", "Alice"))
 *     .build()
 *
 * // Get a record by key
 * transaction(variable("db"), "users")
 *     .store("users")
 *     .get(1)
 *     .build()
 *
 * // Query with cursor
 * cursorQuery(variable("db"), "users")
 *     .onEach(callback("cursor")
 *         .log(variable("cursor").dot("value")))
 *     .build()
 *
 * // Delete a database
 * deleteDB("myApp")
 * </pre>
 */
public final class JSIndexedDB {
    private JSIndexedDB() {}

    // ==================== Database Operations ====================

    /**
     * Opens (or creates) an IndexedDB database.
     *
     * @param name the database name
     * @param version the database version
     * @return an OpenDBBuilder for configuring the open request
     */
    public static OpenDBBuilder openDB(String name, int version) {
        return new OpenDBBuilder(name, version);
    }

    /**
     * Opens an IndexedDB database with dynamic name and version.
     *
     * @param name the database name expression
     * @param version the database version expression
     * @return an OpenDBBuilder for configuring the open request
     */
    public static OpenDBBuilder openDB(Val name, Val version) {
        return new OpenDBBuilder(name, version);
    }

    /**
     * Deletes an IndexedDB database: indexedDB.deleteDatabase(name)
     *
     * @param name the database name
     * @return a Val representing the delete request
     */
    public static Val deleteDB(String name) {
        return new Val("indexedDB.deleteDatabase('" + JS.esc(name) + "')");
    }

    /**
     * Deletes an IndexedDB database with dynamic name.
     *
     * @param name the database name expression
     * @return a Val representing the delete request
     */
    public static Val deleteDB(Val name) {
        return new Val("indexedDB.deleteDatabase(" + name.js() + ")");
    }

    /**
     * Lists all databases: indexedDB.databases()
     * Returns a Promise resolving to an array of {name, version} objects.
     *
     * @return a Val representing the databases promise
     */
    public static Val listDatabases() {
        return new Val("indexedDB.databases()");
    }

    // ==================== Open Database Builder ====================

    /**
     * Builder for opening an IndexedDB database with lifecycle callbacks.
     */
    public static class OpenDBBuilder {
        private final String openExpr;
        private String onUpgrade;
        private String onSuccess;
        private String onError;
        private String onBlocked;

        OpenDBBuilder(String name, int version) {
            this.openExpr = "indexedDB.open('" + JS.esc(name) + "'," + version + ")";
        }

        OpenDBBuilder(Val name, Val version) {
            this.openExpr = "indexedDB.open(" + name.js() + "," + version.js() + ")";
        }

        /**
         * Called when database version changes; use to create/modify object stores.
         *
         * @param handler callback receiving the database (e.target.result)
         * @return this builder
         */
        public OpenDBBuilder onUpgrade(Func handler) {
            this.onUpgrade = handler.toExpr();
            return this;
        }

        /**
         * Called when database is successfully opened.
         *
         * @param handler callback receiving the database
         * @return this builder
         */
        public OpenDBBuilder onSuccess(Func handler) {
            this.onSuccess = handler.toExpr();
            return this;
        }

        /**
         * Called when an error occurs opening the database.
         *
         * @param handler callback receiving the error event
         * @return this builder
         */
        public OpenDBBuilder onError(Func handler) {
            this.onError = handler.toExpr();
            return this;
        }

        /**
         * Called when the open request is blocked by another connection.
         *
         * @param handler callback receiving the blocked event
         * @return this builder
         */
        public OpenDBBuilder onBlocked(Func handler) {
            this.onBlocked = handler.toExpr();
            return this;
        }

        /**
         * Builds the open database code block.
         *
         * @return a Val representing the complete open operation
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var r=").append(openExpr).append(";");
            if (onUpgrade != null) {
                sb.append("r.onupgradeneeded=function(e){(")
                  .append(onUpgrade).append(")(e.target.result,e);};");
            }
            if (onSuccess != null) {
                sb.append("r.onsuccess=function(e){(")
                  .append(onSuccess).append(")(e.target.result,e);};");
            }
            if (onError != null) {
                sb.append("r.onerror=function(e){(")
                  .append(onError).append(")(e);};");
            }
            if (onBlocked != null) {
                sb.append("r.onblocked=function(e){(")
                  .append(onBlocked).append(")(e);};");
            }
            sb.append("return r;}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Object Store Operations ====================

    /**
     * Creates an object store on the database (use inside onUpgrade).
     *
     * @param db the database reference
     * @param storeName the object store name
     * @return a CreateStoreBuilder
     */
    public static CreateStoreBuilder createStore(Val db, String storeName) {
        return new CreateStoreBuilder(db, storeName);
    }

    /**
     * Deletes an object store from the database (use inside onUpgrade).
     *
     * @param db the database reference
     * @param storeName the store to delete
     * @return a Val representing the delete operation
     */
    public static Val deleteStore(Val db, String storeName) {
        return new Val(db.js() + ".deleteObjectStore('" + JS.esc(storeName) + "')");
    }

    /**
     * Builder for creating an object store with options.
     */
    public static class CreateStoreBuilder {
        private final Val db;
        private final String storeName;
        private String keyPath;
        private Boolean autoIncrement;
        private final List<String> indexes = new ArrayList<>();

        CreateStoreBuilder(Val db, String storeName) {
            this.db = db;
            this.storeName = storeName;
        }

        /** Sets the key path for the store. */
        public CreateStoreBuilder keyPath(String keyPath) {
            this.keyPath = keyPath;
            return this;
        }

        /** Enables auto-increment keys. */
        public CreateStoreBuilder autoIncrement() {
            this.autoIncrement = true;
            return this;
        }

        /** Adds an index to the store. */
        public CreateStoreBuilder index(String name, String keyPath) {
            indexes.add("s.createIndex('" + JS.esc(name) + "','" + JS.esc(keyPath) + "')");
            return this;
        }

        /** Adds a unique index to the store. */
        public CreateStoreBuilder uniqueIndex(String name, String keyPath) {
            indexes.add("s.createIndex('" + JS.esc(name) + "','" + JS.esc(keyPath) + "',{unique:true})");
            return this;
        }

        /** Adds a multi-entry index (for array key paths). */
        public CreateStoreBuilder multiEntryIndex(String name, String keyPath) {
            indexes.add("s.createIndex('" + JS.esc(name) + "','" + JS.esc(keyPath) + "',{multiEntry:true})");
            return this;
        }

        /** Adds a compound index with multiple key paths. */
        public CreateStoreBuilder compoundIndex(String name, String... keyPaths) {
            StringBuilder sb = new StringBuilder("s.createIndex('" + JS.esc(name) + "',[");
            for (int i = 0; i < keyPaths.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("'").append(JS.esc(keyPaths[i])).append("'");
            }
            sb.append("])");
            indexes.add(sb.toString());
            return this;
        }

        /**
         * Builds the create store operation.
         *
         * @return a Val representing the store creation
         */
        public Val build() {
            StringBuilder opts = new StringBuilder("{");
            boolean hasOpt = false;
            if (keyPath != null) {
                opts.append("keyPath:'").append(JS.esc(keyPath)).append("'");
                hasOpt = true;
            }
            if (autoIncrement != null && autoIncrement) {
                if (hasOpt) opts.append(",");
                opts.append("autoIncrement:true");
            }
            opts.append("}");

            StringBuilder sb = new StringBuilder("(function(){var s=")
                .append(db.js()).append(".createObjectStore('")
                .append(JS.esc(storeName)).append("',").append(opts).append(");");
            for (String idx : indexes) {
                sb.append(idx).append(";");
            }
            sb.append("return s;}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Transaction Builder ====================

    /**
     * Creates a transaction on the database.
     *
     * @param db the database reference
     * @param storeNames the store names to include in the transaction
     * @param mode the transaction mode ("readonly" or "readwrite")
     * @return a TransactionBuilder
     */
    public static TransactionBuilder transaction(Val db, String storeNames, String mode) {
        return new TransactionBuilder(db, storeNames, mode);
    }

    /**
     * Creates a readonly transaction.
     *
     * @param db the database reference
     * @param storeNames the store names
     * @return a TransactionBuilder in readonly mode
     */
    public static TransactionBuilder transaction(Val db, String storeNames) {
        return new TransactionBuilder(db, storeNames, "readonly");
    }

    /**
     * Builder for IndexedDB transactions and store operations.
     */
    public static class TransactionBuilder {
        private final String txExpr;
        private String storeName;
        private final List<String> operations = new ArrayList<>();
        private String onComplete;
        private String onError;

        TransactionBuilder(Val db, String storeNames, String mode) {
            this.txExpr = db.js() + ".transaction('" + JS.esc(storeNames) + "','" + JS.esc(mode) + "')";
        }

        /** Selects the object store to operate on. */
        public TransactionBuilder store(String name) {
            this.storeName = name;
            return this;
        }

        /** Adds a record: store.add(value) */
        public TransactionBuilder add(Val value) {
            operations.add("st.add(" + value.js() + ")");
            return this;
        }

        /** Adds a record with explicit key: store.add(value, key) */
        public TransactionBuilder add(Val value, Val key) {
            operations.add("st.add(" + value.js() + "," + key.js() + ")");
            return this;
        }

        /** Puts (upserts) a record: store.put(value) */
        public TransactionBuilder put(Val value) {
            operations.add("st.put(" + value.js() + ")");
            return this;
        }

        /** Puts a record with explicit key: store.put(value, key) */
        public TransactionBuilder put(Val value, Val key) {
            operations.add("st.put(" + value.js() + "," + key.js() + ")");
            return this;
        }

        /** Gets a record by key: store.get(key) */
        public TransactionBuilder get(Object key) {
            operations.add("st.get(" + JS.toJs(key) + ")");
            return this;
        }

        /** Gets a record by key expression: store.get(key) */
        public TransactionBuilder get(Val key) {
            operations.add("st.get(" + key.js() + ")");
            return this;
        }

        /** Gets all records: store.getAll() */
        public TransactionBuilder getAll() {
            operations.add("st.getAll()");
            return this;
        }

        /** Gets all records with limit: store.getAll(null, count) */
        public TransactionBuilder getAll(int count) {
            operations.add("st.getAll(null," + count + ")");
            return this;
        }

        /** Gets all keys: store.getAllKeys() */
        public TransactionBuilder getAllKeys() {
            operations.add("st.getAllKeys()");
            return this;
        }

        /** Counts records: store.count() */
        public TransactionBuilder count() {
            operations.add("st.count()");
            return this;
        }

        /** Deletes a record by key: store.delete(key) */
        public TransactionBuilder delete(Object key) {
            operations.add("st.delete(" + JS.toJs(key) + ")");
            return this;
        }

        /** Deletes a record by key expression: store.delete(key) */
        public TransactionBuilder delete(Val key) {
            operations.add("st.delete(" + key.js() + ")");
            return this;
        }

        /** Clears all records: store.clear() */
        public TransactionBuilder clear() {
            operations.add("st.clear()");
            return this;
        }

        /** Called when the transaction completes successfully. */
        public TransactionBuilder onComplete(Func handler) {
            this.onComplete = handler.toExpr();
            return this;
        }

        /** Called when an error occurs during the transaction. */
        public TransactionBuilder onError(Func handler) {
            this.onError = handler.toExpr();
            return this;
        }

        /**
         * Builds the transaction code block.
         * The last operation's result is available via onsuccess callback.
         *
         * @return a Val representing the transaction
         */
        public Val build() {
            if (storeName == null) throw new IllegalStateException("Store name not set");
            if (operations.isEmpty()) throw new IllegalStateException("No operations specified");

            StringBuilder sb = new StringBuilder("(function(){var tx=").append(txExpr)
                .append(";var st=tx.objectStore('").append(JS.esc(storeName)).append("');");

            // Execute operations, tracking the last one for result handling
            for (int i = 0; i < operations.size(); i++) {
                sb.append("var r").append(i).append("=").append(operations.get(i)).append(";");
            }

            if (onComplete != null) {
                sb.append("tx.oncomplete=function(e){(").append(onComplete).append(")(e);};");
            }
            if (onError != null) {
                sb.append("tx.onerror=function(e){(").append(onError).append(")(e);};");
            }

            sb.append("return r").append(operations.size() - 1).append(";}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Cursor Queries ====================

    /**
     * Creates a cursor query for iterating over records.
     *
     * @param db the database reference
     * @param storeName the object store to query
     * @return a CursorBuilder
     */
    public static CursorBuilder cursorQuery(Val db, String storeName) {
        return new CursorBuilder(db, storeName);
    }

    /**
     * Builder for cursor-based iteration over IndexedDB records.
     */
    public static class CursorBuilder {
        private final Val db;
        private final String storeName;
        private String indexName;
        private String range;
        private String direction;
        private String onEach;
        private String onComplete;

        CursorBuilder(Val db, String storeName) {
            this.db = db;
            this.storeName = storeName;
        }

        /** Uses an index for the cursor query. */
        public CursorBuilder index(String indexName) {
            this.indexName = indexName;
            return this;
        }

        /** Sets direction to 'next' (default, ascending). */
        public CursorBuilder ascending() {
            this.direction = "'next'";
            return this;
        }

        /** Sets direction to 'prev' (descending). */
        public CursorBuilder descending() {
            this.direction = "'prev'";
            return this;
        }

        /** Sets direction to 'nextunique' (ascending, unique keys only). */
        public CursorBuilder ascendingUnique() {
            this.direction = "'nextunique'";
            return this;
        }

        /** Sets direction to 'prevunique' (descending, unique keys only). */
        public CursorBuilder descendingUnique() {
            this.direction = "'prevunique'";
            return this;
        }

        // ==================== IDBKeyRange ====================

        /** Only matches the given key. */
        public CursorBuilder only(Object key) {
            this.range = "IDBKeyRange.only(" + JS.toJs(key) + ")";
            return this;
        }

        /** Matches keys >= lower. */
        public CursorBuilder lowerBound(Object lower) {
            this.range = "IDBKeyRange.lowerBound(" + JS.toJs(lower) + ")";
            return this;
        }

        /** Matches keys > lower (exclusive). */
        public CursorBuilder lowerBoundExclusive(Object lower) {
            this.range = "IDBKeyRange.lowerBound(" + JS.toJs(lower) + ",true)";
            return this;
        }

        /** Matches keys <= upper. */
        public CursorBuilder upperBound(Object upper) {
            this.range = "IDBKeyRange.upperBound(" + JS.toJs(upper) + ")";
            return this;
        }

        /** Matches keys < upper (exclusive). */
        public CursorBuilder upperBoundExclusive(Object upper) {
            this.range = "IDBKeyRange.upperBound(" + JS.toJs(upper) + ",true)";
            return this;
        }

        /** Matches keys in range [lower, upper]. */
        public CursorBuilder bound(Object lower, Object upper) {
            this.range = "IDBKeyRange.bound(" + JS.toJs(lower) + "," + JS.toJs(upper) + ")";
            return this;
        }

        /** Matches keys in range with exclusion options. */
        public CursorBuilder bound(Object lower, Object upper, boolean lowerOpen, boolean upperOpen) {
            this.range = "IDBKeyRange.bound(" + JS.toJs(lower) + "," + JS.toJs(upper) + "," + lowerOpen + "," + upperOpen + ")";
            return this;
        }

        /**
         * Callback invoked for each cursor position.
         * The callback receives the cursor object with .value, .key, and .continue() methods.
         *
         * @param handler callback receiving the cursor
         * @return this builder
         */
        public CursorBuilder onEach(Func handler) {
            this.onEach = handler.toExpr();
            return this;
        }

        /**
         * Callback invoked when cursor iteration completes (cursor is null).
         *
         * @param handler callback invoked on completion
         * @return this builder
         */
        public CursorBuilder onComplete(Func handler) {
            this.onComplete = handler.toExpr();
            return this;
        }

        /**
         * Builds the cursor query code block.
         *
         * @return a Val representing the cursor iteration
         */
        public Val build() {
            if (onEach == null) throw new IllegalStateException("onEach handler not set");

            StringBuilder sb = new StringBuilder("(function(){var tx=")
                .append(db.js()).append(".transaction('").append(JS.esc(storeName)).append("');")
                .append("var src=tx.objectStore('").append(JS.esc(storeName)).append("')");

            if (indexName != null) {
                sb.append(".index('").append(JS.esc(indexName)).append("')");
            }
            sb.append(";");

            // Build openCursor arguments
            sb.append("var req=src.openCursor(");
            if (range != null) {
                sb.append(range);
                if (direction != null) sb.append(",").append(direction);
            } else if (direction != null) {
                sb.append("null,").append(direction);
            }
            sb.append(");");

            sb.append("req.onsuccess=function(e){var c=e.target.result;if(c){(")
              .append(onEach).append(")(c);c.continue();}");
            if (onComplete != null) {
                sb.append("else{(").append(onComplete).append(")();}");
            }
            sb.append("};");

            sb.append("}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Index Query Helpers ====================

    /**
     * Gets records from an index.
     *
     * @param db the database reference
     * @param storeName the object store name
     * @param indexName the index name
     * @param key the key to look up
     * @return a Val representing the index get request
     */
    public static Val indexGet(Val db, String storeName, String indexName, Val key) {
        return new Val("(function(){var tx=" + db.js() + ".transaction('" + JS.esc(storeName) + "');"
            + "return tx.objectStore('" + JS.esc(storeName) + "').index('" + JS.esc(indexName) + "').get(" + key.js() + ");}())");
    }

    /**
     * Gets all records matching a key from an index.
     *
     * @param db the database reference
     * @param storeName the object store name
     * @param indexName the index name
     * @param key the key to look up
     * @return a Val representing the index getAll request
     */
    public static Val indexGetAll(Val db, String storeName, String indexName, Val key) {
        return new Val("(function(){var tx=" + db.js() + ".transaction('" + JS.esc(storeName) + "');"
            + "return tx.objectStore('" + JS.esc(storeName) + "').index('" + JS.esc(indexName) + "').getAll(" + key.js() + ");}())");
    }

    // ==================== Key Range Helpers ====================

    /** Creates IDBKeyRange.only(value) */
    public static Val keyOnly(Object value) {
        return new Val("IDBKeyRange.only(" + JS.toJs(value) + ")");
    }

    /** Creates IDBKeyRange.lowerBound(lower) */
    public static Val keyLowerBound(Object lower) {
        return new Val("IDBKeyRange.lowerBound(" + JS.toJs(lower) + ")");
    }

    /** Creates IDBKeyRange.lowerBound(lower, open) */
    public static Val keyLowerBound(Object lower, boolean open) {
        return new Val("IDBKeyRange.lowerBound(" + JS.toJs(lower) + "," + open + ")");
    }

    /** Creates IDBKeyRange.upperBound(upper) */
    public static Val keyUpperBound(Object upper) {
        return new Val("IDBKeyRange.upperBound(" + JS.toJs(upper) + ")");
    }

    /** Creates IDBKeyRange.upperBound(upper, open) */
    public static Val keyUpperBound(Object upper, boolean open) {
        return new Val("IDBKeyRange.upperBound(" + JS.toJs(upper) + "," + open + ")");
    }

    /** Creates IDBKeyRange.bound(lower, upper) */
    public static Val keyBound(Object lower, Object upper) {
        return new Val("IDBKeyRange.bound(" + JS.toJs(lower) + "," + JS.toJs(upper) + ")");
    }

    /** Creates IDBKeyRange.bound(lower, upper, lowerOpen, upperOpen) */
    public static Val keyBound(Object lower, Object upper, boolean lowerOpen, boolean upperOpen) {
        return new Val("IDBKeyRange.bound(" + JS.toJs(lower) + "," + JS.toJs(upper) + "," + lowerOpen + "," + upperOpen + ")");
    }

    // ==================== Promise Wrappers ====================

    /**
     * Wraps an IDBRequest in a Promise for async/await usage.
     *
     * @param request the IDB request expression
     * @return a Val representing the promisified request
     */
    public static Val promisify(Val request) {
        return new Val("new Promise(function(resolve,reject){var r=" + request.js()
            + ";r.onsuccess=function(){resolve(r.result);};r.onerror=function(){reject(r.error);}})");
    }

    /**
     * Wraps an IDB transaction completion in a Promise.
     *
     * @param transaction the transaction expression
     * @return a Val representing the promisified transaction
     */
    public static Val promisifyTransaction(Val transaction) {
        return new Val("new Promise(function(resolve,reject){var t=" + transaction.js()
            + ";t.oncomplete=function(){resolve();};t.onerror=function(){reject(t.error);}})");
    }
}
