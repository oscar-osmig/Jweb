package com.osmig.Jweb.app.api;

import jweb.Doc;
import jweb.Mongo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The review lifecycle on the in-memory store (no Mongo in unit tests):
 * submit → pending, approve → published, delete → gone.
 */
class SnippetStoreTest {

    @Test
    void submitLandsInTheQueueAndApproveMovesItToTheGallery() {
        SnippetStore store = new SnippetStore();
        Doc s = store.submit("Card", "Ada", "div(\"hi\")");

        assertNotNull(s.getId(), "the id is assigned on submit");
        assertEquals(1, store.pending().size());
        assertTrue(store.approved().isEmpty());
        assertTrue(store.byId(s.getId()).isPresent());
        assertEquals(SnippetStore.PENDING, s.getString("status"));

        assertTrue(store.approve(s.getId()));
        assertTrue(store.pending().isEmpty());
        assertEquals("Card", store.approved().get(0).getString("title"));
        assertNotNull(store.approved().get(0).get("approvedAt"));
        assertTrue(store.approve(s.getId()), "approving twice is a no-op, not a miss");

        assertTrue(store.delete(s.getId()));
        assertTrue(store.approved().isEmpty());
        assertFalse(store.delete(s.getId()), "gone is gone");
    }

    @Test
    void unknownAndMalformedIdsAreMisses() {
        SnippetStore store = new SnippetStore();
        assertFalse(store.approve("nope"));
        assertFalse(store.approve(Mongo.newId()), "well-formed but unknown");
        assertFalse(store.delete("../etc/passwd"));
        assertTrue(store.byId(null).isEmpty());
        assertTrue(store.byId("").isEmpty());
    }

    @Test
    void listsAreNewestFirst() {
        SnippetStore store = new SnippetStore();
        store.submit("First", "Ada", "p(\"1\")");
        Doc second = store.submit("Second", "Ada", "p(\"2\")");

        assertEquals("Second", store.pending().get(0).getString("title"));
        store.approve(second.getId());
        store.approve(store.pending().get(0).getId());
        assertEquals("Second", store.approved().get(0).getString("title"));
    }
}
