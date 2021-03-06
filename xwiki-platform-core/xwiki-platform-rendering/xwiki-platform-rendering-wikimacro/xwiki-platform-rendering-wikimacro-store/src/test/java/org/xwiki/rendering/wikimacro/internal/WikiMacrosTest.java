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
package org.xwiki.rendering.wikimacro.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.AllComponents;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.test.MockitoOldcoreRule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Various general tests on wiki macros.
 * 
 * @version $Id$
 */
@AllComponents
public class WikiMacrosTest
{
    @Rule
    public MockitoOldcoreRule oldcore = new MockitoOldcoreRule();

    private XWikiDocument macroDocument;

    private BaseObject macroObject;

    @Before
    public void before() throws Exception
    {
        this.macroDocument = new XWikiDocument(new DocumentReference("wiki", "Space", "Page"));
        this.macroDocument.setSyntax(Syntax.XWIKI_2_0);
        this.macroObject = new BaseObject();
        this.macroObject.setXClassReference(new DocumentReference("wiki", "XWiki", "WikiMacroClass"));
        this.macroObject.setStringValue("id", "macroid");
        this.macroObject.setLargeStringValue("code", "code");
        this.macroDocument.addXObject(macroObject);

        this.oldcore.getXWikiContext().setWikiId("wiki");

        // We need component related events
        this.oldcore.notifyComponentDescriptorEvent();
        this.oldcore.notifyDocumentCreatedEvent(true);
        this.oldcore.notifyDocumentUpdatedEvent(true);
    }

    private ComponentManager getWikiComponentManager() throws Exception
    {
        return this.oldcore.getMocker().getInstance(ComponentManager.class, "wiki");
    }

    private ComponentManager getUserComponentManager() throws Exception
    {
        return this.oldcore.getMocker().getInstance(ComponentManager.class, "user");
    }

    @Test
    public void testSaveWikiMacro() throws Exception
    {
        when(this.oldcore.getMockRightService().hasAccessLevel(any(String.class), any(String.class), any(String.class), any(XWikiContext.class))).thenReturn(true);
        when(this.oldcore.getMockRightService().hasWikiAdminRights(any(XWikiContext.class))).thenReturn(true);
        when(this.oldcore.getMockRightService().hasProgrammingRights(any(XWikiContext.class))).thenReturn(true);

        this.macroObject.setStringValue("visibility", "Current Wiki");

        // Save wiki macro
        this.oldcore.getMockXWiki().saveDocument(this.macroDocument, this.oldcore.getXWikiContext());

        Macro testMacro = getWikiComponentManager().getInstance(Macro.class, "macroid");

        Assert.assertEquals("macroid", testMacro.getDescriptor().getId().getId());

        try {
            testMacro = this.oldcore.getMocker().getInstance(Macro.class, "macroid");

            Assert.fail("Found macro with wiki visibility in global componenet manager");
        } catch (ComponentLookupException expected) {
        }
    }

    @Test
    public void testUnRegisterWikiMacroWithDifferentVisibilityKeys() throws Exception
    {
        when(this.oldcore.getMockRightService().hasAccessLevel(any(String.class), any(String.class), any(String.class), any(XWikiContext.class))).thenReturn(true);

        this.macroObject.setStringValue("visibility", "Current User");

        DocumentReference user1 = new DocumentReference("wiki", "Wiki", "user1");

        this.macroDocument.setAuthorReference(user1);

        // Save wiki macro
        this.oldcore.getMockXWiki().saveDocument(this.macroDocument, this.oldcore.getXWikiContext());

        // Try to lookup the macro
        this.oldcore.getXWikiContext().setUserReference(user1);
        Macro testMacro = getUserComponentManager().getInstance(Macro.class, "macroid");

        Assert.assertEquals("macroid", testMacro.getDescriptor().getId().getId());

        // register with another user

        DocumentReference user2 = new DocumentReference("wiki", "Wiki", "user2");

        this.macroDocument.setAuthorReference(user2);

        // Save wiki macro
        this.oldcore.getMockXWiki().saveDocument(this.macroDocument, this.oldcore.getXWikiContext());

        // Try to lookup the macro
        this.oldcore.getXWikiContext().setUserReference(user2);
        testMacro = getUserComponentManager().getInstance(Macro.class, "macroid");

        Assert.assertEquals("macroid", testMacro.getDescriptor().getId().getId());

        // validate that the macro as been properly unregistered for former user
        this.oldcore.getXWikiContext().setUserReference(user1);

        try {
            testMacro = getUserComponentManager().getInstance(Macro.class, "macroid");

            Assert.fail("The macro has not been properly unregistered");
        } catch (ComponentLookupException expected) {
        }
    }
}
