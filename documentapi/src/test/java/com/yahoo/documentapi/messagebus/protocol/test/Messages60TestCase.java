// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.documentapi.messagebus.protocol.test;

import com.yahoo.component.Version;
import com.yahoo.document.BucketId;
import com.yahoo.document.select.OrderingSpecification;
import com.yahoo.documentapi.messagebus.protocol.CreateVisitorMessage;
import com.yahoo.documentapi.messagebus.protocol.DocumentProtocol;
import com.yahoo.text.Utf8;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Messages60TestCase extends Messages52TestCase {

    @Override
    protected Version version() {
        return new Version(6, 999, 0);
    } // TODO finalize version

    @Override
    protected boolean shouldTestCoverage() {
        return true;
    }

    @Override
    protected void registerTests(Map<Integer, MessagesTestBase.RunnableTest> out) {
        super.registerTests(out);

        // This list MUST mirror the list of routable factories from the DocumentProtocol constructor that support
        // version 6.0. When adding tests to this list, please KEEP THEM ORDERED alphabetically like they are now.

        out.put(DocumentProtocol.MESSAGE_CREATEVISITOR, new Messages60TestCase.testCreateVisitorMessage());
    }

    public class testCreateVisitorMessage implements RunnableTest {

        private static final String BUCKET_SPACE = "bjarne";

        // FIXME there is a large amount of code duplication across version tests, presumably
        // to ensure that fields survive across version boundaries, but it makes code very bloated.
        // TODO head towards a blissful Protobuf future instead...
        @Override
        public void run() {
            CreateVisitorMessage msg = new CreateVisitorMessage("SomeLibrary", "myvisitor", "newyork", "london");
            msg.setDocumentSelection("true and false or true");
            msg.getParameters().put("myvar", Utf8.toBytes("somevalue"));
            msg.getParameters().put("anothervar", Utf8.toBytes("34"));
            msg.getBuckets().add(new BucketId(16, 1234));
            msg.setVisitRemoves(true);
            msg.setFieldSet("foo bar");
            msg.setVisitorOrdering(OrderingSpecification.DESCENDING);
            msg.setMaxBucketsPerVisitor(2);
            msg.setBucketSpace(BUCKET_SPACE);
            assertEquals(BASE_MESSAGE_LENGTH + 184 + serializedLength(BUCKET_SPACE), serialize("CreateVisitorMessage", msg));

            for (Language lang : LANGUAGES) {
                msg = (CreateVisitorMessage)deserialize("CreateVisitorMessage", DocumentProtocol.MESSAGE_CREATEVISITOR, lang);
                assertEquals("SomeLibrary", msg.getLibraryName());
                assertEquals("myvisitor", msg.getInstanceId());
                assertEquals("newyork", msg.getControlDestination());
                assertEquals("london", msg.getDataDestination());
                assertEquals("true and false or true", msg.getDocumentSelection());
                assertEquals(8, msg.getMaxPendingReplyCount());
                assertEquals(true, msg.getVisitRemoves());
                assertEquals("foo bar", msg.getFieldSet());
                assertEquals(false, msg.getVisitInconsistentBuckets());
                assertEquals(1, msg.getBuckets().size());
                assertEquals(new BucketId(16, 1234), msg.getBuckets().iterator().next());
                assertEquals("somevalue", Utf8.toString(msg.getParameters().get("myvar")));
                assertEquals("34", Utf8.toString(msg.getParameters().get("anothervar")));
                assertEquals(OrderingSpecification.DESCENDING, msg.getVisitorOrdering());
                assertEquals(2, msg.getMaxBucketsPerVisitor());
                assertEquals(BUCKET_SPACE, msg.getBucketSpace());
            }
        }
    }

    // TODO want to test that non-default bucket space fails to encode with old version

}
