/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.refactoring.internal.job;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.job.Job;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.refactoring.job.EntityRequest;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.doc.XWikiDocument;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeleteJob}.
 * 
 * @version $Id$
 */
public class DeleteJobTest extends AbstractOldCoreEntityJobTest
{
    @Rule
    public MockitoComponentMockingRule<Job> mocker = new MockitoComponentMockingRule<Job>(DeleteJob.class);

    @Override
    protected MockitoComponentMockingRule<Job> getMocker()
    {
        return this.mocker;
    }

    @Test
    public void deleteDocument() throws Exception
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.xcontext.getWiki().exists(documentReference, xcontext)).thenReturn(true);

        XWikiDocument document = mock(XWikiDocument.class);
        when(this.xcontext.getWiki().getDocument(documentReference, xcontext)).thenReturn(document);

        DocumentReference aliceReference = new DocumentReference("wiki", "Users", "Alice");
        DocumentReference bobReference = new DocumentReference("wiki", "Users", "Bob");
        when(this.xcontext.getUserReference()).thenReturn(bobReference);

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(false);
        request.setUserReference(aliceReference);
        run(request);

        verify(this.xcontext).setUserReference(aliceReference);
        verify(this.xcontext.getWiki()).deleteAllDocuments(document, xcontext);
        verify(this.mocker.getMockedLogger()).info("Document [{}] has been deleted with all its translations.",
            documentReference);
        verify(this.xcontext).setUserReference(bobReference);
    }

    @Test
    public void deleteMissingDocument() throws Exception
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        run(createRequest(documentReference));
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", documentReference);
        verify(this.xcontext.getWiki(), never()).deleteAllDocuments(any(XWikiDocument.class), eq(xcontext));
    }

    @Test
    public void deleteDocumentWithoutDeleteRight() throws Exception
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.xcontext.getWiki().exists(documentReference, xcontext)).thenReturn(true);

        DocumentReference userReference = new DocumentReference("wiki", "Users", "Alice");
        when(this.authorization.hasAccess(Right.DELETE, userReference, documentReference)).thenReturn(false);

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(true);
        request.setUserReference(userReference);
        run(request);

        verify(this.mocker.getMockedLogger()).error("You are not allowed to delete [{}].", documentReference);
        verify(this.xcontext.getWiki(), never()).deleteAllDocuments(any(XWikiDocument.class), eq(xcontext));
    }

    @Test
    public void deleteSpaceHomeDeep() throws Exception
    {
        EntityRequest request = createRequest(new DocumentReference("wiki", "Space", "WebHome"));
        request.setDeep(true);
        run(request);

        // We only verify if the query manager is called. The rest of the test is in #deleteSpace()
        QueryManager queryManager = this.mocker.getInstance(QueryManager.class);
        verify(queryManager).createQuery(any(String.class), eq(Query.HQL));
    }

    @Test
    public void deleteSpace() throws Exception
    {
        SpaceReference spaceReference = new SpaceReference("Space", new WikiReference("wiki"));
        EntityReferenceSerializer<String> localEntityReferenceSerializer =
            this.mocker.getInstance(EntityReferenceSerializer.TYPE_STRING, "local");
        when(localEntityReferenceSerializer.serialize(spaceReference)).thenReturn("Space");

        DocumentReferenceResolver<String> explicitDocumentReferenceResolver =
            this.mocker.getInstance(DocumentReferenceResolver.TYPE_STRING, "explicit");
        DocumentReference aliceReference = new DocumentReference("wiki", "Space", "Alice");
        when(explicitDocumentReferenceResolver.resolve("Space.Alice", spaceReference)).thenReturn(aliceReference);
        DocumentReference bobReference = new DocumentReference("wiki", "Space", "Bob");
        when(explicitDocumentReferenceResolver.resolve("Space.Bob", spaceReference)).thenReturn(bobReference);

        Query query = mock(Query.class);
        when(query.execute()).thenReturn(Arrays.<Object>asList("Space.Alice", "Space.Bob"));

        QueryManager queryManager = this.mocker.getInstance(QueryManager.class);
        when(queryManager.createQuery(any(String.class), eq(Query.HQL))).thenReturn(query);

        run(createRequest(spaceReference));

        // We only verify that the code tries to delete the documents.
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", aliceReference);
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", bobReference);
    }

    @Test
    public void deleteUnsupportedEntity() throws Exception
    {
        run(createRequest(new WikiReference("foo")));
        verify(this.mocker.getMockedLogger()).error("Unsupported entity type [{}].", EntityType.WIKI);
        verify(this.xcontext.getWiki(), never()).deleteAllDocuments(any(XWikiDocument.class), eq(xcontext));
    }

    private EntityRequest createRequest(EntityReference... entityReference)
    {
        EntityRequest request = new EntityRequest();
        request.setEntityReferences(Arrays.asList(entityReference));
        return request;
    }
}
