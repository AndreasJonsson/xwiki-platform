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
package org.xwiki.officeimporter.internal.filter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;


/**
 * Sometimes the heading becomes wrapped in an ordered list structure such as this during import:
 * 1.
 * 
 * 11. 
 * 111. 
 * 1111. 
 * 11111. (((
 * ===== Heading text =====
 * )))
 *
 * This filter removes these structures.
 *
 * @version $Id$
 * @since 6.4.6
 */
@Component
@Named("officeimporter/heading-in-ordered-list")
@Singleton
public class HeadingInOrderedListFilter extends AbstractHTMLFilter
{

    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        final List<Element> headings = filterDescendants(document.getDocumentElement(),
                                                new String[] {
                                                    TAG_H1, TAG_H2, TAG_H3,
                                                    TAG_H4, TAG_H5, TAG_H6
                                                });

        for (Element heading : headings) {
            filterHeading(heading);
        }
    }

    private void filterHeading(Element heading)
    {
        final Element topElement = getTopElement(heading);

        if (topElement == heading) {
            return;
        }

        final List<Node> elementsToKeep = new ArrayList<Node>();
        getElementsToKeep(topElement, elementsToKeep);

        for (Node keep : elementsToKeep) {
            topElement.getParentNode().insertBefore(keep, topElement);
        }

        topElement.getParentNode().removeChild(topElement);
    }

    private Element getTopElement(Element e)
    {
        final Node parent = e.getParentNode();
        if (parent instanceof Element) {
            final Element pe = (Element) parent;
            final String n = pe.getTagName();
            if (n.equals(TAG_LI) || n.equals(TAG_OL)) {
                return getTopElement(pe);
            }
            if (n.equals(TAG_DIV)) {
                // Only filter <div> tags that are inside list.
                final Element te = getTopElement(pe);
                if (te == pe) {
                    return e;
                }
                return te;
            }
        }
        return e;
    }

    private void getElementsToKeep(final Element element, final List<Node> keepList)
    {
        final NodeList children = element.getChildNodes();

        int removed = 0;

        for (int i = 0;  i - removed < children.getLength(); i++) {
            final Node child = children.item(i - removed);
            if (checkKeepChild(child, keepList)) {
                removed++;
            }
        }
    }

    private boolean checkKeepChild(final Node child, final List<Node> keepList)
    {
        boolean addedChild = false;

        if (child instanceof Element) {
            final Element childElement = (Element) child;
            final String tn = childElement.getTagName();
            if (tn.equals(TAG_OL) || tn.equals(TAG_LI)) {
                getElementsToKeep(childElement, keepList);
            } else if (tn.equals(TAG_BR)) {
                // Ignore
            } else {
                addedChild = true;
            }
        } else {
            addedChild = true;
        }

        if (addedChild) {
            keepList.add(child.getParentNode().removeChild(child));
        }

        return addedChild;
    }

}
