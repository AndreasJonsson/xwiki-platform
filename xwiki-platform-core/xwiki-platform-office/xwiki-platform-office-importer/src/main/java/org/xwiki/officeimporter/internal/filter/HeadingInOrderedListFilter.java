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
 * 11. 
 * 111. 
 * 1111. 
 * 11111. (((
 * ===== Heading text =====
 * )))
 *
 * This filter removes the ordered list to produce the following:
 *
 * ===== Heading text =====
 *
 * The filter assumes that the ((( ))) has already been removed by the RedunancyFilter.
 *
 * Sometimes the importer also fails to terminate the ordered list such that the entire following section is included
 * inside the list.  This filter also handles these cases.
 *
 * The algorithm is as follows:
 *
 * For all heading tags,
 *
 *   Find an ancestor chain consisting of ol or li tags, the topmost being an ol.
 *
 *   In the DOM tree rooted at the topmost ol, remove all ol and li tags of ol lists while keeping the contents of the
 *   tags.
 *
 *   Replace the topmost ol with the preserved nodes in order in the original DOM.
 *
 * @version $Id$
 * @since 7.3M1
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
                final Element topElement = getTopElement(pe);
                if (!topElement.getTagName().equals(TAG_OL)) {
                    // We do not meddle with other kinds of lists.
                    return e;
                }
                return topElement;
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
